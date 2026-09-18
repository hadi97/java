package blackjack.calc;

/**
 * Bucket indices for a dealer's final-hand outcome distribution.
 * NATURAL is a sentinel for a two-card dealer blackjack: under this ruleset it always beats any
 * non-natural player total, and simply resolves as a full loss of the current stake, so it needs
 * no special payoff-multiplier logic beyond being distinguished from an ordinary 21.
 */
public final class DealerOutcome {
    public static final int BUST = 0;
    public static final int V17 = 1;
    public static final int V18 = 2;
    public static final int V19 = 3;
    public static final int V20 = 4;
    public static final int V21 = 5;
    public static final int NATURAL = 6;
    public static final int SIZE = 7;

    private DealerOutcome() {}

    public static int bucketForTotal(int total) {
        if (total < 17 || total > 21) throw new IllegalArgumentException("total=" + total);
        return total - 16;
    }
}
