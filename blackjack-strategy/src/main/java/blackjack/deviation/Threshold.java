package blackjack.deviation;

import blackjack.strategy.Action;

/** A single deviation breakpoint: "play `action` when trueCount is at/above (positive) or at/below (negative) this value." */
public final class Threshold {
    public final Action action;
    public final boolean positiveDirection; // true = "N+", false = "N-"
    public final double trueCount;

    public Threshold(Action action, boolean positiveDirection, double trueCount) {
        this.action = action;
        this.positiveDirection = positiveDirection;
        this.trueCount = trueCount;
    }

    public String label() {
        long rounded = Math.round(trueCount);
        return rounded + (positiveDirection ? "+" : "-");
    }
}
