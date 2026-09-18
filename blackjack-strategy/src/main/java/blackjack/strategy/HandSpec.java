package blackjack.strategy;

import blackjack.core.Rank;

public final class HandSpec {
    public enum Kind { HARD, SOFT, PAIR, INSURANCE }

    public final Kind kind;
    public final int total;   // HARD: the hard total. SOFT: total counting ace as 11 (13..21). PAIR: 2*cardValue.
    public final int rank;    // SOFT: the non-ace rank. PAIR: the paired rank. HARD: unused (-1).
    public final String label;

    private HandSpec(Kind kind, int total, int rank, String label) {
        this.kind = kind;
        this.total = total;
        this.rank = rank;
        this.label = label;
    }

    public static HandSpec hard(int total) {
        return new HandSpec(Kind.HARD, total, -1, String.valueOf(total));
    }

    public static HandSpec soft(int otherRank) {
        int total = 11 + Rank.HARD_VALUE[otherRank];
        String label = "A," + Rank.name(otherRank).replace("T", "10");
        return new HandSpec(Kind.SOFT, total, otherRank, label);
    }

    public static HandSpec pair(int rank) {
        String rn = Rank.name(rank).replace("T", "10");
        int total = (rank == Rank.ACE) ? 12 : 2 * Rank.HARD_VALUE[rank];
        return new HandSpec(Kind.PAIR, total, rank, rn + "," + rn);
    }

    public static HandSpec insurance() {
        return new HandSpec(Kind.INSURANCE, 0, -1, "Insurance");
    }
}
