package express.mvp.myra.codec.examples.generated.portfolio;

import java.lang.Override;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Auto-generated writer implementation for Leg.
 *
 * Fill fields directly, then pass this instance to a builder.
 */
public final class LegWriterImpl implements LegWriter {
    public String symbol;

    public MemorySegment symbolScratch;

    public Side side;

    public int quantity;

    @Override
    public void writeTo(LegBuilder builder, int index) {
        Objects.requireNonNull(builder, "builder");
        builder.setSymbol(this.symbol, this.symbolScratch);
        builder.setSide(this.side);
        builder.setQuantity(this.quantity);
    }
}
