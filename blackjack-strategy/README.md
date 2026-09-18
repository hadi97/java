# Blackjack Strategy + Deviation Chart Generator

A from-scratch Java engine that computes exact basic strategy and Hi-Lo true-count deviation
indices for a specific rule set, rather than reproducing a published chart. It does this with:

1. An **exact recursive combinatorial EV engine** (no infinite-deck shortcuts, no Effect-of-Removal
   approximation) that computes the dealer's exact final-outcome probability distribution and the
   player's exact stand/hit/double/split/surrender EVs for any given shoe composition.
2. A **Monte Carlo, count-conditioned simulation** that shuffles and deals many six-deck shoes,
   tracks the Hi-Lo running/true count as it goes, and at each shoe position feeds the *actual*
   remaining composition into the exact engine above to find where the optimal play changes as the
   true count moves away from 0.

## Rules implemented

- 6 decks (configurable via `--decks`)
- Dealer stands on all 17s (S17), including soft 17
- ENHC (European No Hole Card): the dealer's second card is only drawn after all player action is
  complete. If the dealer ends up with a natural blackjack, the player loses the **full** amount
  wagered on that hand, including doubled and split amounts (no "original bets only" protection) —
  this is handled automatically by the payoff comparison logic, since a dealer natural is just an
  unbeatable dealer total.
- Double after split (DAS) allowed
- Split up to 4 hands (3 resplits) for non-ace pairs
- Aces: split once only (2 hands total), no resplitting, and each ace hand receives exactly one
  additional card with no further hitting or doubling
- Early surrender against any dealer upcard except Ace, decided before any other action
- Blackjack pays 3:2
- Hi-Lo running count (2-6 = +1, 7-9 = 0, 10/A = -1); true count = running count / decks remaining
  (continuous, not rounded to whole decks)

## Methodology notes / deliberate approximations

- **Basic-strategy baseline (true count 0) composition convention**: for pairs and soft totals
  there's exactly one card combination per hand, so the composition is unambiguous. For hard totals
  (7-17), multiple two-card combinations sum to the same total (e.g. 16 = 9+7 or T+6); the baseline
  generator evaluates *every* valid combination and combines them into a single
  composition-weighted average per action (weighted by how often each combination occurs in a
  fresh shoe). The deviation simulator does the equivalent thing implicitly by sampling whichever
  combination the current shoe composition favors, so the true-count-0 slice of the simulated data
  is consistent with the static baseline.
- **Split-hand independence approximation**: sibling hands from a split are each evaluated starting
  from the *same* remaining composition rather than from a composition sequentially depleted by
  every other hand's actual draws. This is the standard method used by essentially all published
  composition-dependent strategy/deviation charts (full joint dependency between sibling hands'
  draws is combinatorially intractable at this scale), and the resulting error is negligible (a
  handful of cards out of a 300+ card shoe) relative to the EV magnitudes involved.
- **True-count sampling**: rather than simulating explicit multi-player hand structure, the
  simulator shuffles a fresh shoe and reveals cards one at a time, tracking the running count. Since
  the combinatorial EV engine only cares about what's left to draw (not card order), the remaining
  composition after each reveal is an unbiased sample of "what a shoe looks like at this true
  count," and each sampled position is evaluated directly against a round-robin-selected chart
  cell. This avoids needing to model table size, number of players, etc., which don't affect the
  composition-vs-count relationship.
- Engine correctness was cross-checked against well-known published EVs/strategy for 6-deck S17 DAS
  games (e.g. hard 20 vs 6 EV ≈ +0.70, dealer bust% vs a T upcard ≈ 21%, dealer blackjack probability
  vs T upcard ≈ 24/311 exactly, 8-8 vs 6 split EV ≈ +0.40, hard 16 vs T EVs matching published
  values to 3-4 decimal places) before trusting the deviation output. A handful of cells
  legitimately differ from generic published S17/DAS charts because of this table's specific ENHC +
  early-surrender rules (which change double/split EVs against dealer Ace/Ten, since a peek game
  never lets you double/split into a dealer blackjack in the first place) — that's expected, not a
  bug.

## Performance

The exact engine is expensive for hands that can resplit deeply and then hit each resulting hand
(low pairs like 2,2-7,7 are the worst case, since with DAS + resplitting to 4 hands there's a large
reachable state space). Rough per-sample cost on this development machine (4 cores): hard totals
~0.2-2ms, soft totals ~5-10ms, low pairs ~100-300ms, aces/tens-pairs ~3-5ms. Runtime scales roughly
linearly with `--rounds` and inversely with thread/core count.

As a rough guide (your mileage will vary by CPU):

| `--rounds`   | ~samples/cell | Rough time on 4 cores | Rough time on 16+ cores |
|--------------|---------------|------------------------|--------------------------|
| 300,000      | ~1,000        | a few minutes           | under a minute           |
| 3,000,000    | ~10,000       | ~30-60 minutes          | a few minutes            |
| 30,000,000   | ~100,000      | many hours              | ~30-60 minutes           |
| 300,000,000  | ~1,000,000    | multiple days           | several hours            |

Start small (`--rounds=300000`) to sanity-check the output quickly, then scale up for tighter,
more stable indices. `DeviationAnalyzer.MIN_SAMPLES_PER_BUCKET` (currently 30) is the minimum
samples a true-count bucket needs before it's used to detect a threshold — with very low
`--rounds`, sparse buckets near the extremes of the count range will just be skipped rather than
reported.

## Build & run

Requires a JDK (17+ recommended). No external dependencies -- plain `javac`/`java`.

```bash
# from blackjack-strategy/
javac -d out $(find src -name "*.java")

java -cp out blackjack.Main --rounds=3000000 --out=output
```

Options:

```
--rounds=N          total Monte Carlo samples across all cells (default 3000000)
--threads=N         worker threads (default = CPU core count)
--decks=N           number of decks (default 6)
--penetration=P     fraction of shoe dealt before reshuffle, 0-1 (default 0.75)
--out=DIR           output directory (default 'output')
--csv-only          skip HTML output
--html-only         skip CSV output
--skip-basic        don't generate basic_strategy.{html,csv}
--skip-deviations   don't run the Monte Carlo simulation / generate deviations.{html,csv}
```

Output -- two independent pairs of files:

- `output/basic_strategy.html` / `.csv` -- the exact true-count-0 basic strategy (no threshold
  data at all), in the same section layout as a typical strategy chart (pair splitting, soft
  totals, hard totals, surrender, insurance). This is computed exactly (not simulated) and is
  written almost instantly, regardless of `--rounds`.
- `output/deviations.html` / `.csv` -- the true-count deviation thresholds from the Monte Carlo
  run: the baseline (true-count-0) action per cell plus any true-count(s) where the optimal play
  changes. This is the file that takes a while to compute and scales with `--rounds` (see the
  table above). Use `--skip-deviations` if you only want the instant basic strategy chart, or
  `--skip-basic` to skip re-generating it on a later run.

## Code layout

```
blackjack.core       Rules, card/composition representation, hand-total bookkeeping
blackjack.util       Boxing-free primitive long-keyed hash maps used as memoization caches
blackjack.calc       The exact combinatorial engine: dealer outcome distribution, player stand/
                      hit/double EV, split EV, insurance EV
blackjack.strategy   HandSpec/Action/CellResult model, the cell evaluator, and the true-count-0
                      basic strategy generator
blackjack.deviation  The Monte Carlo shoe simulator, true-count bucketing, and the analyzer that
                      turns accumulated data into per-cell deviation thresholds
blackjack.output     HTML and CSV renderers
blackjack.Main       CLI entry point
```
