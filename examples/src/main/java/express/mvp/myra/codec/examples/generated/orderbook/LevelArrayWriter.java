package express.mvp.myra.codec.examples.generated.orderbook;

import java.lang.Override;
import java.util.Objects;

/**
 * Auto-generated array-backed writer for Level.
 *
 * Populate arrays and reuse this instance for repeated fields.
 */
public final class LevelArrayWriter implements LevelWriter {
    public int count = 0;

    public long[] priceNanos;

    public int[] size;

    public int[] orderCount;

    public boolean[] hasMaker;

    public boolean[] maker;

    public int count() {
        return this.count;
    }

    @Override
    public void writeTo(LevelBuilder builder, int index) {
        Objects.requireNonNull(builder, "builder");
        if (this.priceNanos != null) {
            builder.setPriceNanos(this.priceNanos[index]);
        }
        if (this.size != null) {
            builder.setSize(this.size[index]);
        }
        if (this.orderCount != null) {
            builder.setOrderCount(this.orderCount[index]);
        }
        if (this.hasMaker != null && this.hasMaker[index]) {
            builder.setMaker(this.maker[index]);
        }
    }
}
