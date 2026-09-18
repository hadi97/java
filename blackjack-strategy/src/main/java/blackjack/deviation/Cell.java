package blackjack.deviation;

import blackjack.strategy.HandSpec;

/** One row-vs-upcard chart cell, plus the combos of underlying ranks that can form it (see Registry). */
public final class Cell {
    public final int index;
    public final HandSpec spec;
    public final int dealerUp;

    public Cell(int index, HandSpec spec, int dealerUp) {
        this.index = index;
        this.spec = spec;
        this.dealerUp = dealerUp;
    }
}
