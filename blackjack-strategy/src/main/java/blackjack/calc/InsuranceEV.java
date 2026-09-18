package blackjack.calc;

import blackjack.core.Composition;
import blackjack.core.Rank;

/**
 * Insurance / even-money EV. Insurance is a side bet of half the original wager, paying 2:1 if the
 * dealer has a ten-value hole card (blackjack). Per unit of the original bet:
 * EV = P(ten) * (+1) + (1 - P(ten)) * (-0.5) = 0.5 * (3*P(ten) - 1).
 * Breakeven is P(ten) = 1/3. "Even money" on a player blackjack is mathematically identical to
 * taking insurance (guarantees the 1:1 payout instead of gambling on the 3:2 vs push outcome).
 */
public final class InsuranceEV {
    private InsuranceEV() {}

    public static final double BREAKEVEN_PROB = 1.0 / 3.0;

    /** comp must exclude the dealer's Ace up-card and the player's own cards. */
    public static double ev(Composition comp) {
        double pTen = comp.prob(Rank.TEN);
        return 0.5 * (3.0 * pTen - 1.0);
    }

    public static double tenProbability(Composition comp) {
        return comp.prob(Rank.TEN);
    }
}
