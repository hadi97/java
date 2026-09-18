package blackjack.deviation;

import blackjack.core.Composition;
import blackjack.core.Rank;
import blackjack.core.Rules;
import blackjack.strategy.Action;
import blackjack.strategy.BasicStrategyGenerator;
import blackjack.strategy.CellEvaluator;
import blackjack.strategy.CellResult;
import blackjack.strategy.HandSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monte Carlo driver: repeatedly shuffles fresh N-deck shoes and walks through them card by card,
 * tracking the Hi-Lo running/true count as it goes. After each card is revealed, the CURRENT
 * remaining composition is an unbiased snapshot of "what a shoe looks like at this true count" --
 * card order doesn't affect the combinatorial EV engine (it only cares what's left to draw), so a
 * round-robin-selected chart cell is evaluated directly against that snapshot (removing the
 * dealer's up-card and the cell's hand cards from it) rather than needing to simulate an actual
 * multi-player hand structure. Results are pooled into per-thread Accumulators and merged.
 */
public final class DeviationSimulator {

    private final Rules rules;
    private final CellRegistry registry;

    public DeviationSimulator(Rules rules, CellRegistry registry) {
        this.rules = rules;
        this.registry = registry;
    }

    public List<DeviationResult> run(long totalSamples, int numThreads, java.util.function.LongConsumer progressCallback) throws InterruptedException {
        Accumulator merged = new Accumulator(registry.size());
        AtomicLong remaining = new AtomicLong(totalSamples);
        AtomicLong doneSoFar = new AtomicLong(0);
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        List<java.util.concurrent.Future<Accumulator>> futures = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            futures.add(pool.submit(() -> {
                Accumulator local = new Accumulator(registry.size());
                Worker w = new Worker(rules, registry, local, threadId, remaining, doneSoFar);
                w.runUntilExhausted();
                return local;
            }));
        }

