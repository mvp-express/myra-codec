package express.mvp.myra.codec.examples.generated.portfolio;

import java.lang.Override;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Auto-generated array-backed writer for Leg.
 *
 * Populate arrays and reuse this instance for repeated fields.
 */
public final class LegArrayWriter implements LegWriter {
    public int count = 0;

    public String[] symbol;

    public MemorySegment symbolScratch;

    public Side[] side;

    public int[] quantity;

    public int count() {
        return this.count;
    }

    @Override
    public void writeTo(LegBuilder builder, int index) {
        Objects.requireNonNull(builder, "builder");
        if (this.symbol != null) {
            builder.setSymbol(this.symbol[index], this.symbolScratch);
        }
        if (this.side != null && this.side[index] != null) {
            builder.setSide(this.side[index]);
        }
        if (this.quantity != null) {
            builder.setQuantity(this.quantity[index]);
        }
    }
}
