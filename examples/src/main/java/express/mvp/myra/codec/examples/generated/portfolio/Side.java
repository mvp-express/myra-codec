package express.mvp.myra.codec.examples.generated.portfolio;

import java.lang.SuppressWarnings;

/**
 * Auto-generated enum for Side.
 *
 * Provides type-safe enumeration with stable integer IDs for wire format.
 * Uses O(1) array lookup via {@link #fromId(int)} for high-performance decoding.
 */
public enum Side {
    BUY(0),

    SELL(1);

    private static final Side[] VALUES_BY_ID;

    static {
        VALUES_BY_ID = new Side[2];
        for (Side e : values()) {
            VALUES_BY_ID[e.id] = e;
        }
    }

    private final int id;

    Side(int id) {
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
    public static Side fromId(int id) {
        if (id < 0 || id >= VALUES_BY_ID.length) {
            throw new IllegalArgumentException("Unknown enum id: " + id);
        }
        Side result = VALUES_BY_ID[id];
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
    public static Side fromIdOrNull(int id) {
        if (id < 0 || id >= VALUES_BY_ID.length) {
            return null;
        }
        return VALUES_BY_ID[id];
    }
}
