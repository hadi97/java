package blackjack.strategy;

import blackjack.core.Composition;
import blackjack.core.Rank;
import blackjack.core.Rules;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the true-count-0 baseline ("basic strategy") for every chart cell, using a fresh
 * N-deck shoe with only the dealer's up-card removed as the reference composition.
 *
 * For soft totals and pairs there is exactly one card combination that forms the hand, so this is
 * unambiguous. For hard totals (7..17) there are multiple non-pair two-card combinations that sum
 * to the same total (e.g. hard 16 = 9+7 or T+6); this generator evaluates every valid combination
 * and combines them into a single composition-weighted average EV per action, weighted by how
 * often each combination actually occurs in the fresh shoe (count[r1] * count[r2]). This mirrors
 * how the deviation simulator naturally pools combinations that share a total, so the TC=0 row
 * here is consistent with the TC=0 slice of the simulated data.
 */
public final class BasicStrategyGenerator {

    private final Rules rules;

    public BasicStrategyGenerator(Rules rules) {
        this.rules = rules;
    }

    public List<CellResult> generateAll() {
        List<CellResult> results = new ArrayList<>();
        results.add(insuranceCell());
        for (int dealerUp = 0; dealerUp < Rank.COUNT; dealerUp++) {
            for (int total = 7; total <= 17; total++) {
                results.add(hardTotalCell(total, dealerUp));
            }
            for (int otherRank = 1; otherRank <= 9; otherRank++) {
                results.add(softTotalCell(otherRank, dealerUp));
            }
            for (int rank = 0; rank < Rank.COUNT; rank++) {
                results.add(pairCell(rank, dealerUp));
            }
        }
        return results;
    }

    public CellResult hardTotalCell(int total, int dealerUp) {
        HandSpec spec = HandSpec.hard(total);
        Composition base = Composition.freshShoe(rules.decks).remove(dealerUp);

        Map<Action, double[]> weightedSum = new EnumMap<>(Action.class); // [weightedEvSum, weightSum]
        for (int r1 = 1; r1 <= 9; r1++) {
            for (int r2 = r1 + 1; r2 <= 9; r2++) {
                if (Rank.HARD_VALUE[r1] + Rank.HARD_VALUE[r2] != total) continue;
                int c1 = base.count(r1);
                int c2 = base.count(r2);
                if (c1 == 0 || c2 == 0) continue;
                double weight = (double) c1 * c2;
                Composition comp = base.remove(r1).remove(r2);
                Map<Action, Double> evs = CellEvaluator.evaluate(rules, r1, r2, dealerUp, comp, false);
                for (Map.Entry<Action, Double> e : evs.entrySet()) {
                    double[] acc = weightedSum.computeIfAbsent(e.getKey(), k -> new double[2]);
                    acc[0] += weight * e.getValue();
                    acc[1] += weight;
                }
            }
        }
        CellResult result = new CellResult(spec, dealerUp);
        for (Map.Entry<Action, double[]> e : weightedSum.entrySet()) {
            double[] acc = e.getValue();
            result.put(e.getKey(), acc[1] == 0 ? Double.NEGATIVE_INFINITY : acc[0] / acc[1]);
        }
        result.resolveBest();
        return result;
    }

    public CellResult softTotalCell(int otherRank, int dealerUp) {
        HandSpec spec = HandSpec.soft(otherRank);
        Composition base = Composition.freshShoe(rules.decks).remove(dealerUp);
        Composition comp = base.remove(Rank.ACE).remove(otherRank);
        Map<Action, Double> evs = CellEvaluator.evaluate(rules, Rank.ACE, otherRank, dealerUp, comp, false);
        CellResult result = new CellResult(spec, dealerUp);
        evs.forEach(result::put);
        result.resolveBest();
        return result;
    }

    public CellResult insuranceCell() {
        HandSpec spec = HandSpec.insurance();
        Composition comp = Composition.freshShoe(rules.decks).remove(Rank.ACE);
        Map<Action, Double> evs = CellEvaluator.evaluateInsurance(comp);
        CellResult result = new CellResult(spec, Rank.ACE);
        evs.forEach(result::put);
        result.resolveBest();
        return result;
    }

    public CellResult pairCell(int rank, int dealerUp) {
        HandSpec spec = HandSpec.pair(rank);
        Composition base = Composition.freshShoe(rules.decks).remove(dealerUp);
        Composition comp = base.remove(rank).remove(rank);
        Map<Action, Double> evs = CellEvaluator.evaluate(rules, rank, rank, dealerUp, comp, true);
        CellResult result = new CellResult(spec, dealerUp);
        evs.forEach(result::put);
        result.resolveBest();
        return result;
    }
}
