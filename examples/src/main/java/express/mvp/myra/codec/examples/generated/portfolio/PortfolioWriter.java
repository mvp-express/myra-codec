package express.mvp.myra.codec.examples.generated.portfolio;

/**
 * Auto-generated writer interface for Portfolio.
 *
 * Provides a reusable, allocation-free way to populate a builder.
 */
public interface PortfolioWriter {
    /**
     * Writes the element at the given index into the provided builder.
     * @param builder target builder
     * @param index element index (0-based)
     */
    void writeTo(PortfolioBuilder builder, int index);

    /**
     * Writes field values into the provided builder instance.
     * @param builder target builder
     */
    default void writeTo(PortfolioBuilder builder) {
        writeTo(builder, 0);
    }
}
