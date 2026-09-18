package blackjack.core;

/**
 * Hand total / soft-total bookkeeping.
 *
 * A hand's state is packed into a single int as {@code total * 2 + (soft ? 1 : 0)} to avoid
 * allocation in the hot recursive EV paths. "Soft" means the total currently counts one ace as 11.
 */
public final class HandUtil {

    private HandUtil() {}

    public static int state(int total, boolean soft) {
        return total * 2 + (soft ? 1 : 0);
    }

    public static int total(int state) {
        return state >> 1;
    }

    public static boolean soft(int state) {
        return (state & 1) != 0;
    }

    public static boolean isBust(int state) {
        return total(state) > 21;
    }

    public static boolean isBlackjack(int state, int numCards) {
        return numCards == 2 && total(state) == 21;
    }

    /** Adds one card of the given rank to the hand, returning the new packed state. */
    public static int addCard(int state, int rank) {
        int t = total(state) + Rank.HARD_VALUE[rank];
        boolean s = soft(state);
        if (rank == Rank.ACE) {
            t += 10;
            s = true;
        }
        if (t > 21 && s) {
            t -= 10;
            s = false;
        }
        return state(t, s);
    }

    public static int startHand(int rank1, int rank2) {
        return addCard(addCard(state(0, false), rank1), rank2);
    }
}
