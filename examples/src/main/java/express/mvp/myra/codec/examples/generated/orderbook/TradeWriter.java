package express.mvp.myra.codec.examples.generated.orderbook;

/**
 * Auto-generated writer interface for Trade.
 *
 * Provides a reusable, allocation-free way to populate a builder.
 */
public interface TradeWriter {
    /**
     * Writes the element at the given index into the provided builder.
     * @param builder target builder
     * @param index element index (0-based)
     */
    void writeTo(TradeBuilder builder, int index);

    /**
     * Writes field values into the provided builder instance.
     * @param builder target builder
     */
    default void writeTo(TradeBuilder builder) {
        writeTo(builder, 0);
    }
}
