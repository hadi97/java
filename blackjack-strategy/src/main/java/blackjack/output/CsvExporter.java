package blackjack.output;

import blackjack.core.Rank;
import blackjack.deviation.DeviationResult;
import blackjack.deviation.Threshold;
import blackjack.strategy.Action;
import blackjack.strategy.CellResult;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Raw per-cell data dump: baseline action/EVs and every deviation threshold found. */
public final class CsvExporter {
    private CsvExporter() {}

    public static void write(List<DeviationResult> results, Path outFile) throws IOException {
        try (Writer w = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            w.write("category,hand,dealer_up,baseline_action,baseline_ev,action_evs,thresholds,samples\n");
            for (DeviationResult r : results) {
                CellResult b = r.baseline;
                w.write(csv(r.cell.spec.kind.name()));
                w.write(',');
                w.write(csv(b.hand.label));
                w.write(',');
                w.write(csv(Rank.name(r.cell.dealerUp)));
                w.write(',');
                w.write(csv(b.best.name()));
                w.write(',');
                w.write(String.format("%.6f", b.bestEV()));
                w.write(',');
                w.write(csv(actionEvsString(b)));
                w.write(',');
                w.write(csv(thresholdsString(r.thresholds)));
                w.write(',');
                w.write(String.valueOf(r.totalSamples));
                w.write('\n');
            }
        }
    }

    private static String actionEvsString(CellResult b) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Action, Double> e : b.evs.entrySet()) {
            if (sb.length() > 0) sb.append(';');
            sb.append(e.getKey()).append('=').append(String.format("%.5f", e.getValue()));
        }
        return sb.toString();
    }

    private static String thresholdsString(List<Threshold> thresholds) {
        StringBuilder sb = new StringBuilder();
        for (Threshold t : thresholds) {
            if (sb.length() > 0) sb.append(';');
            sb.append(t.action).append('@').append(t.label());
        }
        return sb.toString();
    }

    private static String csv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}
