package blackjack.deviation;

import blackjack.core.Rank;
import blackjack.strategy.HandSpec;

import java.util.ArrayList;
import java.util.List;

/** Builds the full list of chart cells (hard 7-17, soft A2-A10, all pairs, x every up-card). */
public final class CellRegistry {

    public final List<Cell> cells = new ArrayList<>();
    /** For each HARD total, the list of non-pair (r1,r2) rank combos [values 2..10] that sum to it. */
    public final java.util.Map<Integer, int[][]> hardCombos = new java.util.HashMap<>();

    public CellRegistry() {
        for (int total = 7; total <= 17; total++) {
            List<int[]> combos = new ArrayList<>();
            for (int r1 = 1; r1 <= 9; r1++) {
                for (int r2 = r1 + 1; r2 <= 9; r2++) {
                    if (Rank.HARD_VALUE[r1] + Rank.HARD_VALUE[r2] == total) {
                        combos.add(new int[]{r1, r2});
                    }
                }
            }
            hardCombos.put(total, combos.toArray(new int[0][]));
        }

        int idx = 0;
        cells.add(new Cell(idx++, HandSpec.insurance(), Rank.ACE));
        for (int dealerUp = 0; dealerUp < Rank.COUNT; dealerUp++) {
            for (int total = 7; total <= 17; total++) {
                cells.add(new Cell(idx++, HandSpec.hard(total), dealerUp));
            }
            for (int otherRank = 1; otherRank <= 9; otherRank++) {
                cells.add(new Cell(idx++, HandSpec.soft(otherRank), dealerUp));
            }
            for (int rank = 0; rank < Rank.COUNT; rank++) {
                cells.add(new Cell(idx++, HandSpec.pair(rank), dealerUp));
            }
        }
    }

    public int size() {
        return cells.size();
    }
}
