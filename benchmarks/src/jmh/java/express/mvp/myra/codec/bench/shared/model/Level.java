package express.mvp.myra.codec.bench.shared.model;

import java.util.Objects;

/**
 * Quote-level representation shared across benchmark codecs.
 */
public final class Level {
    private long priceNanos;
    private int size;
    private int orderCount;
    private Boolean maker;

    public Level() {
        // Required for serialization frameworks such as Kryo.
    }

    public Level(long priceNanos, int size, int orderCount, Boolean maker) {
        this.priceNanos = priceNanos;
        this.size = size;
        this.orderCount = orderCount;
        this.maker = maker;
    }

    public long priceNanos() {
        return priceNanos;
    }

    public int size() {
        return size;
    }

    public int orderCount() {
        return orderCount;
    }

    public Boolean maker() {
        return maker;
    }

    @Override
    public String toString() {
        return "Level{" +
                "priceNanos=" + priceNanos +
                ", size=" + size +
                ", orderCount=" + orderCount +
                ", maker=" + maker +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(priceNanos, size, orderCount, maker);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Level other)) {
            return false;
        }
        return priceNanos == other.priceNanos
                && size == other.size
                && orderCount == other.orderCount
                && Objects.equals(maker, other.maker);
    }
}
