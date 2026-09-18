package blackjack.calc;

import blackjack.core.Composition;
import blackjack.core.HandUtil;
import blackjack.core.Rank;
import blackjack.util.LongObjectMap;

/**
 * Computes the dealer's exact final-outcome probability distribution given an up-card and the
 * remaining shoe composition, under S17 (dealer stands on all 17s, including soft 17) and ENHC
 * (the hole card is just another card drawn from the same composition -- there is no separate
 * "peek" branch to model beyond distinguishing a two-card natural).
 *
 * A fresh memo cache is created per top-level call (one call = one decision-cell EV computation),
 * since compositions essentially never repeat across independent Monte Carlo samples -- the cache
 * only needs to pay for itself within a single recursive call tree, where it does (many draw paths
 * converge on the same remaining composition + running total).
 */
public final class DealerCalculator {

    private static final double[] BUST_DIST = zeroed();

    private static double[] zeroed() {
        double[] d = new double[DealerOutcome.SIZE];
        d[DealerOutcome.BUST] = 1.0;
        return d;
    }

    private final LongObjectMap<double[]> cache = new LongObjectMap<>(64);

    /** Distribution over the dealer's final outcome, given the up-card and composition (up-card already removed from comp). */
    public double[] distribution(Composition comp, int upcard) {
        double[] dist = new double[DealerOutcome.SIZE];
        int startState = HandUtil.state(0, false);
        startState = HandUtil.addCard(startState, upcard);

        if (upcard == Rank.ACE) {
            double pTen = comp.prob(Rank.TEN);
            dist[DealerOutcome.NATURAL] += pTen;
            for (int hole = 0; hole < Rank.COUNT; hole++) {
                if (hole == Rank.TEN || !comp.has(hole)) continue;
                addBranch(dist, comp, hole, startState);
            }
        } else if (upcard == Rank.TEN) {
            double pAce = comp.prob(Rank.ACE);
            dist[DealerOutcome.NATURAL] += pAce;
            for (int hole = 0; hole < Rank.COUNT; hole++) {
                if (hole == Rank.ACE || !comp.has(hole)) continue;
                addBranch(dist, comp, hole, startState);
            }
        } else {
            for (int hole = 0; hole < Rank.COUNT; hole++) {
                if (!comp.has(hole)) continue;
                addBranch(dist, comp, hole, startState);
            }
        }
        return dist;
    }

    private void addBranch(double[] dist, Composition comp, int hole, int startState) {
        double p = comp.prob(hole);
        Composition c2 = comp.remove(hole);
        int state = HandUtil.addCard(startState, hole);
        double[] sub = recurse(c2, state);
        for (int i = 0; i < DealerOutcome.SIZE; i++) dist[i] += p * sub[i];
    }

    private double[] recurse(Composition comp, int state) {
        int total = HandUtil.total(state);
        if (total > 21) return BUST_DIST;
        if (total >= 17) {
            double[] d = new double[DealerOutcome.SIZE];
            d[DealerOutcome.bucketForTotal(total)] = 1.0;
            return d;
        }
        long key = (comp.key() << 6) | (state & 0x3F);
        double[] cached = cache.get(key);
        if (cached != null) return cached;

        double[] d = new double[DealerOutcome.SIZE];
        int total0 = comp.total();
        if (total0 > 0) {
            for (int r = 0; r < Rank.COUNT; r++) {
                int cnt = comp.count(r);
                if (cnt == 0) continue;
                double p = ((double) cnt) / total0;
                Composition c2 = comp.remove(r);
                int ns = HandUtil.addCard(state, r);
                double[] sub = recurse(c2, ns);
                for (int i = 0; i < DealerOutcome.SIZE; i++) d[i] += p * sub[i];
            }
        }
        cache.put(key, d);
        return d;
    }
}
