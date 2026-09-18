package blackjack.core;

/**
 * Table rule configuration.
 *
 * Fixed ruleset for this build (as specified):
 *  - 6 decks
 *  - Dealer stands on all 17s (S17), including soft 17
 *  - ENHC (European No Hole Card): dealer's second card is not drawn/checked until after all
 *    player action is complete. If the dealer ends up with a natural blackjack, the player loses
 *    the FULL amount wagered on that hand (including doubled and split amounts) -- there is no
 *    "original bets only" (OBO) protection. This falls straight out of the payoff comparison logic
 *    (a dealer natural is simply an unbeatable dealer total), so no special-casing is needed beyond
 *    resolving it as a normal loss of the current stake.
 *  - Double after split (DAS) allowed
 *  - Split up to 4 hands (3 resplits) for non-ace pairs
 *  - Aces: split once only (2 hands total), no resplitting even on a third ace, and each ace hand
 *    receives exactly one additional card with no further hitting/doubling.
 *  - Early surrender available against any dealer upcard except Ace, decided before any other
 *    action and before the dealer's hand is resolved at all -- so surrender EV is a flat -0.5
 *    independent of composition (unlike late surrender).
 *  - Blackjack pays 3:2.
 */
public final class Rules {

    public final int decks;
    public final boolean das = true;
    public final int maxSplitHands = 4;
    public final boolean resplitAces = false;
    public final boolean hitSplitAces = false;
    public final double blackjackPayout = 1.5;
    public final boolean earlySurrenderVsAce = false; // surrender NOT offered vs Ace
    public final double penetration; // fraction of shoe dealt before reshuffle, for simulation

    public Rules(int decks, double penetration) {
        this.decks = decks;
        this.penetration = penetration;
    }

    public static Rules sixDeckDefault() {
        return new Rules(6, 0.75);
    }
}
