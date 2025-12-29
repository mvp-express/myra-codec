package express.mvp.myra.codec.examples.generated.orderbook;

import express.mvp.myra.codec.runtime.struct.VariableSizeRepeatingGroupIterator;
import express.mvp.roray.ffm.utils.memory.BinaryWriter;
import express.mvp.roray.ffm.utils.memory.BitSetView;
import express.mvp.roray.ffm.utils.memory.FlyweightAccessor;
import express.mvp.roray.ffm.utils.memory.Layouts;
import express.mvp.roray.ffm.utils.memory.Utf8View;
import java.lang.Override;
import java.lang.foreign.MemorySegment;

/**
 * Auto-generated, zero-copy flyweight for the OrderBookSnapshot message.
 *
 * Provides direct access to binary data without deserialization overhead.
 * Thread-safe for read operations when properly synchronized.
 *
 * @see FlyweightAccessor
 */
public final class OrderBookSnapshotFlyweight implements FlyweightAccessor {
    public static final int PRESENCE_BYTES = 1;

    public static final int TRADINGSTATUS_OPT_BIT = 0;

    public static final int LASTTRADE_OPT_BIT = 1;

    public static final int INSTRUMENTID_OFFSET = 1;

    public static final int SEQUENCE_OFFSET = 5;

    public static final int ISTRADING_OFFSET = 13;

    public static final int TIMESTAMP_OFFSET = 14;

    public static final int VENUE_OFFSET = 22;

    public static final int SYMBOL_OFFSET = 30;

    public static final int TRADINGSTATUS_OFFSET = 38;

    public static final int LASTTRADE_OFFSET = 46;

    public static final int BIDS_OFFSET = 54;

    public static final int ASKS_OFFSET = 62;

    public static final int METADATA_OFFSET = 70;

    public static final int TEMPLATE_ID = 4;

    /**
     * Schema version in wire format: 1.0
     */
    public static final short SCHEMA_VERSION = (short) 256;

    public static final int BLOCK_LENGTH = 78;

    private MemorySegment segment;

    private long offset;

    private final BitSetView presenceBits = new BitSetView();

    private final Utf8View timestampView = new Utf8View();

    private final Utf8View venueView = new Utf8View();

    private final Utf8View symbolView = new Utf8View();

    private final Utf8View tradingStatusView = new Utf8View();

    private final TradeFlyweight lastTradeView = new TradeFlyweight();

    private final VariableSizeRepeatingGroupIterator bidsIterator = new VariableSizeRepeatingGroupIterator();

    private final LevelFlyweight bidsView = new LevelFlyweight();

    private final VariableSizeRepeatingGroupIterator asksIterator = new VariableSizeRepeatingGroupIterator();

    private final LevelFlyweight asksView = new LevelFlyweight();

    private final VariableSizeRepeatingGroupIterator metadataIterator = new VariableSizeRepeatingGroupIterator();

    private final MetadataEntryFlyweight metadataView = new MetadataEntryFlyweight();

    @Override
    public void wrap(MemorySegment segment, long offset) {
        this.segment = segment;
        this.offset = offset;
        this.presenceBits.wrap(segment, offset, PRESENCE_BYTES);
    }

    @Override
    public MemorySegment segment() {
        return this.segment;
    }

    @Override
    public int byteSize() {
        return BLOCK_LENGTH;
    }

    @Override
    public boolean isWrapped() {
        return this.segment != null;
    }

    @Override
    public void validate() {
        if (this.segment == null) {
            throw new IllegalStateException("Flyweight is not wrapped");
        }
        final long remaining = segment.byteSize() - this.offset;
        if (remaining < BLOCK_LENGTH) {
            throw new IllegalStateException("Insufficient bytes for flyweight: required " + BLOCK_LENGTH);
        }
    }

    public int getInstrumentId() {
        return segment.get(Layouts.INT_BE, this.offset + INSTRUMENTID_OFFSET);
    }

    public void setInstrumentId(int value) {
        segment.set(Layouts.INT_BE, this.offset + INSTRUMENTID_OFFSET, value);
    }

    public long getSequence() {
        return segment.get(Layouts.LONG_BE, this.offset + SEQUENCE_OFFSET);
    }

    public void setSequence(long value) {
        segment.set(Layouts.LONG_BE, this.offset + SEQUENCE_OFFSET, value);
    }

    public boolean getIsTrading() {
        return segment.get(Layouts.BOOLEAN, this.offset + ISTRADING_OFFSET);
    }

    public void setIsTrading(boolean value) {
        segment.set(Layouts.BOOLEAN, this.offset + ISTRADING_OFFSET, value);
    }

