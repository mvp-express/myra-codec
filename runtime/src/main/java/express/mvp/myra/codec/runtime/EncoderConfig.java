package express.mvp.myra.codec.runtime;

/**
 * Configuration options for the Myra message encoder.
 *
 * <p>This class provides immutable configuration that controls encoding behavior. Use the {@link
 * Builder} to create instances with custom settings.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * EncoderConfig config = EncoderConfig.builder()
 *     .checksumEnabled(false)  // Disable for maximum throughput
 *     .build();
 *
 * MessageEncoder encoder = new MessageEncoder(pool, config);
 * }</pre>
 *
 * <p><b>Performance note:</b> The runtime cost of checking the {@code checksumEnabled} flag is
 * negligible (~1 CPU cycle for a boolean field access). The JIT compiler typically inlines these
 * checks. The real performance gain comes from skipping the CRC32 computation itself.
 *
 * <h2>Thread Safety</h2>
 *
 * <p><b>This class is immutable and thread-safe.</b> A single {@code EncoderConfig} instance can be
 * safely shared across multiple threads and {@link MessageEncoder} instances. The pre-defined
 * constants {@link #DEFAULT} and {@link #HIGH_PERFORMANCE} are designed for sharing.
 */
public final class EncoderConfig {

    /** Default configuration with checksum enabled. */
    public static final EncoderConfig DEFAULT = new EncoderConfig(true);

    /** High-performance configuration with checksum disabled. */
    public static final EncoderConfig HIGH_PERFORMANCE = new EncoderConfig(false);

    private final boolean checksumEnabled;

    private EncoderConfig(boolean checksumEnabled) {
        this.checksumEnabled = checksumEnabled;
    }

    /**
     * Returns whether CRC32 checksum computation is enabled.
     *
     * <p>When enabled, the encoder computes a CRC32 checksum over the message payload and stores it
     * in the message header. This provides data integrity verification but adds ~10-20% encoding
     * overhead.
     *
     * <p>When disabled, the checksum field in the header is set to 0, and no CRC32 computation is
     * performed. Use this for maximum throughput when integrity is verified by other means (e.g.,
     * transport-layer checksums).
     *
     * @return true if checksum computation is enabled, false otherwise.
     */
    public boolean isChecksumEnabled() {
        return checksumEnabled;
    }

    /**
     * Creates a new builder for constructing an {@link EncoderConfig}.
     *
     * @return a new Builder instance with default settings.
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "EncoderConfig{checksumEnabled=" + checksumEnabled + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EncoderConfig other)) {
            return false;
        }
        return checksumEnabled == other.checksumEnabled;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(checksumEnabled);
    }

    /**
     * Builder for creating {@link EncoderConfig} instances.
     *
     * <p>By default, checksum is enabled. Call {@link #checksumEnabled(boolean)} to change this.
     */
    public static final class Builder {

        private boolean checksumEnabled = true;

        private Builder() {}

        /**
         * Sets whether CRC32 checksum computation should be enabled.
         *
         * @param enabled true to enable checksum (default), false to disable for maximum
         *     throughput.
         * @return this builder for chaining.
         */
        public Builder checksumEnabled(boolean enabled) {
            this.checksumEnabled = enabled;
            return this;
        }

        /**
         * Builds an immutable {@link EncoderConfig} with the configured settings.
         *
         * @return a new EncoderConfig instance.
         */
        public EncoderConfig build() {
            return new EncoderConfig(checksumEnabled);
        }
    }
}
