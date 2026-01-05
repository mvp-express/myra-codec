package express.mvp.myra.codec.examples.generated.portfolio;

/**
 * Auto-generated writer interface for Leg.
 *
 * Provides a reusable, allocation-free way to populate a builder.
 */
public interface LegWriter {
    /**
     * Writes the element at the given index into the provided builder.
     * @param builder target builder
     * @param index element index (0-based)
     */
    void writeTo(LegBuilder builder, int index);

    /**
     * Writes field values into the provided builder instance.
     * @param builder target builder
     */
    default void writeTo(LegBuilder builder) {
        writeTo(builder, 0);
    }
}
