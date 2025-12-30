package express.mvp.myra.codec.examples.generated.orderbook;

import java.lang.Override;
import java.util.Objects;

/**
 * Auto-generated writer implementation for Level.
 *
 * Fill fields directly, then pass this instance to a builder.
 */
public final class LevelWriterImpl implements LevelWriter {
    public long priceNanos;

    public int size;

    public int orderCount;

    public boolean hasMaker = false;

    public boolean maker;

    @Override
    public void writeTo(LevelBuilder builder, int index) {
        Objects.requireNonNull(builder, "builder");
        builder.setPriceNanos(this.priceNanos);
        builder.setSize(this.size);
        builder.setOrderCount(this.orderCount);
        if (this.hasMaker) {
            builder.setMaker(this.maker);
        }
    }
}
