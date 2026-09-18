package blackjack.util;

import java.util.Arrays;

/**
 * Open-addressing long -> primitive double hash map, used as the memoization cache in the hot
 * recursive EV paths. Avoids the Long/Double autoboxing and per-entry Node allocation that
 * java.util.HashMap<Long,Double> would incur, which matters a lot here since these caches can see
 * tens of thousands of put/get calls per single top-level EV computation.
 *
 * Absence is represented by Double.NaN, which is never a legitimate EV value. The key sentinel for
 * an empty slot is Long.MIN_VALUE, which never collides with a real composition/state key (those
 * are always non-negative bit-packed values).
 */
public final class LongDoubleMap {
    private static final long EMPTY = Long.MIN_VALUE;

    private long[] keys;
    private double[] values;
    private int mask;
    private int size;

    public LongDoubleMap(int expectedEntries) {
        int cap = Integer.highestOneBit(Math.max(16, expectedEntries * 2 - 1)) * 2;
        keys = new long[cap];
        Arrays.fill(keys, EMPTY);
        values = new double[cap];
        mask = cap - 1;
    }

    /** Returns Double.NaN if absent. */
    public double get(long key) {
        int idx = index(key);
        while (true) {
            long k = keys[idx];
            if (k == EMPTY) return Double.NaN;
            if (k == key) return values[idx];
            idx = (idx + 1) & mask;
        }
    }

    public void put(long key, double value) {
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

    private void grow() {
        long[] oldKeys = keys;
        double[] oldValues = values;
        int newCap = keys.length * 2;
        keys = new long[newCap];
        Arrays.fill(keys, EMPTY);
        values = new double[newCap];
        mask = newCap - 1;
        size = 0;
        for (int i = 0; i < oldKeys.length; i++) {
            if (oldKeys[i] != EMPTY) put(oldKeys[i], oldValues[i]);
        }
    }
}
