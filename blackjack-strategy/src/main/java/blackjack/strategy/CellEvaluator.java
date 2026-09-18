package blackjack.strategy;

import blackjack.calc.InsuranceEV;
import blackjack.calc.PlayerEV;
import blackjack.calc.SplitEV;
import blackjack.core.Composition;
import blackjack.core.HandUtil;
import blackjack.core.Rank;
import blackjack.core.Rules;

import java.util.EnumMap;
import java.util.Map;

/**
 * Computes the EV of every legal action for a specific two-card hand (ranks r1, r2) against a
 * dealer up-card, given a composition that already has the dealer's up-card AND both of the
 * player's cards removed. This is the shared core used by both the static basic-strategy
 * generator (which aggregates over all card combos that form a given hard total) and the
 * count-conditioned deviation simulator (which evaluates whatever specific combo was actually
 * dealt in a simulated round).
 *
 * A fresh PlayerEV/SplitEV (and therefore a fresh memo cache) is created per call deliberately:
 * their caches are keyed by exact composition, and since the composition is essentially different
 * on every call here (a different simulated shoe state each time), caching across calls would
 * only grow memory without meaningfully improving the hit rate -- the memoization that actually
 * pays off is the one local to a single call's own recursive exploration, which these fresh
 * instances still provide in full.
 */
public final class CellEvaluator {
    private CellEvaluator() {}

    public static Map<Action, Double> evaluate(Rules rules, int r1, int r2, int dealerUp, Composition comp, boolean isPairCategory) {
        Map<Action, Double> m = new EnumMap<>(Action.class);
        int state = HandUtil.startHand(r1, r2);
        PlayerEV pev = new PlayerEV(dealerUp);

        m.put(Action.STAND, pev.standEV(comp, state));
        m.put(Action.HIT, pev.hitEV(comp, state));
        m.put(Action.DOUBLE, pev.doubleEV(comp, state));

        if (dealerUp != Rank.ACE) {
            m.put(Action.SURRENDER, PlayerEV.surrenderEV());
        }

        if (isPairCategory && r1 == r2) {
            SplitEV sev = new SplitEV(rules, dealerUp, r1);
            m.put(Action.SPLIT, sev.totalSplitEV(comp));
        }
        return m;
    }

    /** comp must exclude only the dealer's Ace up-card (insurance doesn't depend on the player's own hand). */
    public static Map<Action, Double> evaluateInsurance(Composition comp) {
        Map<Action, Double> m = new EnumMap<>(Action.class);
        m.put(Action.TAKE_INSURANCE, InsuranceEV.ev(comp));
        m.put(Action.DECLINE_INSURANCE, 0.0);
        return m;
    }
}
