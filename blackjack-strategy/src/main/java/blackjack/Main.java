package blackjack;

import blackjack.core.Rules;
import blackjack.deviation.CellRegistry;
import blackjack.deviation.DeviationResult;
import blackjack.deviation.DeviationSimulator;
import blackjack.output.CsvExporter;
import blackjack.output.HtmlChartRenderer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI entry point.
 *
 * Usage:
 *   java -cp out blackjack.Main [--rounds=N] [--threads=N] [--decks=N] [--penetration=P] [--out=DIR] [--csv-only] [--html-only]
 *
 * See README.md for what --rounds controls and how runtime scales with it.
 */
public final class Main {
    public static void main(String[] args) throws Exception {
        long rounds = 3_000_000L;
        int threads = Runtime.getRuntime().availableProcessors();
        int decks = 6;
        double penetration = 0.75;
        String outDir = "output";
        boolean doCsv = true;
        boolean doHtml = true;

        for (String arg : args) {
            if (arg.startsWith("--rounds=")) rounds = Long.parseLong(arg.substring(9));
            else if (arg.startsWith("--threads=")) threads = Integer.parseInt(arg.substring(10));
            else if (arg.startsWith("--decks=")) decks = Integer.parseInt(arg.substring(8));
            else if (arg.startsWith("--penetration=")) penetration = Double.parseDouble(arg.substring(14));
            else if (arg.startsWith("--out=")) outDir = arg.substring(6);
            else if (arg.equals("--csv-only")) doHtml = false;
            else if (arg.equals("--html-only")) doCsv = false;
            else if (arg.equals("--help") || arg.equals("-h")) {
                printHelp();
                return;
            } else {
                System.err.println("Unknown argument: " + arg);
                printHelp();
                System.exit(1);
            }
        }

        Rules rules = new Rules(decks, penetration);
        CellRegistry registry = new CellRegistry();

        System.out.printf("Blackjack strategy + deviation generator%n");
        System.out.printf("Rules: %d decks, S17, ENHC, DAS, split to %d hands, aces split once/no-resplit/no-hit, "
                        + "early surrender vs 2-9/T, BJ pays 3:2, penetration %.0f%%%n",
                rules.decks, rules.maxSplitHands, rules.penetration * 100);
        System.out.printf("Chart cells: %d (110 hard, 90 soft, 100 pairs, 1 insurance)%n", registry.size());
        System.out.printf("Monte Carlo budget: %,d samples total (~%,d per cell) across %d thread(s)%n",
                rounds, rounds / registry.size(), threads);
        System.out.println("This can take anywhere from minutes to many hours depending on --rounds and your CPU core count -- see README.md.");

        long start = System.currentTimeMillis();
        final long totalRounds = rounds;
        DeviationSimulator sim = new DeviationSimulator(rules, registry);
        List<DeviationResult> results = sim.run(rounds, threads, done -> {
            double pct = 100.0 * done / totalRounds;
            long elapsed = System.currentTimeMillis() - start;
            double rate = done / Math.max(1.0, elapsed / 1000.0);
            System.out.printf("  progress: %,d / %,d (%.1f%%) -- %.0f samples/sec%n", done, totalRounds, pct, rate);
        });
        long elapsedMs = System.currentTimeMillis() - start;
        System.out.printf("Simulation complete in %.1f minutes.%n", elapsedMs / 60000.0);

        Path outPath = Path.of(outDir);
        Files.createDirectories(outPath);

        if (doCsv) {
            Path csv = outPath.resolve("deviation_chart.csv");
            CsvExporter.write(results, csv);
            System.out.println("Wrote " + csv.toAbsolutePath());
        }
        if (doHtml) {
            Path html = outPath.resolve("deviation_chart.html");
            HtmlChartRenderer.write(rules, results, html);
            System.out.println("Wrote " + html.toAbsolutePath());
        }
    }

    private static void printHelp() {
        System.out.println("Usage: java -cp out blackjack.Main [options]\n"
                + "  --rounds=N        total Monte Carlo samples across all cells (default 3000000)\n"
                + "  --threads=N       worker threads (default = CPU core count)\n"
                + "  --decks=N         number of decks (default 6)\n"
                + "  --penetration=P   fraction of shoe dealt before reshuffle, 0-1 (default 0.75)\n"
                + "  --out=DIR         output directory (default 'output')\n"
                + "  --csv-only        skip HTML output\n"
                + "  --html-only       skip CSV output\n");
    }
}
