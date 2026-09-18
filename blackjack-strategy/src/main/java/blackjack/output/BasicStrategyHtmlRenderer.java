package blackjack.output;

import blackjack.core.Rank;
import blackjack.core.Rules;
import blackjack.strategy.Action;
import blackjack.strategy.CellResult;
import blackjack.strategy.HandSpec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the plain true-count-0 basic strategy (no deviation/threshold data at all) as a
 * self-contained HTML page, laid out like the reference chart. For the true-count thresholds,
 * see {@link DeviationHtmlRenderer}.
 */
public final class BasicStrategyHtmlRenderer {
    private BasicStrategyHtmlRenderer() {}

    private static final int[] UP_ORDER = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0}; // 2..9, T, A

    public static void write(Rules rules, List<CellResult> results, Path outFile) throws IOException {
        Map<String, CellResult> byKey = new HashMap<>();
        CellResult insurance = null;
        for (CellResult r : results) {
            if (r.hand.kind == HandSpec.Kind.INSURANCE) {
                insurance = r;
            } else {
                byKey.put(key(r.hand.kind, r.hand.label, r.dealerUp), r);
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>Basic Strategy Chart</title>");
        html.append("<style>").append(CSS).append("</style></head><body>");
        html.append("<h1>Basic Strategy Chart</h1>");
        html.append("<div class='rules'>").append(rulesSummary(rules)).append("</div>");
        html.append("<p class='note'>True-count-0 exact composition-dependent basic strategy. For the true-count "
                + "deviation thresholds, see the separate deviations chart.</p>");

        html.append(sectionHeader("Pair Splitting"));
        int[] pairRanks = {0, 9, 8, 7, 6, 5, 4, 3, 2, 1}; // A,T,9,8,7,6,5,4,3,2
        html.append(tableOpen());
        for (int rank : pairRanks) {
            String label = HandSpec.pair(rank).label;
            html.append(row(label, HandSpec.Kind.PAIR, label, byKey));
        }
        html.append(tableClose());

        html.append(sectionHeader("Soft Totals"));
        html.append(tableOpen());
        for (int other = 9; other >= 1; other--) {
            String label = HandSpec.soft(other).label;
            html.append(row(label, HandSpec.Kind.SOFT, label, byKey));
        }
        html.append(tableClose());

        html.append(sectionHeader("Hard Totals"));
        html.append(tableOpen());
        for (int total = 17; total >= 7; total--) {
            String label = String.valueOf(total);
            html.append(row(label, HandSpec.Kind.HARD, label, byKey));
        }
        html.append(tableClose());

        html.append(sectionHeader("Late Surrender (where SUR is the baseline play)"));
        html.append(surrenderTable(byKey));

        html.append(sectionHeader("Insurance / Even Money"));
        html.append(insuranceBlock(insurance));

        html.append(legend());
        html.append("</body></html>");

        Files.write(outFile, html.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String rulesSummary(Rules rules) {
        return String.format("%d decks &middot; S17 &middot; ENHC (full loss on doubles/splits vs dealer blackjack) &middot; DAS &middot; "
                        + "split to %d hands &middot; aces split once, one card, no resplit/hit &middot; early surrender vs 2-9,T (not vs A) &middot; "
                        + "blackjack pays 3:2",
                rules.decks, rules.maxSplitHands);
    }

    private static String key(HandSpec.Kind kind, String label, int dealerUp) {
        return kind + "|" + label + "|" + dealerUp;
    }

    private static String sectionHeader(String title) {
        return "<h2>" + esc(title) + "</h2>";
    }

    private static String tableOpen() {
        StringBuilder sb = new StringBuilder("<table><thead><tr><th>Hand</th>");
        for (int up : UP_ORDER) sb.append("<th>").append(Rank.name(up).replace("T", "10")).append("</th>");
        sb.append("</tr></thead><tbody>");
        return sb.toString();
    }

    private static String tableClose() {
        return "</tbody></table>";
    }

    private static String row(String rowLabel, HandSpec.Kind kind, String specLabel, Map<String, CellResult> byKey) {
        StringBuilder sb = new StringBuilder("<tr><td class='rowlabel'>").append(esc(rowLabel)).append("</td>");
        for (int up : UP_ORDER) {
            CellResult r = byKey.get(key(kind, specLabel, up));
            sb.append(cell(r));
        }
        sb.append("</tr>");
        return sb.toString();
    }

    private static String cell(CellResult r) {
        if (r == null) return "<td class='act-NA'>-</td>";
        Action best = r.best;
        String cls = "act-" + best.name();
        return "<td class='" + cls + "'><div class='action'>" + best.shortCode + "</div></td>";
    }

    private static String surrenderTable(Map<String, CellResult> byKey) {
        StringBuilder sb = new StringBuilder(tableOpen());
        String[][] rows = {{"HARD", "17"}, {"HARD", "16"}, {"HARD", "15"}, {"HARD", "14"}, {"PAIR", "8,8"}};
        for (String[] rr : rows) {
            HandSpec.Kind k = HandSpec.Kind.valueOf(rr[0]);
            StringBuilder line = new StringBuilder("<tr><td class='rowlabel'>").append(esc(rr[1])).append("</td>");
            for (int up : UP_ORDER) {
                CellResult r = byKey.get(key(k, rr[1], up));
                boolean surrenderRelevant = r != null && r.best == Action.SURRENDER;
                line.append(surrenderRelevant ? cell(r) : "<td class='act-NA'></td>");
            }
            line.append("</tr>");
            sb.append(line);
        }
        sb.append(tableClose());
        return sb.toString();
    }

    private static String insuranceBlock(CellResult insurance) {
        if (insurance == null) return "<p>No insurance data.</p>";
        StringBuilder sb = new StringBuilder("<table><tbody><tr><td class='rowlabel'>Insurance / Even Money</td>");
        sb.append(cell(insurance));
        sb.append("</tr></tbody></table>");
        return sb.toString();
    }

    private static String legend() {
        return "<h2>Key</h2><div class='legend'>"
                + "<span class='act-STAND'>S</span> Stand &nbsp; "
                + "<span class='act-HIT'>H</span> Hit &nbsp; "
                + "<span class='act-DOUBLE'>D</span> Double (else Hit) &nbsp; "
                + "<span class='act-SPLIT'>Y</span> Split &nbsp; "
                + "<span class='act-SURRENDER'>SUR</span> Surrender (else next-best) &nbsp; "
                + "<span class='act-TAKE_INSURANCE'>INS</span> Take insurance / even money"
                + "</div>";
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final String CSS = "body{font-family:Arial,Helvetica,sans-serif;background:#1e1e28;color:#eee;padding:20px;}"
            + "h1{color:#2ecc71;} h2{color:#2ecc71;margin-top:30px;border-bottom:1px solid #444;padding-bottom:4px;}"
            + ".rules{font-size:13px;color:#aaa;margin-bottom:20px;}"
            + ".note{font-size:13px;color:#ccc;margin-bottom:20px;}"
            + "table{border-collapse:collapse;margin-bottom:10px;}"
            + "th,td{border:1px solid #444;text-align:center;padding:4px 8px;font-size:13px;min-width:38px;}"
            + "th{background:#333;color:#fff;}"
            + ".rowlabel{background:#333;font-weight:bold;text-align:left;}"
            + ".action{font-weight:bold;font-size:15px;color:#111;}"
            + ".act-STAND{background:#f4d03f;} .act-HIT{background:#fdfefe;} .act-DOUBLE{background:#58d68d;}"
            + ".act-SPLIT{background:#48c9b0;} .act-SURRENDER{background:#af7ac5;}"
            + ".act-TAKE_INSURANCE{background:#58d68d;} .act-DECLINE_INSURANCE{background:#fdfefe;}"
            + ".act-NA{background:#2a2a35;}"
            + ".legend span{display:inline-block;padding:2px 6px;border-radius:3px;color:#111;font-weight:bold;margin-right:4px;}";
}
