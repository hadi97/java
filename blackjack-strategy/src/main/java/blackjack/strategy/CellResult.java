package blackjack.strategy;

import java.util.EnumMap;
import java.util.Map;

public final class CellResult {
    public final HandSpec hand;
    public final int dealerUp;
    public final Map<Action, Double> evs = new EnumMap<>(Action.class);
    public Action best;

    public CellResult(HandSpec hand, int dealerUp) {
        this.hand = hand;
        this.dealerUp = dealerUp;
    }

    public void put(Action a, double ev) {
        evs.put(a, ev);
    }

    public void resolveBest() {
        Action bestA = null;
        double bestV = Double.NEGATIVE_INFINITY;
        for (Map.Entry<Action, Double> e : evs.entrySet()) {
            if (e.getValue() > bestV) {
                bestV = e.getValue();
                bestA = e.getKey();
            }
        }
        this.best = bestA;
    }

    public double bestEV() {
        return evs.get(best);
    }
}
