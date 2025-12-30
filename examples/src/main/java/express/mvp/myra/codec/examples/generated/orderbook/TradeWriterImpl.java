package express.mvp.myra.codec.examples.generated.orderbook;

import java.lang.Override;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Auto-generated writer implementation for Trade.
 *
 * Fill fields directly, then pass this instance to a builder.
 */
public final class TradeWriterImpl implements TradeWriter {
    public long priceNanos;

    public int size;

    public String aggressor;

    public MemorySegment aggressorScratch;

    @Override
    public void writeTo(TradeBuilder builder, int index) {
        Objects.requireNonNull(builder, "builder");
        builder.setPriceNanos(this.priceNanos);
        builder.setSize(this.size);
        if (this.aggressor != null) {
            builder.setAggressor(this.aggressor, this.aggressorScratch);
        }
    }
}
