package blackjack.calc;

import blackjack.core.Composition;
import blackjack.core.HandUtil;
import blackjack.core.Rank;
import blackjack.util.LongDoubleMap;

/**
 * Exact recursive EV engine for a single (non-split) hand: stand, hit (full recursive expectimax
 * "play on to the end" search), double, and early surrender.
 *
 * All EVs are per unit of the ORIGINAL bet (so doubleEV already reflects the doubled stake, i.e.
 * it can range roughly -2..+2). A fresh instance should be created per top-level decision-cell
 * computation (one dealer up-card context); its internal memo caches are only valid for that
 * up-card and are not thread-shared.
 */
public final class PlayerEV {

    private final DealerCalculator dealerCalc = new DealerCalculator();
    private final LongDoubleMap playCache = new LongDoubleMap(128);
    private final int dealerUp;

    public PlayerEV(int dealerUp) {
        this.dealerUp = dealerUp;
    }

    public static double surrenderEV() {
        return -0.5;
    }

    /** EV of standing now, per unit of the current stake (not yet scaled for doubled/split stakes). */
    public double standEV(Composition comp, int state) {
        double[] dist = dealerCalc.distribution(comp, dealerUp);
        int total = HandUtil.total(state);
        double ev = 0.0;
        ev += dist[DealerOutcome.BUST] * 1.0;
        ev += dist[DealerOutcome.NATURAL] * -1.0;
        for (int bucket = DealerOutcome.V17; bucket <= DealerOutcome.V21; bucket++) {
            int dealerTotal = 16 + bucket; // V17->17 .. V21->21
            double p = dist[bucket];
            if (p == 0.0) continue;
            if (dealerTotal > total) ev += p * -1.0;
            else if (dealerTotal < total) ev += p * 1.0;
            // equal -> push, contributes 0
        }
        return ev;
    }

    /** EV of hitting now and then playing optimally (recursive), per unit stake. */
    public double hitEV(Composition comp, int state) {
        int tot0 = comp.total();
        if (tot0 == 0) return -1.0;
        double ev = 0.0;
        for (int r = 0; r < Rank.COUNT; r++) {
            int cnt = comp.count(r);
            if (cnt == 0) continue;
            double p = ((double) cnt) / tot0;
            Composition c2 = comp.remove(r);
            int ns = HandUtil.addCard(state, r);
            if (HandUtil.isBust(ns)) {
                ev += p * -1.0;
            } else {
                ev += p * playEV(c2, ns);
            }
        }
        return ev;
    }

    /** Optimal EV of the hand from this point (max of stand / hit-and-continue), memoized. */
    public double playEV(Composition comp, int state) {
        int total = HandUtil.total(state);
        if (total >= 21) {
            // Standing on 21 always dominates hitting (hitting can never improve on 21, only push
            // in the impossible case of drawing nothing, or bust/lose ground).
            return standEV(comp, state);
        }
        long key = (comp.key() << 6) | (state & 0x3F);
        double cached = playCache.get(key);
        if (!Double.isNaN(cached)) return cached;

        double stand = standEV(comp, state);
        double hit = hitEV(comp, state);
        double best = Math.max(stand, hit);
        playCache.put(key, best);
        return best;
    }

    /** EV of doubling: exactly one more card, then forced stand, at double the stake. */
    public double doubleEV(Composition comp, int state) {
        int tot0 = comp.total();
        if (tot0 == 0) return -2.0;
        double ev = 0.0;
        for (int r = 0; r < Rank.COUNT; r++) {
            int cnt = comp.count(r);
            if (cnt == 0) continue;
            double p = ((double) cnt) / tot0;
            Composition c2 = comp.remove(r);
            int ns = HandUtil.addCard(state, r);
            if (HandUtil.isBust(ns)) {
                ev += p * -2.0;
            } else {
                ev += p * 2.0 * standEV(c2, ns);
            }
        }
        return ev;
    }
}
