package express.mvp.myra.codec.bench.shared.model;

import java.util.Objects;

public final class Trade {
    private long priceNanos;
    private int size;
    private String aggressor;

    public Trade() {
    }

    public Trade(long priceNanos, int size, String aggressor) {
        this.priceNanos = priceNanos;
        this.size = size;
        this.aggressor = aggressor;
    }

    public long priceNanos() {
        return priceNanos;
    }

    public int size() {
        return size;
    }

    public String aggressor() {
        return aggressor;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "priceNanos=" + priceNanos +
                ", size=" + size +
                ", aggressor='" + aggressor + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(priceNanos, size, aggressor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Trade other)) {
            return false;
        }
        return priceNanos == other.priceNanos
                && size == other.size
                && Objects.equals(aggressor, other.aggressor);
    }
}
