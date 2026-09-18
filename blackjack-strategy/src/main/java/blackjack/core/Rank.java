package blackjack.core;

/**
 * Rank indices used throughout the engine: 0=Ace, 1..8 = 2..9, 9 = Ten-value (10/J/Q/K collapsed).
 */
public final class Rank {
    public static final int ACE = 0;
    public static final int TEN = 9;
    public static final int COUNT = 10;

    /** Hard card value (Ace counted as 1 here; soft handling is done by the hand-total logic). */
    public static final int[] HARD_VALUE = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    /** Number of cards of this rank per 52-card deck (Ten-value has 4 ranks: 10,J,Q,K). */
    public static final int[] PER_DECK = {4, 4, 4, 4, 4, 4, 4, 4, 4, 16};

    /** Hi-Lo tags: 2-6 = +1, 7-9 = 0, 10/A = -1. */
    public static final int[] HI_LO = {-1, 1, 1, 1, 1, 1, 0, 0, 0, -1};

    private Rank() {}

    public static String name(int rank) {
        switch (rank) {
            case 0: return "A";
            case 1: return "2";
            case 2: return "3";
            case 3: return "4";
            case 4: return "5";
            case 5: return "6";
            case 6: return "7";
            case 7: return "8";
            case 8: return "9";
            case 9: return "T";
            default: throw new IllegalArgumentException("bad rank " + rank);
        }
    }
}
