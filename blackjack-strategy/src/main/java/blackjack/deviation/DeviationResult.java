package blackjack.deviation;

import blackjack.strategy.Action;
import blackjack.strategy.CellResult;

import java.util.List;

public final class DeviationResult {
    public final Cell cell;
    public final CellResult baseline;
    public final List<Threshold> thresholds;
    public final long totalSamples;

    public DeviationResult(Cell cell, CellResult baseline, List<Threshold> thresholds, long totalSamples) {
        this.cell = cell;
        this.baseline = baseline;
        this.thresholds = thresholds;
        this.totalSamples = totalSamples;
    }
}
