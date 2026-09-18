package blackjack.deviation;

import blackjack.strategy.Action;

/** Per-thread (then merged) accumulation of summed EVs and sample counts, per cell per true-count bucket per action. */
public final class Accumulator {
    private static final int NUM_ACTIONS = Action.values().length;

    public final double[][][] sumEv; // [cellIndex][bucket][actionOrdinal]
    public final long[][] count;     // [cellIndex][bucket]

    public Accumulator(int numCells) {
        sumEv = new double[numCells][TrueCountBucketing.BUCKETS][NUM_ACTIONS];
        count = new long[numCells][TrueCountBucketing.BUCKETS];
    }

    public void record(int cellIndex, int bucket, java.util.Map<Action, Double> evs) {
        double[] slot = sumEv[cellIndex][bucket];
        for (java.util.Map.Entry<Action, Double> e : evs.entrySet()) {
            slot[e.getKey().ordinal()] += e.getValue();
        }
        count[cellIndex][bucket]++;
    }

    public void mergeInto(Accumulator target) {
        for (int c = 0; c < sumEv.length; c++) {
            for (int b = 0; b < TrueCountBucketing.BUCKETS; b++) {
                target.count[c][b] += count[c][b];
                for (int a = 0; a < NUM_ACTIONS; a++) {
                    target.sumEv[c][b][a] += sumEv[c][b][a];
                }
            }
        }
    }
}