        // progress reporting
        Thread reporter = null;
        if (progressCallback != null) {
            reporter = new Thread(() -> {
                try {
                    while (remaining.get() > 0) {
                        progressCallback.accept(doneSoFar.get());
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException ignored) {}
            });
            reporter.setDaemon(true);
            reporter.start();
        }

        for (java.util.concurrent.Future<Accumulator> f : futures) {
            try {
                Accumulator local = f.get();
                local.mergeInto(merged);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);
        if (reporter != null) reporter.interrupt();
        if (progressCallback != null) progressCallback.accept(doneSoFar.get());

        BasicStrategyGenerator gen = new BasicStrategyGenerator(rules);
        List<DeviationResult> results = new ArrayList<>();
        for (Cell cell : registry.cells) {
            CellResult baseline = computeBaseline(gen, cell);
            results.add(DeviationAnalyzer.analyze(cell, baseline, merged));
        }
        return results;
    }

    private CellResult computeBaseline(BasicStrategyGenerator gen, Cell cell) {
        switch (cell.spec.kind) {
            case INSURANCE: return gen.insuranceCell();
            case HARD: return gen.hardTotalCell(cell.spec.total, cell.dealerUp);
            case SOFT: return gen.softTotalCell(cell.spec.rank, cell.dealerUp);
            case PAIR: return gen.pairCell(cell.spec.rank, cell.dealerUp);
            default: throw new IllegalStateException();
        }
    }

    private static final class Worker {
        final Rules rules;
        final CellRegistry registry;
        final Accumulator acc;
        final Random rnd;
        final AtomicLong remaining;
        final AtomicLong doneSoFar;
        int cellPtr;

        Worker(Rules rules, CellRegistry registry, Accumulator acc, int threadId, AtomicLong remaining, AtomicLong doneSoFar) {
            this.rules = rules;
            this.registry = registry;
            this.acc = acc;
            this.rnd = new Random();
            this.remaining = remaining;
            this.doneSoFar = doneSoFar;
            this.cellPtr = threadId;
        }

        void runUntilExhausted() {
            int decks = rules.decks;
            int totalCards = 0;
            for (int i = 0; i < Rank.COUNT; i++) totalCards += Rank.PER_DECK[i] * decks;

            while (remaining.get() > 0) {
                int[] counts = freshCounts(decks);
                int[] order = shuffledOrder(decks, rnd);
                int runningCount = 0;
                int dealt = 0;
                int cutoff = (int) (totalCards * rules.penetration);

                for (int i = 0; i < order.length && dealt < cutoff; i++) {
                    int rank = order[i];
                    counts[rank]--;
                    runningCount += Rank.HI_LO[rank];
                    dealt++;

                    int remainingCards = totalCards - dealt;
                    if (remainingCards < 20) continue;
                    if (remaining.get() <= 0) break;

                    double decksRemaining = remainingCards / 52.0;
                    double trueCount = runningCount / decksRemaining;

                    if (processSample(counts, trueCount)) {
                        remaining.decrementAndGet();
                        doneSoFar.incrementAndGet();
                    }
                }
            }
        }

        boolean processSample(int[] shoeCounts, double trueCount) {
            Cell cell = registry.cells.get(cellPtr);
            cellPtr = (cellPtr + 1) % registry.cells.size();

            if (shoeCounts[cell.dealerUp] <= 0) return false;
            int[] afterDealer = shoeCounts.clone();
            afterDealer[cell.dealerUp]--;

            HandSpec spec = cell.spec;
            Map<Action, Double> evs;
            if (spec.kind == HandSpec.Kind.INSURANCE) {
                evs = CellEvaluator.evaluateInsurance(new Composition(afterDealer));
            } else {
                int r1, r2;
                if (spec.kind == HandSpec.Kind.PAIR) {
                    int rank = spec.rank;
                    if (afterDealer[rank] < 2) return false;
                    r1 = r2 = rank;
                    afterDealer[rank] -= 2;
                } else if (spec.kind == HandSpec.Kind.SOFT) {
                    if (afterDealer[Rank.ACE] < 1 || afterDealer[spec.rank] < 1) return false;
                    r1 = Rank.ACE;
                    r2 = spec.rank;
                    afterDealer[r1]--;
                    afterDealer[r2]--;
                } else {
                    int[][] combos = registry.hardCombos.get(spec.total);
                    int[] picked = pickWeighted(combos, afterDealer, rnd);
                    if (picked == null) return false;
                    r1 = picked[0];
                    r2 = picked[1];
                    afterDealer[r1]--;
                    afterDealer[r2]--;
                }
                Composition comp = new Composition(afterDealer);
                evs = CellEvaluator.evaluate(rules, r1, r2, cell.dealerUp, comp, spec.kind == HandSpec.Kind.PAIR);
            }

            int bucket = TrueCountBucketing.bucketOf(trueCount);
            acc.record(cell.index, bucket, evs);
            return true;
        }
    }

    private static int[] freshCounts(int decks) {
        int[] c = new int[Rank.COUNT];
        for (int i = 0; i < Rank.COUNT; i++) c[i] = Rank.PER_DECK[i] * decks;
        return c;
    }

    private static int[] shuffledOrder(int decks, Random rnd) {
        int total = 0;
        for (int i = 0; i < Rank.COUNT; i++) total += Rank.PER_DECK[i] * decks;
        int[] order = new int[total];
        int p = 0;
        for (int r = 0; r < Rank.COUNT; r++) {
            int n = Rank.PER_DECK[r] * decks;
            for (int k = 0; k < n; k++) order[p++] = r;
        }
        for (int i = order.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = order[i];
            order[i] = order[j];
            order[j] = tmp;
        }
        return order;
    }

    private static int[] pickWeighted(int[][] combos, int[] counts, Random rnd) {
        int n = combos.length;
        if (n == 0) return null;
        long[] weights = new long[n];
        long sum = 0;
        for (int i = 0; i < n; i++) {
            long w = (long) counts[combos[i][0]] * counts[combos[i][1]];
            weights[i] = w;
            sum += w;
        }
        if (sum <= 0) return null;
        long pick = (long) (rnd.nextDouble() * sum);
        long acc = 0;
        for (int i = 0; i < n; i++) {
            acc += weights[i];
            if (pick < acc) return combos[i];
        }
        return combos[n - 1];
    }
}
