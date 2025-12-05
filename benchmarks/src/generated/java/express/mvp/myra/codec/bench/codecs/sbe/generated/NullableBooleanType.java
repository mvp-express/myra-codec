/* Generated SBE (Simple Binary Encoding) message codec. */
package express.mvp.myra.codec.bench.codecs.sbe.generated;

@SuppressWarnings("all")
public enum NullableBooleanType
{
    NULL_VALUE((byte)-1),

    FALSE((byte)0),

    TRUE((byte)1),

    /**
     * To be used to represent not present or null.
     */
    NULL_VAL((byte)-128);

    private final byte value;

    NullableBooleanType(final byte value)
    {
        this.value = value;
    }

    /**
     * The raw encoded value in the Java type representation.
     *
     * @return the raw value encoded.
     */
    public byte value()
    {
        return value;
    }

    /**
     * Lookup the enum value representing the value.
     *
     * @param value encoded to be looked up.
     * @return the enum value representing the value.
     */
    public static NullableBooleanType get(final byte value)
    {
        switch (value)
        {
            case -1: return NULL_VALUE;
            case 0: return FALSE;
            case 1: return TRUE;
            case -128: return NULL_VAL;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
