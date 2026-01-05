package express.mvp.myra.codec.examples.generated.telemetry;

/**
 * Auto-generated writer interface for Telemetry.
 *
 * Provides a reusable, allocation-free way to populate a builder.
 */
public interface TelemetryWriter {
    /**
     * Writes the element at the given index into the provided builder.
     * @param builder target builder
     * @param index element index (0-based)
     */
    void writeTo(TelemetryBuilder builder, int index);

    /**
     * Writes field values into the provided builder instance.
     * @param builder target builder
     */
    default void writeTo(TelemetryBuilder builder) {
        writeTo(builder, 0);
    }
}
