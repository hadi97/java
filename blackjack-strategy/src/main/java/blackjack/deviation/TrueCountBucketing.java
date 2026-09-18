package blackjack.deviation;

public final class TrueCountBucketing {
    public static final double MIN_TC = -15.0;
    public static final double MAX_TC = 15.0;
    public static final double STEP = 0.5;
    public static final int BUCKETS = (int) Math.round((MAX_TC - MIN_TC) / STEP) + 1;

    private TrueCountBucketing() {}

    public static int bucketOf(double trueCount) {
        double clamped = Math.max(MIN_TC, Math.min(MAX_TC, trueCount));
        return (int) Math.round((clamped - MIN_TC) / STEP);
    }

    public static double trueCountOf(int bucket) {
        return MIN_TC + bucket * STEP;
    }

    public static int zeroBucket() {
        return bucketOf(0.0);
    }
}
