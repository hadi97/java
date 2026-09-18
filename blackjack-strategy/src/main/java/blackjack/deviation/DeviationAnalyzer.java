package blackjack.deviation;

import blackjack.core.Rank;
import blackjack.strategy.Action;
import blackjack.strategy.CellResult;
import blackjack.strategy.HandSpec;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Turns accumulated Monte Carlo EV sums into per-cell deviation thresholds: the true-count
 * breakpoints where the argmax action (by simulated average EV) changes from the exact
 * true-count-0 basic-strategy action, scanning outward from TC=0 in both directions.
 */
public final class DeviationAnalyzer {

    public static final long MIN_SAMPLES_PER_BUCKET = 30;

    /**
     * Neighboring true-count buckets are pooled (summed) within this radius before computing an
     * argmax, and a candidate action must win PERSISTENCE consecutive pooled buckets in a row
     * before it's accepted as a real threshold. Both exist purely to reject single-bucket sampling
     * noise on close decisions: without them, a near-tie action pair (e.g. hit vs double on a
     * marginal hand) can flip back and forth bucket-to-bucket from pure variance, producing a
     * nonsensical oscillating threshold list instead of the single monotonic crossover a real
     * deviation should have.
     */
    private static final int POOL_RADIUS = 1; // pool bucket +/- this many neighbors (step=0.5, so radius 1 = a 1.0 true-count-wide window)
    private static final int PERSISTENCE = 2; // consecutive pooled buckets that must agree before accepting a flip

    private DeviationAnalyzer() {}

    public static Set<Action> legalActions(Cell cell) {
        if (cell.spec.kind == HandSpec.Kind.INSURANCE) {
            return EnumSet.of(Action.TAKE_INSURANCE, Action.DECLINE_INSURANCE);
        }
        Set<Action> s = EnumSet.of(Action.STAND, Action.HIT, Action.DOUBLE);
        if (cell.dealerUp != Rank.ACE) s.add(Action.SURRENDER);
        if (cell.spec.kind == HandSpec.Kind.PAIR) s.add(Action.SPLIT);
        return s;
    }

    public static DeviationResult analyze(Cell cell, CellResult baseline, Accumulator acc) {
        Set<Action> legal = legalActions(cell);
        double[][] sums = acc.sumEv[cell.index];
        long[] counts = acc.count[cell.index];
        long total = 0;
        for (long c : counts) total += c;

        int zero = TrueCountBucketing.zeroBucket();
        List<Threshold> thresholds = new ArrayList<>();

        scan(sums, counts, legal, baseline.best, zero + 1, TrueCountBucketing.BUCKETS, 1, true, thresholds);
        scan(sums, counts, legal, baseline.best, zero - 1, -1, -1, false, thresholds);

        return new DeviationResult(cell, baseline, thresholds, total);
    }

    private static void scan(double[][] sums, long[] counts, Set<Action> legal, Action baselineAction,
                              int start, int end, int step, boolean positiveDirection, List<Threshold> thresholds) {
        Action last = baselineAction;
        Action candidate = null;
        int candidateRunStart = -1;
        int consecutive = 0;

        for (int b = start; b != end; b += step) {
            PooledStats p = pool(sums, counts, b);
            if (p.count < MIN_SAMPLES_PER_BUCKET) {
                candidate = null;
                consecutive = 0;
                continue;
            }
            Action argmax = argmax(p.sum, p.count, legal);
            if (argmax == null || argmax == last) {
                candidate = null;
                consecutive = 0;
                continue;
            }
            if (argmax == candidate) {
                consecutive++;
            } else {
                candidate = argmax;
                candidateRunStart = b;
                consecutive = 1;
            }
            if (consecutive >= PERSISTENCE) {
                thresholds.add(new Threshold(argmax, positiveDirection, TrueCountBucketing.trueCountOf(candidateRunStart)));
                last = argmax;
                candidate = null;
                consecutive = 0;
            }
        }
    }

    private static final class PooledStats {
        final double[] sum;
        final long count;
        PooledStats(double[] sum, long count) {
            this.sum = sum;
            this.count = count;
        }
    }

    private static PooledStats pool(double[][] sums, long[] counts, int center) {
        double[] sum = new double[Action.values().length];
        long count = 0;
        int lo = Math.max(0, center - POOL_RADIUS);
        int hi = Math.min(TrueCountBucketing.BUCKETS - 1, center + POOL_RADIUS);
        for (int b = lo; b <= hi; b++) {
            count += counts[b];
            for (int a = 0; a < sum.length; a++) sum[a] += sums[b][a];
        }
        return new PooledStats(sum, count);
    }

    private static Action argmax(double[] pooledSum, long pooledCount, Set<Action> legal) {
        Action best = null;
        double bestV = Double.NEGATIVE_INFINITY;
        for (Action a : legal) {
            double avg = (a == Action.SURRENDER) ? -0.5 : pooledSum[a.ordinal()] / pooledCount;
            if (avg > bestV) {
                bestV = avg;
                best = a;
            }
        }
        return best;
    }
}
