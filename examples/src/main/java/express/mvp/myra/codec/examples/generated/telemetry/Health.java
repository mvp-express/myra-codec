package express.mvp.myra.codec.examples.generated.telemetry;

import java.lang.SuppressWarnings;

/**
 * Auto-generated enum for Health.
 *
 * Provides type-safe enumeration with stable integer IDs for wire format.
 * Uses O(1) array lookup via {@link #fromId(int)} for high-performance decoding.
 */
public enum Health {
    OK(0),

    WARN(1),

    FAIL(2);

    private static final Health[] VALUES_BY_ID;

    static {
        VALUES_BY_ID = new Health[3];
        for (Health e : values()) {
            VALUES_BY_ID[e.id] = e;
        }
    }

    private final int id;

    Health(int id) {
        this.id = id;
    }

    /**
     * Returns the wire-format integer ID for this enum value.
     * @return the numeric ID
     */
    public int id() {
        return this.id;
    }

    /**
     * Returns the enum constant for the given wire-format ID.
     * Uses O(1) array lookup for high-performance decoding.
     *
     * @param id the wire-format integer ID
     * @return the corresponding enum constant
     * @throws IllegalArgumentException if id is out of range or unknown
     */
    public static Health fromId(int id) {
        if (id < 0 || id >= VALUES_BY_ID.length) {
            throw new IllegalArgumentException("Unknown enum id: " + id);
        }
        Health result = VALUES_BY_ID[id];
        if (result == null) {
            throw new IllegalArgumentException("Unknown enum id: " + id);
        }
        return result;
    }

    /**
     * Returns the enum constant for the given wire-format ID, or null if unknown.
     * Uses O(1) array lookup for high-performance decoding.
     *
     * @param id the wire-format integer ID
     * @return the corresponding enum constant, or null if id is out of range or unknown
     */
    @SuppressWarnings("unused")
    public static Health fromIdOrNull(int id) {
        if (id < 0 || id >= VALUES_BY_ID.length) {
            return null;
        }
        return VALUES_BY_ID[id];
    }
}
