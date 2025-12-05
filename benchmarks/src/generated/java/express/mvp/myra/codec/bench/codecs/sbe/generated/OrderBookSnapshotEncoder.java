/* Generated SBE (Simple Binary Encoding) message codec. */
package express.mvp.myra.codec.bench.codecs.sbe.generated;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;

@SuppressWarnings("all")
public final class OrderBookSnapshotEncoder
{
    public static final int BLOCK_LENGTH = 28;
    public static final int TEMPLATE_ID = 4001;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 1;
    public static final String SEMANTIC_VERSION = "1.0.0";
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final OrderBookSnapshotEncoder parentMessage = this;
    private MutableDirectBuffer buffer;
    private int offset;
    private int limit;

    public int sbeBlockLength()
    {
        return BLOCK_LENGTH;
    }

    public int sbeTemplateId()
    {
        return TEMPLATE_ID;
    }

    public int sbeSchemaId()
    {
        return SCHEMA_ID;
    }

    public int sbeSchemaVersion()
    {
        return SCHEMA_VERSION;
    }

    public String sbeSemanticType()
    {
        return "";
    }

    public MutableDirectBuffer buffer()
    {
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public OrderBookSnapshotEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);

        return this;
    }

    public OrderBookSnapshotEncoder wrapAndApplyHeader(
        final MutableDirectBuffer buffer, final int offset, final MessageHeaderEncoder headerEncoder)
    {
        headerEncoder
            .wrap(buffer, offset)
            .blockLength(BLOCK_LENGTH)
            .templateId(TEMPLATE_ID)
            .schemaId(SCHEMA_ID)
            .version(SCHEMA_VERSION);

        return wrap(buffer, offset + MessageHeaderEncoder.ENCODED_LENGTH);
    }

    public int encodedLength()
    {
        return limit - offset;
    }

    public int limit()
    {
        return limit;
    }

    public void limit(final int limit)
    {
        this.limit = limit;
    }

    public static int instrumentIdId()
    {
        return 1;
    }

    public static int instrumentIdSinceVersion()
    {
        return 0;
    }

    public static int instrumentIdEncodingOffset()
    {
        return 0;
    }

    public static int instrumentIdEncodingLength()
    {
        return 4;
    }

    public static String instrumentIdMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static int instrumentIdNullValue()
    {
        return -2147483648;
    }

    public static int instrumentIdMinValue()
    {
        return -2147483647;
    }

    public static int instrumentIdMaxValue()
    {
        return 2147483647;
    }

    public OrderBookSnapshotEncoder instrumentId(final int value)
    {
        buffer.putInt(offset + 0, value, BYTE_ORDER);
        return this;
    }


    public static int sequenceId()
    {
        return 2;
    }

    public static int sequenceSinceVersion()
    {
        return 0;
    }

    public static int sequenceEncodingOffset()
    {
        return 4;
    }

    public static int sequenceEncodingLength()
    {
        return 8;
    }

    public static String sequenceMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static long sequenceNullValue()
    {
        return -9223372036854775808L;
    }

    public static long sequenceMinValue()
    {
        return -9223372036854775807L;
    }

    public static long sequenceMaxValue()
    {
        return 9223372036854775807L;
    }

    public OrderBookSnapshotEncoder sequence(final long value)
    {
        buffer.putLong(offset + 4, value, BYTE_ORDER);
        return this;
    }


    public static int isTradingId()
    {
        return 3;
    }

    public static int isTradingSinceVersion()
    {
        return 0;
    }

    public static int isTradingEncodingOffset()
    {
        return 12;
    }

    public static int isTradingEncodingLength()
    {
        return 1;
    }

    public static String isTradingMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public OrderBookSnapshotEncoder isTrading(final BooleanType value)
    {
        buffer.putByte(offset + 12, (byte)value.value());
        return this;
    }

    public static int hasTradingStatusId()
    {
        return 4;
    }

    public static int hasTradingStatusSinceVersion()
    {
        return 0;
    }

    public static int hasTradingStatusEncodingOffset()
    {
        return 13;
    }

    public static int hasTradingStatusEncodingLength()
    {
        return 1;
    }

    public static String hasTradingStatusMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public OrderBookSnapshotEncoder hasTradingStatus(final BooleanType value)
    {
        buffer.putByte(offset + 13, (byte)value.value());
        return this;
    }

    public static int hasLastTradeId()
    {
        return 5;
    }

    public static int hasLastTradeSinceVersion()
    {
        return 0;
    }

    public static int hasLastTradeEncodingOffset()
    {
        return 14;
    }

    public static int hasLastTradeEncodingLength()
    {
        return 1;
    }

    public static String hasLastTradeMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public OrderBookSnapshotEncoder hasLastTrade(final BooleanType value)
    {
        buffer.putByte(offset + 14, (byte)value.value());
        return this;
    }

    public static int lastTradePriceId()
    {
        return 6;
    }

    public static int lastTradePriceSinceVersion()
    {
        return 0;
    }

    public static int lastTradePriceEncodingOffset()
    {
        return 15;
    }

    public static int lastTradePriceEncodingLength()
    {
        return 8;
    }

    public static String lastTradePriceMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "optional";
        }

        return "";
    }

    public static long lastTradePriceNullValue()
    {
        return -9223372036854775808L;
    }

    public static long lastTradePriceMinValue()
    {
        return -9223372036854775807L;
    }

    public static long lastTradePriceMaxValue()
    {
        return 9223372036854775807L;
    }

    public OrderBookSnapshotEncoder lastTradePrice(final long value)
    {
        buffer.putLong(offset + 15, value, BYTE_ORDER);
        return this;
    }


    public static int lastTradeSizeId()
    {
        return 7;
    }

    public static int lastTradeSizeSinceVersion()
    {
        return 0;
    }

    public static int lastTradeSizeEncodingOffset()
    {
        return 23;
    }

    public static int lastTradeSizeEncodingLength()
    {
        return 4;
    }

    public static String lastTradeSizeMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "optional";
        }

        return "";
    }

    public static int lastTradeSizeNullValue()
    {
        return -2147483648;
    }

    public static int lastTradeSizeMinValue()
    {
        return -2147483647;
    }

    public static int lastTradeSizeMaxValue()
    {
        return 2147483647;
    }

    public OrderBookSnapshotEncoder lastTradeSize(final int value)
    {
        buffer.putInt(offset + 23, value, BYTE_ORDER);
        return this;
    }


    public static int hasLastTradeAggressorId()
    {
        return 8;
    }

    public static int hasLastTradeAggressorSinceVersion()
    {
        return 0;
    }

    public static int hasLastTradeAggressorEncodingOffset()
    {
        return 27;
    }

    public static int hasLastTradeAggressorEncodingLength()
    {
        return 1;
    }

    public static String hasLastTradeAggressorMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public OrderBookSnapshotEncoder hasLastTradeAggressor(final BooleanType value)
    {
        buffer.putByte(offset + 27, (byte)value.value());
        return this;
    }

    private final BidsEncoder bids = new BidsEncoder(this);

    public static long bidsId()
    {
        return 20;
    }

    public BidsEncoder bidsCount(final int count)
    {
        bids.wrap(buffer, count);
        return bids;
    }

    public static final class BidsEncoder
    {
        public static final int HEADER_SIZE = 4;
        private final OrderBookSnapshotEncoder parentMessage;
        private MutableDirectBuffer buffer;
        private int count;
        private int index;
        private int offset;
        private int initialLimit;

        BidsEncoder(final OrderBookSnapshotEncoder parentMessage)
        {
            this.parentMessage = parentMessage;
        }

        public void wrap(final MutableDirectBuffer buffer, final int count)
        {
            if (count < 0 || count > 65534)
            {
                throw new IllegalArgumentException("count outside allowed range: count=" + count);
            }

            if (buffer != this.buffer)
            {
                this.buffer = buffer;
            }

            index = 0;
            this.count = count;
            final int limit = parentMessage.limit();
            initialLimit = limit;
            parentMessage.limit(limit + HEADER_SIZE);
            buffer.putShort(limit + 0, (short)17, BYTE_ORDER);
            buffer.putShort(limit + 2, (short)count, BYTE_ORDER);
        }

        public BidsEncoder next()
        {
            if (index >= count)
            {
                throw new java.util.NoSuchElementException();
            }

            offset = parentMessage.limit();
            parentMessage.limit(offset + sbeBlockLength());
            ++index;

            return this;
        }

        public int resetCountToIndex()
        {
            count = index;
            buffer.putShort(initialLimit + 2, (short)count, BYTE_ORDER);

            return count;
        }

        public static int countMinValue()
        {
            return 0;
        }

        public static int countMaxValue()
        {
            return 65534;
        }

        public static int sbeHeaderSize()
        {
            return HEADER_SIZE;
        }

        public static int sbeBlockLength()
        {
            return 17;
        }

        public static int priceNanosId()
        {
            return 1;
        }

        public static int priceNanosSinceVersion()
        {
            return 0;
        }

        public static int priceNanosEncodingOffset()
        {
            return 0;
        }

        public static int priceNanosEncodingLength()
        {
            return 8;
        }

        public static String priceNanosMetaAttribute(final MetaAttribute metaAttribute)
        {
            if (MetaAttribute.PRESENCE == metaAttribute)
            {
                return "required";
            }

            return "";
        }

        public static long priceNanosNullValue()
        {
            return -9223372036854775808L;
        }

        public static long priceNanosMinValue()
        {
            return -9223372036854775807L;
        }

        public static long priceNanosMaxValue()
        {
            return 9223372036854775807L;
        }

        public BidsEncoder priceNanos(final long value)
        {
            buffer.putLong(offset + 0, value, BYTE_ORDER);
            return this;
        }


        public static int sizeId()
        {
            return 2;
        }

        public static int sizeSinceVersion()
        {
            return 0;
        }

        public static int sizeEncodingOffset()
        {
            return 8;
        }

        public static int sizeEncodingLength()
        {
            return 4;
        }

        public static String sizeMetaAttribute(final MetaAttribute metaAttribute)
        {
            if (MetaAttribute.PRESENCE == metaAttribute)
            {
                return "required";
            }

            return "";
        }

        public static int sizeNullValue()
        {
            return -2147483648;
        }

        public static int sizeMinValue()
        {
            return -2147483647;
        }

        public static int sizeMaxValue()
        {
            return 2147483647;
        }

        public BidsEncoder size(final int value)
        {
            buffer.putInt(offset + 8, value, BYTE_ORDER);
            return this;
        }


        public static int orderCountId()
        {
            return 3;
        }

        public static int orderCountSinceVersion()
        {
            return 0;
        }

        public static int orderCountEncodingOffset()
        {
            return 12;
        }

        public static int orderCountEncodingLength()
        {
            return 4;
        }

        public static String orderCountMetaAttribute(final MetaAttribute metaAttribute)
        {
            if (MetaAttribute.PRESENCE == metaAttribute)
            {
                return "required";
            }

            return "";
        }

        public static int orderCountNullValue()
        {
            return -2147483648;
        }

        public static int orderCountMinValue()
        {
            return -2147483647;
        }

        public static int orderCountMaxValue()
        {
            return 2147483647;
        }

        public BidsEncoder orderCount(final int value)
        {
            buffer.putInt(offset + 12, value, BYTE_ORDER);
            return this;
        }


        public static int makerId()
        {
            return 4;
        }

        public static int makerSinceVersion()
        {
            return 0;
        }

        public static int makerEncodingOffset()
        {
            return 16;
        }

        public static int makerEncodingLength()
        {
            return 1;
        }

        public static String makerMetaAttribute(final MetaAttribute metaAttribute)
        {
            if (MetaAttribute.PRESENCE == metaAttribute)
            {
                return "required";
            }

            return "";
        }

        public BidsEncoder maker(final NullableBooleanType value)
        {
            buffer.putByte(offset + 16, value.value());
            return this;
        }
    }

    private final AsksEncoder asks = new AsksEncoder(this);

    public static long asksId()
    {
        return 30;
    }

    public AsksEncoder asksCount(final int count)
    {
        asks.wrap(buffer, count);
        return asks;
    }

    public static final class AsksEncoder
    {
        public static final int HEADER_SIZE = 4;
        private final OrderBookSnapshotEncoder parentMessage;
        private MutableDirectBuffer buffer;
        private int count;
        private int index;
        private int offset;
        private int initialLimit;

        AsksEncoder(final OrderBookSnapshotEncoder parentMessage)
        {
            this.parentMessage = parentMessage;
        }

        public void wrap(final MutableDirectBuffer buffer, final int count)
        {
            if (count < 0 || count > 65534)
            {
                throw new IllegalArgumentException("count outside allowed range: count=" + count);
            }

            if (buffer != this.buffer)
            {
                this.buffer = buffer;
            }

            index = 0;
            this.count = count;
            final int limit = parentMessage.limit();
            initialLimit = limit;
            parentMessage.limit(limit + HEADER_SIZE);
            buffer.putShort(limit + 0, (short)17, BYTE_ORDER);
            buffer.putShort(limit + 2, (short)count, BYTE_ORDER);
        }

        public AsksEncoder next()
        {
            if (index >= count)
            {
                throw new java.util.NoSuchElementException();
            }

            offset = parentMessage.limit();
            parentMessage.limit(offset + sbeBlockLength());
            ++index;

            return this;
        }

        public int resetCountToIndex()
        {
            count = index;
            buffer.putShort(initialLimit + 2, (short)count, BYTE_ORDER);

            return count;
        }

        public static int countMinValue()
        {
            return 0;
        }

        public static int countMaxValue()
        {
            return 65534;
        }

        public static int sbeHeaderSize()
        {
            return HEADER_SIZE;
        }

        public static int sbeBlockLength()
        {
            return 17;
        }

        public static int priceNanosId()
        {
            return 1;
        }

        public static int priceNanosSinceVersion()
        {
            return 0;
        }

        public static int priceNanosEncodingOffset()
        {
            return 0;
        }

        public static int priceNanosEncodingLength()
        {
            return 8;
        }

        public static String priceNanosMetaAttribute(final MetaAttribute metaAttribute)
        {
            if (MetaAttribute.PRESENCE == metaAttribute)
            {
                return "required";
            }

            return "";
        }

        public static long priceNanosNullValue()
        {
            return -9223372036854775808L;
        }

        public static long priceNanosMinValue()
        {
            return -9223372036854775807L;
        }

        public static long priceNanosMaxValue()
        {
            return 9223372036854775807L;
        }

        public AsksEncoder priceNanos(final long value)
        {
            buffer.putLong(offset + 0, value, BYTE_ORDER);
            return this;
        }


        public static int sizeId()
        {
            return 2;
        }

        public static int sizeSinceVersion()
        {
            return 0;
        }

        public static int sizeEncodingOffset()
        {
            return 8;
        }

        public static int sizeEncodingLength()
        {
            return 4;
        }

        public static String sizeMetaAttribute(final MetaAttribute metaAttribute)
        {
            if (MetaAttribute.PRESENCE == metaAttribute)
            {
                return "required";
            }

            return "";
        }

        public static int sizeNullValue()
        {
            return -2147483648;
        }

        public static int sizeMinValue()
        {
            return -2147483647;
        }

        public static int sizeMaxValue()
        {
            return 2147483647;
        }

        public AsksEncoder size(final int value)
        {
            buffer.putInt(offset + 8, value, BYTE_ORDER);
            return this;
        }


        public static int orderCountId()
        {
            return 3;
        }

        public static int orderCountSinceVersion()
        {
            return 0;
        }

        public static int orderCountEncodingOffset()
        {
            return 12;
        }

        public static int orderCountEncodingLength()
        {
            return 4;
        }

        public static String orderCountMetaAttribute(final MetaAttribute metaAttribute)
        {
            if (MetaAttribute.PRESENCE == metaAttribute)
            {
                return "required";
            }

            return "";
        }

        public static int orderCountNullValue()
        {
            return -2147483648;
        }

        public static int orderCountMinValue()
        {
            return -2147483647;
        }

        public static int orderCountMaxValue()
        {
            return 2147483647;
        }

        public AsksEncoder orderCount(final int value)
        {
            buffer.putInt(offset + 12, value, BYTE_ORDER);
            return this;
        }


        public static int makerId()
        {
            return 4;
        }

        public static int makerSinceVersion()
        {
            return 0;
        }

        public static int makerEncodingOffset()
        {
            return 16;
        }

        public static int makerEncodingLength()
        {
            return 1;
        }

        public static String makerMetaAttribute(final MetaAttribute metaAttribute)
        {
            if (MetaAttribute.PRESENCE == metaAttribute)
            {
                return "required";
            }

            return "";
        }

        public AsksEncoder maker(final NullableBooleanType value)
        {
            buffer.putByte(offset + 16, value.value());
            return this;
        }
    }

    private final MetadataEncoder metadata = new MetadataEncoder(this);

    public static long metadataId()
    {
        return 40;
    }

    public MetadataEncoder metadataCount(final int count)
    {
        metadata.wrap(buffer, count);
        return metadata;
    }

    public static final class MetadataEncoder
    {
        public static final int HEADER_SIZE = 4;
        private final OrderBookSnapshotEncoder parentMessage;
        private MutableDirectBuffer buffer;
        private int count;
        private int index;
        private int offset;
        private int initialLimit;

        MetadataEncoder(final OrderBookSnapshotEncoder parentMessage)
        {
            this.parentMessage = parentMessage;
        }

        public void wrap(final MutableDirectBuffer buffer, final int count)
        {
            if (count < 0 || count > 65534)
            {
                throw new IllegalArgumentException("count outside allowed range: count=" + count);
            }

            if (buffer != this.buffer)
            {
                this.buffer = buffer;
            }

            index = 0;
            this.count = count;
            final int limit = parentMessage.limit();
            initialLimit = limit;
            parentMessage.limit(limit + HEADER_SIZE);
            buffer.putShort(limit + 0, (short)0, BYTE_ORDER);
            buffer.putShort(limit + 2, (short)count, BYTE_ORDER);
        }

        public MetadataEncoder next()
        {
            if (index >= count)
            {
                throw new java.util.NoSuchElementException();
            }

            offset = parentMessage.limit();
            parentMessage.limit(offset + sbeBlockLength());
            ++index;

            return this;
        }

        public int resetCountToIndex()
        {
            count = index;
            buffer.putShort(initialLimit + 2, (short)count, BYTE_ORDER);

            return count;
        }

        public static int countMinValue()
        {
            return 0;
        }

        public static int countMaxValue()
        {
            return 65534;
        }

        public static int sbeHeaderSize()
        {
            return HEADER_SIZE;
        }

        public static int sbeBlockLength()
        {
            return 0;
        }

        public static int keyId()
        {
            return 1;
        }

        public static String keyCharacterEncoding()
        {
            return java.nio.charset.StandardCharsets.US_ASCII.name();
        }

        public static String keyMetaAttribute(final MetaAttribute metaAttribute)
        {
            if (MetaAttribute.PRESENCE == metaAttribute)
            {
                return "required";
            }

            return "";
        }

        public static int keyHeaderLength()
        {
            return 2;
        }

        public MetadataEncoder putKey(final DirectBuffer src, final int srcOffset, final int length)
        {
            if (length > 65534)
            {
                throw new IllegalStateException("length > maxValue for type: " + length);
            }

            final int headerLength = 2;
            final int limit = parentMessage.limit();
            parentMessage.limit(limit + headerLength + length);
            buffer.putShort(limit, (short)length, BYTE_ORDER);
            buffer.putBytes(limit + headerLength, src, srcOffset, length);

            return this;
        }

        public MetadataEncoder putKey(final byte[] src, final int srcOffset, final int length)
        {
            if (length > 65534)
            {
                throw new IllegalStateException("length > maxValue for type: " + length);
            }

            final int headerLength = 2;
            final int limit = parentMessage.limit();
            parentMessage.limit(limit + headerLength + length);
            buffer.putShort(limit, (short)length, BYTE_ORDER);
            buffer.putBytes(limit + headerLength, src, srcOffset, length);

            return this;
        }

        public MetadataEncoder key(final String value)
        {
            final int length = null == value ? 0 : value.length();
            if (length > 65534)
            {
                throw new IllegalStateException("length > maxValue for type: " + length);
            }

            final int headerLength = 2;
            final int limit = parentMessage.limit();
            parentMessage.limit(limit + headerLength + length);
            buffer.putShort(limit, (short)length, BYTE_ORDER);
            buffer.putStringWithoutLengthAscii(limit + headerLength, value);

            return this;
        }

        public MetadataEncoder key(final CharSequence value)
        {
            final int length = null == value ? 0 : value.length();
            if (length > 65534)
            {
                throw new IllegalStateException("length > maxValue for type: " + length);
            }

            final int headerLength = 2;
            final int limit = parentMessage.limit();
            parentMessage.limit(limit + headerLength + length);
            buffer.putShort(limit, (short)length, BYTE_ORDER);
            buffer.putStringWithoutLengthAscii(limit + headerLength, value);

            return this;
        }

        public static int valueId()
        {
            return 2;
        }

        public static String valueCharacterEncoding()
        {
            return java.nio.charset.StandardCharsets.US_ASCII.name();
        }

        public static String valueMetaAttribute(final MetaAttribute metaAttribute)
        {
            if (MetaAttribute.PRESENCE == metaAttribute)
            {
                return "required";
            }

            return "";
        }

        public static int valueHeaderLength()
        {
            return 2;
        }

        public MetadataEncoder putValue(final DirectBuffer src, final int srcOffset, final int length)
        {
            if (length > 65534)
            {
                throw new IllegalStateException("length > maxValue for type: " + length);
            }

            final int headerLength = 2;
            final int limit = parentMessage.limit();
            parentMessage.limit(limit + headerLength + length);
            buffer.putShort(limit, (short)length, BYTE_ORDER);
            buffer.putBytes(limit + headerLength, src, srcOffset, length);

            return this;
        }

        public MetadataEncoder putValue(final byte[] src, final int srcOffset, final int length)
        {
            if (length > 65534)
            {
                throw new IllegalStateException("length > maxValue for type: " + length);
            }

            final int headerLength = 2;
            final int limit = parentMessage.limit();
            parentMessage.limit(limit + headerLength + length);
            buffer.putShort(limit, (short)length, BYTE_ORDER);
            buffer.putBytes(limit + headerLength, src, srcOffset, length);

            return this;
        }

        public MetadataEncoder value(final String value)
        {
            final int length = null == value ? 0 : value.length();
            if (length > 65534)
            {
                throw new IllegalStateException("length > maxValue for type: " + length);
            }

            final int headerLength = 2;
            final int limit = parentMessage.limit();
            parentMessage.limit(limit + headerLength + length);
            buffer.putShort(limit, (short)length, BYTE_ORDER);
            buffer.putStringWithoutLengthAscii(limit + headerLength, value);

            return this;
        }

        public MetadataEncoder value(final CharSequence value)
        {
            final int length = null == value ? 0 : value.length();
            if (length > 65534)
            {
                throw new IllegalStateException("length > maxValue for type: " + length);
            }

            final int headerLength = 2;
            final int limit = parentMessage.limit();
            parentMessage.limit(limit + headerLength + length);
            buffer.putShort(limit, (short)length, BYTE_ORDER);
            buffer.putStringWithoutLengthAscii(limit + headerLength, value);

            return this;
        }
    }

    public static int timestampId()
    {
        return 10;
    }

    public static String timestampCharacterEncoding()
    {
        return java.nio.charset.StandardCharsets.US_ASCII.name();
    }

    public static String timestampMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static int timestampHeaderLength()
    {
        return 2;
    }

    public OrderBookSnapshotEncoder putTimestamp(final DirectBuffer src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public OrderBookSnapshotEncoder putTimestamp(final byte[] src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public OrderBookSnapshotEncoder timestamp(final String value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putStringWithoutLengthAscii(limit + headerLength, value);

        return this;
    }

    public OrderBookSnapshotEncoder timestamp(final CharSequence value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putStringWithoutLengthAscii(limit + headerLength, value);

        return this;
    }

    public static int venueId()
    {
        return 11;
    }

    public static String venueCharacterEncoding()
    {
        return java.nio.charset.StandardCharsets.US_ASCII.name();
    }

    public static String venueMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static int venueHeaderLength()
    {
        return 2;
    }

    public OrderBookSnapshotEncoder putVenue(final DirectBuffer src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public OrderBookSnapshotEncoder putVenue(final byte[] src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public OrderBookSnapshotEncoder venue(final String value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putStringWithoutLengthAscii(limit + headerLength, value);

        return this;
    }

    public OrderBookSnapshotEncoder venue(final CharSequence value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putStringWithoutLengthAscii(limit + headerLength, value);

        return this;
    }

    public static int symbolId()
    {
        return 12;
    }

    public static String symbolCharacterEncoding()
    {
        return java.nio.charset.StandardCharsets.US_ASCII.name();
    }

    public static String symbolMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static int symbolHeaderLength()
    {
        return 2;
    }

    public OrderBookSnapshotEncoder putSymbol(final DirectBuffer src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public OrderBookSnapshotEncoder putSymbol(final byte[] src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public OrderBookSnapshotEncoder symbol(final String value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putStringWithoutLengthAscii(limit + headerLength, value);

        return this;
    }

    public OrderBookSnapshotEncoder symbol(final CharSequence value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putStringWithoutLengthAscii(limit + headerLength, value);

        return this;
    }

    public static int tradingStatusId()
    {
        return 13;
    }

    public static String tradingStatusCharacterEncoding()
    {
        return java.nio.charset.StandardCharsets.US_ASCII.name();
    }

    public static String tradingStatusMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static int tradingStatusHeaderLength()
    {
        return 2;
    }

    public OrderBookSnapshotEncoder putTradingStatus(final DirectBuffer src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public OrderBookSnapshotEncoder putTradingStatus(final byte[] src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public OrderBookSnapshotEncoder tradingStatus(final String value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putStringWithoutLengthAscii(limit + headerLength, value);

        return this;
    }

    public OrderBookSnapshotEncoder tradingStatus(final CharSequence value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putStringWithoutLengthAscii(limit + headerLength, value);

        return this;
    }

    public static int lastTradeAggressorId()
    {
        return 14;
    }

    public static String lastTradeAggressorCharacterEncoding()
    {
        return java.nio.charset.StandardCharsets.US_ASCII.name();
    }

    public static String lastTradeAggressorMetaAttribute(final MetaAttribute metaAttribute)
    {
        if (MetaAttribute.PRESENCE == metaAttribute)
        {
            return "required";
        }

        return "";
    }

    public static int lastTradeAggressorHeaderLength()
    {
        return 2;
    }

    public OrderBookSnapshotEncoder putLastTradeAggressor(final DirectBuffer src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public OrderBookSnapshotEncoder putLastTradeAggressor(final byte[] src, final int srcOffset, final int length)
    {
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public OrderBookSnapshotEncoder lastTradeAggressor(final String value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putStringWithoutLengthAscii(limit + headerLength, value);

        return this;
    }

    public OrderBookSnapshotEncoder lastTradeAggressor(final CharSequence value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 65534)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 2;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putShort(limit, (short)length, BYTE_ORDER);
        buffer.putStringWithoutLengthAscii(limit + headerLength, value);

        return this;
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

        final OrderBookSnapshotDecoder decoder = new OrderBookSnapshotDecoder();
        decoder.wrap(buffer, offset, BLOCK_LENGTH, SCHEMA_VERSION);

        return decoder.appendTo(builder);
    }
}
