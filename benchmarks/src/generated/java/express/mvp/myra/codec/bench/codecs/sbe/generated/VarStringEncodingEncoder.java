/* Generated SBE (Simple Binary Encoding) message codec. */
package express.mvp.myra.codec.bench.codecs.sbe.generated;

import org.agrona.MutableDirectBuffer;


/**
 * Variable length UTF-8 string
 */
@SuppressWarnings("all")
public final class VarStringEncodingEncoder
{
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 1;
    public static final String SEMANTIC_VERSION = "1.0.0";
    public static final int ENCODED_LENGTH = -1;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private int offset;
    private MutableDirectBuffer buffer;

    public VarStringEncodingEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;

        return this;
    }

    public MutableDirectBuffer buffer()
    {
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public int encodedLength()
    {
        return ENCODED_LENGTH;
    }

    public int sbeSchemaId()
    {
        return SCHEMA_ID;
    }

    public int sbeSchemaVersion()
    {
        return SCHEMA_VERSION;
    }

    public static int lengthEncodingOffset()
    {
        return 0;
    }

    public static int lengthEncodingLength()
    {
        return 2;
    }

    public static int lengthNullValue()
    {
        return 65535;
    }

    public static int lengthMinValue()
    {
        return 0;
    }

    public static int lengthMaxValue()
    {
        return 65534;
    }

    public VarStringEncodingEncoder length(final int value)
    {
        buffer.putShort(offset + 0, (short)value, BYTE_ORDER);
        return this;
    }


    public static int varDataEncodingOffset()
    {
        return 2;
    }

    public static int varDataEncodingLength()
    {
        return -1;
    }

    public static byte varDataNullValue()
    {
        return (byte)0;
    }

    public static byte varDataMinValue()
    {
        return (byte)32;
    }

    public static byte varDataMaxValue()
    {
        return (byte)126;
    }

    public String toString()
    {
        if (null == buffer)
        {
            return "";
        }

        return appendTo(new StringBuilder()).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        if (null == buffer)
        {
            return builder;
        }

        final VarStringEncodingDecoder decoder = new VarStringEncodingDecoder();
        decoder.wrap(buffer, offset);

        return decoder.appendTo(builder);
    }
}
