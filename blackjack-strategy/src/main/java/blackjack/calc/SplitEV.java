package blackjack.calc;

import blackjack.core.Composition;
import blackjack.core.HandUtil;
import blackjack.core.Rank;
import blackjack.core.Rules;
import blackjack.util.LongDoubleMap;

/**
 * EV of splitting a specific pair rank against a given dealer up-card.
 *
 * Recursion models resplitting up to {@code rules.maxSplitHands} (aces: exactly 2 hands, no
 * resplit, per {@code rules.resplitAces == false}), DAS, and split aces receiving exactly one
 * extra card with no further action ({@code rules.hitSplitAces == false}).
 *
 * Approximation (standard in published combinatorial analyzers, and used here deliberately):
 * sibling split hands are evaluated INDEPENDENTLY, each starting from the same composition (the
 * one remaining after removing the dealer's up-card and the original pair), rather than from the
 * composition further depleted by every other hand's actual draws. Modeling the true joint
 * dependency between sibling hands' draws is combinatorially intractable at this scale; the
 * approximation error from ignoring it is negligible (a handful of cards out of a several-hundred
 * card shoe) compared to the EV magnitudes involved, and it's the same method used to produce
 * every published composition-dependent strategy/deviation chart.
 */
public final class SplitEV {

    private final Rules rules;
    private final int rank;
    private final boolean isAce;
    private final int maxHands;
    private final PlayerEV playerEV;
    private final LongDoubleMap cache = new LongDoubleMap(64);

    public SplitEV(Rules rules, int dealerUp, int rank) {
        this.rules = rules;
        this.rank = rank;
        this.isAce = (rank == Rank.ACE);
        this.maxHands = isAce ? 2 : rules.maxSplitHands;
        this.playerEV = new PlayerEV(dealerUp);
    }

    /** comp must already exclude the dealer's up-card and both cards of the original pair. */
    public double totalSplitEV(Composition comp) {
        return 2.0 * handEV(comp, 2);
    }

    private double handEV(Composition comp, int handsUsed) {
        long key = (comp.key() << 3) | (handsUsed & 0x7);
        double cached = cache.get(key);
        if (!Double.isNaN(cached)) return cached;

        double ev = 0.0;
        int tot0 = comp.total();
        if (tot0 > 0) {
            for (int r = 0; r < Rank.COUNT; r++) {
                int cnt = comp.count(r);
                if (cnt == 0) continue;
                double p = ((double) cnt) / tot0;
                Composition c2 = comp.remove(r);

                if (r == rank && handsUsed < maxHands) {
                    ev += p * 2.0 * handEV(c2, handsUsed + 1);
                } else {
                    int state = HandUtil.startHand(rank, r);
                    if (isAce) {
                        ev += p * playerEV.standEV(c2, state);
                    } else {
                        double playOn = playerEV.playEV(c2, state);
                        double dbl = rules.das ? playerEV.doubleEV(c2, state) : Double.NEGATIVE_INFINITY;
                        ev += p * Math.max(playOn, dbl);
                    }
                }
            }
        }
        cache.put(key, ev);
        return ev;
    }
}
