package blackjack.util;

import java.util.Arrays;

/** Open-addressing long -> Object hash map (see LongDoubleMap for rationale). Empty key sentinel is Long.MIN_VALUE. */
public final class LongObjectMap<V> {
    private static final long EMPTY = Long.MIN_VALUE;

    private long[] keys;
    private Object[] values;
    private int mask;
    private int size;

    public LongObjectMap(int expectedEntries) {
        int cap = Integer.highestOneBit(Math.max(16, expectedEntries * 2 - 1)) * 2;
        keys = new long[cap];
        Arrays.fill(keys, EMPTY);
        values = new Object[cap];
        mask = cap - 1;
    }

    @SuppressWarnings("unchecked")
    public V get(long key) {
        int idx = index(key);
        while (true) {
            long k = keys[idx];
            if (k == EMPTY) return null;
            if (k == key) return (V) values[idx];
            idx = (idx + 1) & mask;
        }
    }

    public void put(long key, V value) {
        if ((size + 1) * 10L >= keys.length * 7L) grow();
        int idx = index(key);
        while (true) {
            long k = keys[idx];
            if (k == EMPTY) {
                keys[idx] = key;
                values[idx] = value;
                size++;
                return;
            }
            if (k == key) {
                values[idx] = value;
                return;
            }
            idx = (idx + 1) & mask;
        }
    }

    private int index(long key) {
        long h = key * 0x9E3779B97F4A7C15L;
        h ^= (h >>> 32);
        return (int) (h & mask);
    }

    @SuppressWarnings("unchecked")
    private void grow() {
        long[] oldKeys = keys;
        Object[] oldValues = values;
        int newCap = keys.length * 2;
        keys = new long[newCap];
        Arrays.fill(keys, EMPTY);
        values = new Object[newCap];
        mask = newCap - 1;
        size = 0;
        for (int i = 0; i < oldKeys.length; i++) {
            if (oldKeys[i] != EMPTY) put(oldKeys[i], (V) oldValues[i]);
        }
    }
}