    public Utf8View getTimestamp() {
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + TIMESTAMP_OFFSET);
        final int dataLength = segment.get(Layouts.INT_BE, this.offset + TIMESTAMP_OFFSET + 4);
        this.timestampView.wrap(this.segment, this.offset + relativeOffset, dataLength);
        return this.timestampView;
    }

    public Utf8View getVenue() {
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + VENUE_OFFSET);
        final int dataLength = segment.get(Layouts.INT_BE, this.offset + VENUE_OFFSET + 4);
        this.venueView.wrap(this.segment, this.offset + relativeOffset, dataLength);
        return this.venueView;
    }

    public Utf8View getSymbol() {
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + SYMBOL_OFFSET);
        final int dataLength = segment.get(Layouts.INT_BE, this.offset + SYMBOL_OFFSET + 4);
        this.symbolView.wrap(this.segment, this.offset + relativeOffset, dataLength);
        return this.symbolView;
    }

    public Utf8View getTradingStatus() {
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + TRADINGSTATUS_OFFSET);
        final int dataLength = segment.get(Layouts.INT_BE, this.offset + TRADINGSTATUS_OFFSET + 4);
        this.tradingStatusView.wrap(this.segment, this.offset + relativeOffset, dataLength);
        return this.tradingStatusView;
    }

    public TradeFlyweight getLastTrade() {
        if (!hasLastTrade()) {
            throw new IllegalStateException("Field 'lastTrade' is not present");
        }
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + LASTTRADE_OFFSET);
        this.lastTradeView.wrap(this.segment, this.offset + relativeOffset);
        return this.lastTradeView;
    }

    public int getBidsCount() {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + BIDS_OFFSET);
        this.bidsIterator.wrap(this.segment, dataOffset);
        return this.bidsIterator.count();
    }

    /**
     * Returns the nested message at the given index, wrapped in a reusable flyweight.
     * @param index the element index (0-based)
     * @return the flyweight wrapper (reused instance)
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public LevelFlyweight getBidsAt(int index) {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + BIDS_OFFSET);
        this.bidsIterator.wrap(this.segment, dataOffset);
        return this.bidsIterator.wrapElementAt(index, this.bidsView);
    }

    public int getAsksCount() {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + ASKS_OFFSET);
        this.asksIterator.wrap(this.segment, dataOffset);
        return this.asksIterator.count();
    }

    /**
     * Returns the nested message at the given index, wrapped in a reusable flyweight.
     * @param index the element index (0-based)
     * @return the flyweight wrapper (reused instance)
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public LevelFlyweight getAsksAt(int index) {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + ASKS_OFFSET);
        this.asksIterator.wrap(this.segment, dataOffset);
        return this.asksIterator.wrapElementAt(index, this.asksView);
    }

    public int getMetadataCount() {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + METADATA_OFFSET);
        this.metadataIterator.wrap(this.segment, dataOffset);
        return this.metadataIterator.count();
    }

    /**
     * Returns the nested message at the given index, wrapped in a reusable flyweight.
     * @param index the element index (0-based)
     * @return the flyweight wrapper (reused instance)
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public MetadataEntryFlyweight getMetadataAt(int index) {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + METADATA_OFFSET);
        this.metadataIterator.wrap(this.segment, dataOffset);
        return this.metadataIterator.wrapElementAt(index, this.metadataView);
    }

    public boolean hasTradingStatus() {
        return this.presenceBits.get(0);
    }

    public boolean hasLastTrade() {
        return this.presenceBits.get(1);
    }

    @Override
    public void writeTo(BinaryWriter writer) {
        Utf8View timestampViewTmp = this.getTimestamp();
        byte[] timestampBytesTmp = new byte[(int)timestampViewTmp.byteSize()];
        MemorySegment.copy(timestampViewTmp.segment(), timestampViewTmp.offset(), MemorySegment.ofArray(timestampBytesTmp), 0, timestampViewTmp.byteSize());
        writer.writeBytes(timestampBytesTmp);
        Utf8View venueViewTmp = this.getVenue();
        byte[] venueBytesTmp = new byte[(int)venueViewTmp.byteSize()];
        MemorySegment.copy(venueViewTmp.segment(), venueViewTmp.offset(), MemorySegment.ofArray(venueBytesTmp), 0, venueViewTmp.byteSize());
        writer.writeBytes(venueBytesTmp);
        Utf8View symbolViewTmp = this.getSymbol();
        byte[] symbolBytesTmp = new byte[(int)symbolViewTmp.byteSize()];
        MemorySegment.copy(symbolViewTmp.segment(), symbolViewTmp.offset(), MemorySegment.ofArray(symbolBytesTmp), 0, symbolViewTmp.byteSize());
        writer.writeBytes(symbolBytesTmp);
        writer.writeIntBE(this.getInstrumentId());
        writer.writeLongBE(this.getSequence());
        writer.writeBoolean(this.getIsTrading());
        Utf8View tradingStatusViewTmp = this.getTradingStatus();
        byte[] tradingStatusBytesTmp = new byte[(int)tradingStatusViewTmp.byteSize()];
        MemorySegment.copy(tradingStatusViewTmp.segment(), tradingStatusViewTmp.offset(), MemorySegment.ofArray(tradingStatusBytesTmp), 0, tradingStatusViewTmp.byteSize());
        writer.writeBytes(tradingStatusBytesTmp);
        final int relativeOffset = this.segment.get(Layouts.INT_BE, this.offset + LASTTRADE_OFFSET);
        final int nestedLength = this.segment.get(Layouts.INT_BE, this.offset + LASTTRADE_OFFSET + 4);
        writer.writeVarInt(nestedLength);
        writer.writeSegmentRaw(this.segment, this.offset + relativeOffset, nestedLength);
        final int relativeOffset = this.segment.get(Layouts.INT_BE, this.offset + BIDS_OFFSET);
        final int nestedLength = this.segment.get(Layouts.INT_BE, this.offset + BIDS_OFFSET + 4);
        writer.writeVarInt(nestedLength);
        writer.writeSegmentRaw(this.segment, this.offset + relativeOffset, nestedLength);
        final int relativeOffset = this.segment.get(Layouts.INT_BE, this.offset + ASKS_OFFSET);
        final int nestedLength = this.segment.get(Layouts.INT_BE, this.offset + ASKS_OFFSET + 4);
        writer.writeVarInt(nestedLength);
        writer.writeSegmentRaw(this.segment, this.offset + relativeOffset, nestedLength);
        final int relativeOffset = this.segment.get(Layouts.INT_BE, this.offset + METADATA_OFFSET);
        final int nestedLength = this.segment.get(Layouts.INT_BE, this.offset + METADATA_OFFSET + 4);
        writer.writeVarInt(nestedLength);
        writer.writeSegmentRaw(this.segment, this.offset + relativeOffset, nestedLength);
    }
}
