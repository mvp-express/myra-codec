/* Generated SBE (Simple Binary Encoding) message codec. */
package express.mvp.myra.codec.bench.codecs.sbe.generated;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;

@SuppressWarnings("all")
public final class OrderBookSnapshotDecoder
{
    public static final int BLOCK_LENGTH = 28;
    public static final int TEMPLATE_ID = 4001;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 1;
    public static final String SEMANTIC_VERSION = "1.0.0";
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final OrderBookSnapshotDecoder parentMessage = this;
    private DirectBuffer buffer;
    private int offset;
    private int limit;
    int actingBlockLength;
    int actingVersion;

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

    public DirectBuffer buffer()
    {
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public OrderBookSnapshotDecoder wrap(
        final DirectBuffer buffer,
        final int offset,
        final int actingBlockLength,
        final int actingVersion)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        this.actingBlockLength = actingBlockLength;
        this.actingVersion = actingVersion;
        limit(offset + actingBlockLength);

        return this;
    }

    public OrderBookSnapshotDecoder wrapAndApplyHeader(
        final DirectBuffer buffer,
        final int offset,
        final MessageHeaderDecoder headerDecoder)
    {
        headerDecoder.wrap(buffer, offset);

        final int templateId = headerDecoder.templateId();
        if (TEMPLATE_ID != templateId)
        {
            throw new IllegalStateException("Invalid TEMPLATE_ID: " + templateId);
        }

        return wrap(
            buffer,
            offset + MessageHeaderDecoder.ENCODED_LENGTH,
            headerDecoder.blockLength(),
            headerDecoder.version());
    }

    public OrderBookSnapshotDecoder sbeRewind()
    {
        return wrap(buffer, offset, actingBlockLength, actingVersion);
    }

    public int sbeDecodedLength()
    {
        final int currentLimit = limit();
        sbeSkip();
        final int decodedLength = encodedLength();
        limit(currentLimit);

        return decodedLength;
    }

