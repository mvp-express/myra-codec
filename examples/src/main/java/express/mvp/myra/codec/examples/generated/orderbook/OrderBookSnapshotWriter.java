package express.mvp.myra.codec.examples.generated.orderbook;

/**
 * Auto-generated writer interface for OrderBookSnapshot.
 *
 * Provides a reusable, allocation-free way to populate a builder.
 */
public interface OrderBookSnapshotWriter {
    /**
     * Writes the element at the given index into the provided builder.
     * @param builder target builder
     * @param index element index (0-based)
     */
    void writeTo(OrderBookSnapshotBuilder builder, int index);

    /**
     * Writes field values into the provided builder instance.
     * @param builder target builder
     */
    default void writeTo(OrderBookSnapshotBuilder builder) {
        writeTo(builder, 0);
    }
}
