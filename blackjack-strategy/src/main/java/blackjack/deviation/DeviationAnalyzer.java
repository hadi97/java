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

        Action last = baseline.best;
        for (int b = zero + 1; b < TrueCountBucketing.BUCKETS; b++) {
            if (counts[b] < MIN_SAMPLES_PER_BUCKET) continue;
            Action argmax = argmax(sums[b], counts[b], legal);
            if (argmax != null && argmax != last) {
                thresholds.add(new Threshold(argmax, true, TrueCountBucketing.trueCountOf(b)));
                last = argmax;
            }
        }
        last = baseline.best;
        for (int b = zero - 1; b >= 0; b--) {
            if (counts[b] < MIN_SAMPLES_PER_BUCKET) continue;
            Action argmax = argmax(sums[b], counts[b], legal);
            if (argmax != null && argmax != last) {
                thresholds.add(new Threshold(argmax, false, TrueCountBucketing.trueCountOf(b)));
                last = argmax;
            }
        }
        return new DeviationResult(cell, baseline, thresholds, total);
    }

    private static Action argmax(double[] sumsForBucket, long count, Set<Action> legal) {
        Action best = null;
        double bestV = Double.NEGATIVE_INFINITY;
        for (Action a : legal) {
            double avg = sumsForBucket[a.ordinal()] / count;
            if (a == Action.SURRENDER) avg = -0.5; // constant, not sampled per-bucket
            if (avg > bestV) {
                bestV = avg;
                best = a;
            }
        }
        return best;
    }
}