    public int actingVersion()
    {
        return actingVersion;
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

    public int instrumentId()
    {
        return buffer.getInt(offset + 0, BYTE_ORDER);
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

    public long sequence()
    {
        return buffer.getLong(offset + 4, BYTE_ORDER);
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

    public short isTradingRaw()
    {
        return ((short)(buffer.getByte(offset + 12) & 0xFF));
    }

    public BooleanType isTrading()
    {
        return BooleanType.get(((short)(buffer.getByte(offset + 12) & 0xFF)));
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

    public short hasTradingStatusRaw()
    {
        return ((short)(buffer.getByte(offset + 13) & 0xFF));
    }

    public BooleanType hasTradingStatus()
    {
        return BooleanType.get(((short)(buffer.getByte(offset + 13) & 0xFF)));
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

    public short hasLastTradeRaw()
    {
        return ((short)(buffer.getByte(offset + 14) & 0xFF));
    }

    public BooleanType hasLastTrade()
    {
        return BooleanType.get(((short)(buffer.getByte(offset + 14) & 0xFF)));
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

    public long lastTradePrice()
    {
        return buffer.getLong(offset + 15, BYTE_ORDER);
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

    public int lastTradeSize()
    {
        return buffer.getInt(offset + 23, BYTE_ORDER);
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

    public short hasLastTradeAggressorRaw()
    {
        return ((short)(buffer.getByte(offset + 27) & 0xFF));
    }

    public BooleanType hasLastTradeAggressor()
    {
        return BooleanType.get(((short)(buffer.getByte(offset + 27) & 0xFF)));
    }


    private final BidsDecoder bids = new BidsDecoder(this);

    public static long bidsDecoderId()
    {
        return 20;
    }

    public static int bidsDecoderSinceVersion()
    {
        return 0;
    }

    public BidsDecoder bids()
    {
        bids.wrap(buffer);
        return bids;
    }

    public static final class BidsDecoder
        implements Iterable<BidsDecoder>, java.util.Iterator<BidsDecoder>
    {
        public static final int HEADER_SIZE = 4;
        private final OrderBookSnapshotDecoder parentMessage;
        private DirectBuffer buffer;
        private int count;
        private int index;
        private int offset;
        private int blockLength;

        BidsDecoder(final OrderBookSnapshotDecoder parentMessage)
        {
            this.parentMessage = parentMessage;
        }

        public void wrap(final DirectBuffer buffer)
        {
            if (buffer != this.buffer)
            {
                this.buffer = buffer;
            }

            index = 0;
            final int limit = parentMessage.limit();
            parentMessage.limit(limit + HEADER_SIZE);
            blockLength = (buffer.getShort(limit + 0, BYTE_ORDER) & 0xFFFF);
            count = (buffer.getShort(limit + 2, BYTE_ORDER) & 0xFFFF);
        }

        public BidsDecoder next()
        {
            if (index >= count)
            {
                throw new java.util.NoSuchElementException();
            }

            offset = parentMessage.limit();
            parentMessage.limit(offset + blockLength);
            ++index;

            return this;
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

        public int actingBlockLength()
        {
            return blockLength;
        }

        public int actingVersion()
        {
            return parentMessage.actingVersion;
        }

        public int count()
        {
            return count;
        }

        public java.util.Iterator<BidsDecoder> iterator()
        {
            return this;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext()
        {
            return index < count;
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

        public long priceNanos()
        {
            return buffer.getLong(offset + 0, BYTE_ORDER);
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

        public int size()
        {
            return buffer.getInt(offset + 8, BYTE_ORDER);
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

        public int orderCount()
        {
            return buffer.getInt(offset + 12, BYTE_ORDER);
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

        public byte makerRaw()
        {
            return buffer.getByte(offset + 16);
        }

        public NullableBooleanType maker()
        {
            return NullableBooleanType.get(buffer.getByte(offset + 16));
        }


        public StringBuilder appendTo(final StringBuilder builder)
        {
            if (null == buffer)
            {
                return builder;
            }

            builder.append('(');
            builder.append("priceNanos=");
            builder.append(this.priceNanos());
            builder.append('|');
            builder.append("size=");
            builder.append(this.size());
            builder.append('|');
            builder.append("orderCount=");
            builder.append(this.orderCount());
            builder.append('|');
            builder.append("maker=");
            builder.append(this.maker());
            builder.append(')');

            return builder;
        }
        
        public BidsDecoder sbeSkip()
        {

            return this;
        }
    }

    private final AsksDecoder asks = new AsksDecoder(this);

    public static long asksDecoderId()
    {
        return 30;
    }

    public static int asksDecoderSinceVersion()
    {
        return 0;
    }

    public AsksDecoder asks()
    {
        asks.wrap(buffer);
        return asks;
    }

    public static final class AsksDecoder
        implements Iterable<AsksDecoder>, java.util.Iterator<AsksDecoder>
    {
        public static final int HEADER_SIZE = 4;
        private final OrderBookSnapshotDecoder parentMessage;
        private DirectBuffer buffer;
        private int count;
        private int index;
        private int offset;
        private int blockLength;

        AsksDecoder(final OrderBookSnapshotDecoder parentMessage)
        {
            this.parentMessage = parentMessage;
        }

        public void wrap(final DirectBuffer buffer)
        {
            if (buffer != this.buffer)
            {
                this.buffer = buffer;
            }

            index = 0;
            final int limit = parentMessage.limit();
            parentMessage.limit(limit + HEADER_SIZE);
            blockLength = (buffer.getShort(limit + 0, BYTE_ORDER) & 0xFFFF);
            count = (buffer.getShort(limit + 2, BYTE_ORDER) & 0xFFFF);
        }

        public AsksDecoder next()
        {
            if (index >= count)
            {
                throw new java.util.NoSuchElementException();
            }

            offset = parentMessage.limit();
            parentMessage.limit(offset + blockLength);
            ++index;

            return this;
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

        public int actingBlockLength()
        {
            return blockLength;
        }

        public int actingVersion()
        {
            return parentMessage.actingVersion;
        }

        public int count()
        {
            return count;
        }

        public java.util.Iterator<AsksDecoder> iterator()
        {
            return this;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext()
        {
            return index < count;
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

        public long priceNanos()
        {
            return buffer.getLong(offset + 0, BYTE_ORDER);
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

        public int size()
        {
            return buffer.getInt(offset + 8, BYTE_ORDER);
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

        public int orderCount()
        {
            return buffer.getInt(offset + 12, BYTE_ORDER);
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

        public byte makerRaw()
        {
            return buffer.getByte(offset + 16);
        }

        public NullableBooleanType maker()
        {
            return NullableBooleanType.get(buffer.getByte(offset + 16));
        }


        public StringBuilder appendTo(final StringBuilder builder)
        {
            if (null == buffer)
            {
                return builder;
            }

            builder.append('(');
            builder.append("priceNanos=");
            builder.append(this.priceNanos());
            builder.append('|');
            builder.append("size=");
            builder.append(this.size());
            builder.append('|');
            builder.append("orderCount=");
            builder.append(this.orderCount());
            builder.append('|');
            builder.append("maker=");
            builder.append(this.maker());
            builder.append(')');

            return builder;
        }
        
        public AsksDecoder sbeSkip()
        {

            return this;
        }
    }

    private final MetadataDecoder metadata = new MetadataDecoder(this);

    public static long metadataDecoderId()
    {
        return 40;
    }

    public static int metadataDecoderSinceVersion()
    {
        return 0;
    }

    public MetadataDecoder metadata()
    {
        metadata.wrap(buffer);
        return metadata;
    }

    public static final class MetadataDecoder
        implements Iterable<MetadataDecoder>, java.util.Iterator<MetadataDecoder>
    {
        public static final int HEADER_SIZE = 4;
        private final OrderBookSnapshotDecoder parentMessage;
        private DirectBuffer buffer;
        private int count;
        private int index;
        private int offset;
        private int blockLength;

        MetadataDecoder(final OrderBookSnapshotDecoder parentMessage)
        {
            this.parentMessage = parentMessage;
        }

        public void wrap(final DirectBuffer buffer)
        {
            if (buffer != this.buffer)
            {
                this.buffer = buffer;
            }

            index = 0;
            final int limit = parentMessage.limit();
            parentMessage.limit(limit + HEADER_SIZE);
            blockLength = (buffer.getShort(limit + 0, BYTE_ORDER) & 0xFFFF);
            count = (buffer.getShort(limit + 2, BYTE_ORDER) & 0xFFFF);
        }

        public MetadataDecoder next()
        {
            if (index >= count)
            {
                throw new java.util.NoSuchElementException();
            }

            offset = parentMessage.limit();
            parentMessage.limit(offset + blockLength);
            ++index;

            return this;
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

        public int actingBlockLength()
        {
            return blockLength;
        }

        public int actingVersion()
        {
            return parentMessage.actingVersion;
        }

        public int count()
        {
            return count;
        }

        public java.util.Iterator<MetadataDecoder> iterator()
        {
            return this;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext()
        {
            return index < count;
        }

        public static int keyId()
        {
            return 1;
        }

        public static int keySinceVersion()
        {
            return 0;
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

        public int keyLength()
        {
            final int limit = parentMessage.limit();
            return (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        }

        public int skipKey()
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            final int dataOffset = limit + headerLength;
            parentMessage.limit(dataOffset + dataLength);

            return dataLength;
        }

        public int getKey(final MutableDirectBuffer dst, final int dstOffset, final int length)
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            final int bytesCopied = Math.min(length, dataLength);
            parentMessage.limit(limit + headerLength + dataLength);
            buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

            return bytesCopied;
        }

        public int getKey(final byte[] dst, final int dstOffset, final int length)
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            final int bytesCopied = Math.min(length, dataLength);
            parentMessage.limit(limit + headerLength + dataLength);
            buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

            return bytesCopied;
        }

        public void wrapKey(final DirectBuffer wrapBuffer)
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            parentMessage.limit(limit + headerLength + dataLength);
            wrapBuffer.wrap(buffer, limit + headerLength, dataLength);
        }

        public String key()
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            parentMessage.limit(limit + headerLength + dataLength);

            if (0 == dataLength)
            {
                return "";
            }

            final byte[] tmp = new byte[dataLength];
            buffer.getBytes(limit + headerLength, tmp, 0, dataLength);

            return new String(tmp, java.nio.charset.StandardCharsets.US_ASCII);
        }

        public int getKey(final Appendable appendable)
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            final int dataOffset = limit + headerLength;

            parentMessage.limit(dataOffset + dataLength);
            buffer.getStringWithoutLengthAscii(dataOffset, dataLength, appendable);

            return dataLength;
        }

        public static int valueId()
        {
            return 2;
        }

        public static int valueSinceVersion()
        {
            return 0;
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

        public int valueLength()
        {
            final int limit = parentMessage.limit();
            return (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        }

        public int skipValue()
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            final int dataOffset = limit + headerLength;
            parentMessage.limit(dataOffset + dataLength);

            return dataLength;
        }

        public int getValue(final MutableDirectBuffer dst, final int dstOffset, final int length)
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            final int bytesCopied = Math.min(length, dataLength);
            parentMessage.limit(limit + headerLength + dataLength);
            buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

            return bytesCopied;
        }

        public int getValue(final byte[] dst, final int dstOffset, final int length)
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            final int bytesCopied = Math.min(length, dataLength);
            parentMessage.limit(limit + headerLength + dataLength);
            buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

            return bytesCopied;
        }

        public void wrapValue(final DirectBuffer wrapBuffer)
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            parentMessage.limit(limit + headerLength + dataLength);
            wrapBuffer.wrap(buffer, limit + headerLength, dataLength);
        }

        public String value()
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            parentMessage.limit(limit + headerLength + dataLength);

            if (0 == dataLength)
            {
                return "";
            }

            final byte[] tmp = new byte[dataLength];
            buffer.getBytes(limit + headerLength, tmp, 0, dataLength);

            return new String(tmp, java.nio.charset.StandardCharsets.US_ASCII);
        }

        public int getValue(final Appendable appendable)
        {
            final int headerLength = 2;
            final int limit = parentMessage.limit();
            final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
            final int dataOffset = limit + headerLength;

            parentMessage.limit(dataOffset + dataLength);
            buffer.getStringWithoutLengthAscii(dataOffset, dataLength, appendable);

            return dataLength;
        }

        public StringBuilder appendTo(final StringBuilder builder)
        {
            if (null == buffer)
            {
                return builder;
            }

            builder.append('(');
            builder.append("key=");
            builder.append('\'');
            getKey(builder);
            builder.append('\'');
            builder.append('|');
            builder.append("value=");
            builder.append('\'');
            getValue(builder);
            builder.append('\'');
            builder.append(')');

            return builder;
        }
        
        public MetadataDecoder sbeSkip()
        {
            skipKey();
            skipValue();

            return this;
        }
    }

    public static int timestampId()
    {
        return 10;
    }

    public static int timestampSinceVersion()
    {
        return 0;
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

    public int timestampLength()
    {
        final int limit = parentMessage.limit();
        return (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
    }

    public int skipTimestamp()
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int dataOffset = limit + headerLength;
        parentMessage.limit(dataOffset + dataLength);

        return dataLength;
    }

    public int getTimestamp(final MutableDirectBuffer dst, final int dstOffset, final int length)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int getTimestamp(final byte[] dst, final int dstOffset, final int length)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public void wrapTimestamp(final DirectBuffer wrapBuffer)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        parentMessage.limit(limit + headerLength + dataLength);
        wrapBuffer.wrap(buffer, limit + headerLength, dataLength);
    }

    public String timestamp()
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        parentMessage.limit(limit + headerLength + dataLength);

        if (0 == dataLength)
        {
            return "";
        }

        final byte[] tmp = new byte[dataLength];
        buffer.getBytes(limit + headerLength, tmp, 0, dataLength);

        return new String(tmp, java.nio.charset.StandardCharsets.US_ASCII);
    }

    public int getTimestamp(final Appendable appendable)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int dataOffset = limit + headerLength;

        parentMessage.limit(dataOffset + dataLength);
        buffer.getStringWithoutLengthAscii(dataOffset, dataLength, appendable);

        return dataLength;
    }

    public static int venueId()
    {
        return 11;
    }

    public static int venueSinceVersion()
    {
        return 0;
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

    public int venueLength()
    {
        final int limit = parentMessage.limit();
        return (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
    }

    public int skipVenue()
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int dataOffset = limit + headerLength;
        parentMessage.limit(dataOffset + dataLength);

        return dataLength;
    }

    public int getVenue(final MutableDirectBuffer dst, final int dstOffset, final int length)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int getVenue(final byte[] dst, final int dstOffset, final int length)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public void wrapVenue(final DirectBuffer wrapBuffer)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        parentMessage.limit(limit + headerLength + dataLength);
        wrapBuffer.wrap(buffer, limit + headerLength, dataLength);
    }

    public String venue()
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        parentMessage.limit(limit + headerLength + dataLength);

        if (0 == dataLength)
        {
            return "";
        }

        final byte[] tmp = new byte[dataLength];
        buffer.getBytes(limit + headerLength, tmp, 0, dataLength);

        return new String(tmp, java.nio.charset.StandardCharsets.US_ASCII);
    }

    public int getVenue(final Appendable appendable)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int dataOffset = limit + headerLength;

        parentMessage.limit(dataOffset + dataLength);
        buffer.getStringWithoutLengthAscii(dataOffset, dataLength, appendable);

        return dataLength;
    }

    public static int symbolId()
    {
        return 12;
    }

    public static int symbolSinceVersion()
    {
        return 0;
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

    public int symbolLength()
    {
        final int limit = parentMessage.limit();
        return (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
    }

    public int skipSymbol()
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int dataOffset = limit + headerLength;
        parentMessage.limit(dataOffset + dataLength);

        return dataLength;
    }

    public int getSymbol(final MutableDirectBuffer dst, final int dstOffset, final int length)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int getSymbol(final byte[] dst, final int dstOffset, final int length)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public void wrapSymbol(final DirectBuffer wrapBuffer)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        parentMessage.limit(limit + headerLength + dataLength);
        wrapBuffer.wrap(buffer, limit + headerLength, dataLength);
    }

    public String symbol()
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        parentMessage.limit(limit + headerLength + dataLength);

        if (0 == dataLength)
        {
            return "";
        }

        final byte[] tmp = new byte[dataLength];
        buffer.getBytes(limit + headerLength, tmp, 0, dataLength);

        return new String(tmp, java.nio.charset.StandardCharsets.US_ASCII);
    }

    public int getSymbol(final Appendable appendable)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int dataOffset = limit + headerLength;

        parentMessage.limit(dataOffset + dataLength);
        buffer.getStringWithoutLengthAscii(dataOffset, dataLength, appendable);

        return dataLength;
    }

    public static int tradingStatusId()
    {
        return 13;
    }

    public static int tradingStatusSinceVersion()
    {
        return 0;
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

    public int tradingStatusLength()
    {
        final int limit = parentMessage.limit();
        return (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
    }

    public int skipTradingStatus()
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int dataOffset = limit + headerLength;
        parentMessage.limit(dataOffset + dataLength);

        return dataLength;
    }

    public int getTradingStatus(final MutableDirectBuffer dst, final int dstOffset, final int length)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int getTradingStatus(final byte[] dst, final int dstOffset, final int length)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public void wrapTradingStatus(final DirectBuffer wrapBuffer)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        parentMessage.limit(limit + headerLength + dataLength);
        wrapBuffer.wrap(buffer, limit + headerLength, dataLength);
    }

    public String tradingStatus()
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        parentMessage.limit(limit + headerLength + dataLength);

        if (0 == dataLength)
        {
            return "";
        }

        final byte[] tmp = new byte[dataLength];
        buffer.getBytes(limit + headerLength, tmp, 0, dataLength);

        return new String(tmp, java.nio.charset.StandardCharsets.US_ASCII);
    }

    public int getTradingStatus(final Appendable appendable)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int dataOffset = limit + headerLength;

        parentMessage.limit(dataOffset + dataLength);
        buffer.getStringWithoutLengthAscii(dataOffset, dataLength, appendable);

        return dataLength;
    }

    public static int lastTradeAggressorId()
    {
        return 14;
    }

    public static int lastTradeAggressorSinceVersion()
    {
        return 0;
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

    public int lastTradeAggressorLength()
    {
        final int limit = parentMessage.limit();
        return (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
    }

    public int skipLastTradeAggressor()
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int dataOffset = limit + headerLength;
        parentMessage.limit(dataOffset + dataLength);

        return dataLength;
    }

    public int getLastTradeAggressor(final MutableDirectBuffer dst, final int dstOffset, final int length)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int getLastTradeAggressor(final byte[] dst, final int dstOffset, final int length)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public void wrapLastTradeAggressor(final DirectBuffer wrapBuffer)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        parentMessage.limit(limit + headerLength + dataLength);
        wrapBuffer.wrap(buffer, limit + headerLength, dataLength);
    }

    public String lastTradeAggressor()
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        parentMessage.limit(limit + headerLength + dataLength);

        if (0 == dataLength)
        {
            return "";
        }

        final byte[] tmp = new byte[dataLength];
        buffer.getBytes(limit + headerLength, tmp, 0, dataLength);

        return new String(tmp, java.nio.charset.StandardCharsets.US_ASCII);
    }

    public int getLastTradeAggressor(final Appendable appendable)
    {
        final int headerLength = 2;
        final int limit = parentMessage.limit();
        final int dataLength = (buffer.getShort(limit, BYTE_ORDER) & 0xFFFF);
        final int dataOffset = limit + headerLength;

        parentMessage.limit(dataOffset + dataLength);
        buffer.getStringWithoutLengthAscii(dataOffset, dataLength, appendable);

        return dataLength;
    }

    public String toString()
    {
        if (null == buffer)
        {
            return "";
        }

        final OrderBookSnapshotDecoder decoder = new OrderBookSnapshotDecoder();
        decoder.wrap(buffer, offset, actingBlockLength, actingVersion);

        return decoder.appendTo(new StringBuilder()).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        if (null == buffer)
        {
            return builder;
        }

        final int originalLimit = limit();
        limit(offset + actingBlockLength);
        builder.append("[OrderBookSnapshot](sbeTemplateId=");
        builder.append(TEMPLATE_ID);
        builder.append("|sbeSchemaId=");
        builder.append(SCHEMA_ID);
        builder.append("|sbeSchemaVersion=");
        if (parentMessage.actingVersion != SCHEMA_VERSION)
        {
            builder.append(parentMessage.actingVersion);
            builder.append('/');
        }
        builder.append(SCHEMA_VERSION);
        builder.append("|sbeBlockLength=");
        if (actingBlockLength != BLOCK_LENGTH)
        {
            builder.append(actingBlockLength);
            builder.append('/');
        }
        builder.append(BLOCK_LENGTH);
        builder.append("):");
        builder.append("instrumentId=");
        builder.append(this.instrumentId());
        builder.append('|');
        builder.append("sequence=");
        builder.append(this.sequence());
        builder.append('|');
        builder.append("isTrading=");
        builder.append(this.isTrading());
        builder.append('|');
        builder.append("hasTradingStatus=");
        builder.append(this.hasTradingStatus());
        builder.append('|');
        builder.append("hasLastTrade=");
        builder.append(this.hasLastTrade());
        builder.append('|');
        builder.append("lastTradePrice=");
        builder.append(this.lastTradePrice());
        builder.append('|');
        builder.append("lastTradeSize=");
        builder.append(this.lastTradeSize());
        builder.append('|');
        builder.append("hasLastTradeAggressor=");
        builder.append(this.hasLastTradeAggressor());
        builder.append('|');
        builder.append("bids=[");
        final int bidsOriginalOffset = bids.offset;
        final int bidsOriginalIndex = bids.index;
        final BidsDecoder bids = this.bids();
        if (bids.count() > 0)
        {
            while (bids.hasNext())
            {
                bids.next().appendTo(builder);
                builder.append(',');
            }
            builder.setLength(builder.length() - 1);
        }
        bids.offset = bidsOriginalOffset;
        bids.index = bidsOriginalIndex;
        builder.append(']');
        builder.append('|');
        builder.append("asks=[");
        final int asksOriginalOffset = asks.offset;
        final int asksOriginalIndex = asks.index;
        final AsksDecoder asks = this.asks();
        if (asks.count() > 0)
        {
            while (asks.hasNext())
            {
                asks.next().appendTo(builder);
                builder.append(',');
            }
            builder.setLength(builder.length() - 1);
        }
        asks.offset = asksOriginalOffset;
        asks.index = asksOriginalIndex;
        builder.append(']');
        builder.append('|');
        builder.append("metadata=[");
        final int metadataOriginalOffset = metadata.offset;
        final int metadataOriginalIndex = metadata.index;
        final MetadataDecoder metadata = this.metadata();
        if (metadata.count() > 0)
        {
            while (metadata.hasNext())
            {
                metadata.next().appendTo(builder);
                builder.append(',');
            }
            builder.setLength(builder.length() - 1);
        }
        metadata.offset = metadataOriginalOffset;
        metadata.index = metadataOriginalIndex;
        builder.append(']');
        builder.append('|');
        builder.append("timestamp=");
        builder.append('\'');
        getTimestamp(builder);
        builder.append('\'');
        builder.append('|');
        builder.append("venue=");
        builder.append('\'');
        getVenue(builder);
        builder.append('\'');
        builder.append('|');
        builder.append("symbol=");
        builder.append('\'');
        getSymbol(builder);
        builder.append('\'');
        builder.append('|');
        builder.append("tradingStatus=");
        builder.append('\'');
        getTradingStatus(builder);
        builder.append('\'');
        builder.append('|');
        builder.append("lastTradeAggressor=");
        builder.append('\'');
        getLastTradeAggressor(builder);
        builder.append('\'');

        limit(originalLimit);

        return builder;
    }
    
    public OrderBookSnapshotDecoder sbeSkip()
    {
        sbeRewind();
        BidsDecoder bids = this.bids();
        if (bids.count() > 0)
        {
            while (bids.hasNext())
            {
                bids.next();
                bids.sbeSkip();
            }
        }
        AsksDecoder asks = this.asks();
        if (asks.count() > 0)
        {
            while (asks.hasNext())
            {
                asks.next();
                asks.sbeSkip();
            }
        }
        MetadataDecoder metadata = this.metadata();
        if (metadata.count() > 0)
        {
            while (metadata.hasNext())
            {
                metadata.next();
                metadata.sbeSkip();
            }
        }
        skipTimestamp();
        skipVenue();
        skipSymbol();
        skipTradingStatus();
        skipLastTradeAggressor();

        return this;
    }
}
