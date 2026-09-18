package blackjack.core;

/**
 * Immutable snapshot of how many cards of each rank remain in the shoe.
 * Index convention matches {@link Rank}.
 */
public final class Composition {

    private final int[] counts; // length 10
    private final int total;

    public Composition(int[] counts) {
        this.counts = counts;
        int t = 0;
        for (int c : counts) t += c;
        this.total = t;
    }

    public static Composition freshShoe(int decks) {
        int[] c = new int[Rank.COUNT];
        for (int i = 0; i < Rank.COUNT; i++) c[i] = Rank.PER_DECK[i] * decks;
        return new Composition(c);
    }

    public int count(int rank) {
        return counts[rank];
    }

    public int total() {
        return total;
    }

    public double prob(int rank) {
        return total == 0 ? 0.0 : ((double) counts[rank]) / total;
    }

    /** Returns a new composition with one card of the given rank removed. Caller must ensure count(rank) > 0. */
    public Composition remove(int rank) {
        int[] nc = counts.clone();
        nc[rank]--;
        return new Composition(nc);
    }

    /** Returns a new composition with one card of the given rank removed, or this composition unchanged if none remain. */
    public Composition removeIfPresent(int rank) {
        if (counts[rank] <= 0) return this;
        return remove(rank);
    }

    public boolean has(int rank) {
        return counts[rank] > 0;
    }

    public int[] rawCounts() {
        return counts.clone();
    }

    /**
     * Packs the composition into a single long for use as a hash-map key in memoized recursion.
     * Ranks 0..8 get 5 bits each (max 31), rank 9 (tens) gets 7 bits (max 127) -- 52 bits total,
     * safe for shoes up to 7 decks (this app uses 6). Callers that further combine this key with a
     * small hand-state value (see DealerCalculator/PlayerEV) rely on it fitting in <= 52 bits.
     */
    public long key() {
        long k = 0L;
        for (int i = 0; i < 9; i++) {
            k = (k << 5) | (counts[i] & 0x1F);
        }
        k = (k << 7) | (counts[9] & 0x7F);
        return k;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Rank.COUNT; i++) {
            if (i > 0) sb.append(',');
            sb.append(Rank.name(i)).append('=').append(counts[i]);
        }
        return sb.append(']').toString();
    }
}
