package blackjack.output;

import blackjack.core.Rank;
import blackjack.strategy.Action;
import blackjack.strategy.CellResult;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Plain true-count-0 basic strategy dump: one row per cell with the best action and every action's EV. */
public final class BasicStrategyCsvExporter {
    private BasicStrategyCsvExporter() {}

    public static void write(List<CellResult> results, Path outFile) throws IOException {
        try (Writer w = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            w.write("category,hand,dealer_up,best_action,best_ev,action_evs\n");
            for (CellResult r : results) {
                w.write(csv(r.hand.kind.name()));
                w.write(',');
                w.write(csv(r.hand.label));
                w.write(',');
                w.write(csv(Rank.name(r.dealerUp)));
                w.write(',');
                w.write(csv(r.best.name()));
                w.write(',');
                w.write(String.format("%.6f", r.bestEV()));
                w.write(',');
                w.write(csv(actionEvsString(r)));
                w.write('\n');
            }
        }
    }

    private static String actionEvsString(CellResult r) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Action, Double> e : r.evs.entrySet()) {
            if (sb.length() > 0) sb.append(';');
            sb.append(e.getKey()).append('=').append(String.format("%.5f", e.getValue()));
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
