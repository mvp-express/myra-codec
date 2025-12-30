package express.mvp.myra.codec.examples.generated.orderbook;

import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.myra.codec.runtime.struct.VariableSizeRepeatingGroupBuilder;
import express.mvp.roray.ffm.utils.memory.BitSetView;
import express.mvp.roray.ffm.utils.memory.Layouts;
import express.mvp.roray.ffm.utils.memory.PooledSegment;
import express.mvp.roray.ffm.utils.memory.VarFieldWriter;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Single-pass, write-once builder for {@code OrderBookSnapshot}.
 */
public final class OrderBookSnapshotBuilder {
    private static final int TOTAL_FIELDS = 11;

    private static final int VAR_FIELD_COUNT = 7;

    private static final int PRESENCE_BYTES = 1;

    private static final String[] FIELD_NAMES = {
        "timestamp",
        "venue",
        "symbol",
        "instrumentId",
        "sequence",
        "isTrading",
        "tradingStatus",
        "lastTrade",
        "bids",
        "asks",
        "metadata"
    };

    private static final int TRADINGSTATUS_OPT_BIT = 0;

    private static final int LASTTRADE_OPT_BIT = 1;

    private static final int TIMESTAMP_INDEX = 0;

    private static final int TIMESTAMP_VAR_SLOT = 0;

    private static final int VENUE_INDEX = 1;

    private static final int VENUE_VAR_SLOT = 1;

    private static final int SYMBOL_INDEX = 2;

    private static final int SYMBOL_VAR_SLOT = 2;

    private static final int INSTRUMENTID_INDEX = 3;

    private static final int SEQUENCE_INDEX = 4;

    private static final int ISTRADING_INDEX = 5;

    private static final int TRADINGSTATUS_INDEX = 6;

    private static final int LASTTRADE_INDEX = 7;

    private static final int LASTTRADE_VAR_SLOT = 3;

    private static final int BIDS_INDEX = 8;

    private static final int BIDS_VAR_SLOT = 4;

    private static final int ASKS_INDEX = 9;

    private static final int ASKS_VAR_SLOT = 5;

    private static final int METADATA_INDEX = 10;

    private static final int METADATA_VAR_SLOT = 6;

    private static final int[] REQUIRED_FIELD_INDEXES = new int[] { 0, 1, 2, 3, 4, 5, 8, 9, 10 };

    private final MessageEncoder encoder;

    private MemorySegment segment;

    private long payloadBase;

    private final boolean inline;

    private final boolean[] written;

    private VarFieldWriter varWriter;

    private final BitSetView presenceBits;

    private boolean built;

    private long frameLength;

    private TradeBuilder lastTradeReusableBuilder;

    private LevelBuilder bidsReusableBuilder;

    private LevelBuilder asksReusableBuilder;

    private MetadataEntryBuilder metadataReusableBuilder;

    private OrderBookSnapshotBuilder(MessageEncoder encoder, MemorySegment segment,
            boolean inlineMode) {
        this.inline = inlineMode;
        this.encoder = inline ? encoder : Objects.requireNonNull(encoder, "encoder");
        this.segment = Objects.requireNonNull(segment, "segment");
        this.payloadBase = inline ? 0L : MessageHeader.HEADER_SIZE;
        this.written = new boolean[TOTAL_FIELDS];
        MemorySegment body = segment.asSlice(this.payloadBase, segment.byteSize() - this.payloadBase);
        this.varWriter = new VarFieldWriter(body, OrderBookSnapshotFlyweight.BLOCK_LENGTH - (VAR_FIELD_COUNT * 8), VAR_FIELD_COUNT);
        for (int i = 0; i < VAR_FIELD_COUNT; i++) {
                    this.varWriter.reserveVarField();
                };
        this.presenceBits = new BitSetView();
        this.presenceBits.wrap(segment, this.payloadBase, PRESENCE_BYTES);
        this.presenceBits.clearAll();
    }

    public static OrderBookSnapshotBuilder allocate(MessageEncoder encoder, int capacity) {
        Objects.requireNonNull(encoder, "encoder");
        MemorySegment segment = encoder.acquire(capacity);
        return new OrderBookSnapshotBuilder(encoder, segment, false);
    }

    static OrderBookSnapshotBuilder inline(MemorySegment target) {
        Objects.requireNonNull(target, "target");
        return new OrderBookSnapshotBuilder(null, target, true);
    }

    public void resetInline(MemorySegment target, long offset) {
        if (!inline) {
            throw new IllegalStateException("resetInline() is only valid for inline builders");
        }
        this.segment = Objects.requireNonNull(target, "target");
        this.payloadBase = offset;
        this.built = false;
        this.frameLength = 0;
        for (int i = 0; i < written.length; i++) {
            written[i] = false;
        }
        MemorySegment body = segment.asSlice(this.payloadBase, segment.byteSize() - this.payloadBase);
        this.varWriter = new VarFieldWriter(body, OrderBookSnapshotFlyweight.BLOCK_LENGTH - (VAR_FIELD_COUNT * 8), VAR_FIELD_COUNT);
        for (int i = 0; i < VAR_FIELD_COUNT; i++) {
                    this.varWriter.reserveVarField();
                };
        this.presenceBits.wrap(segment, this.payloadBase, PRESENCE_BYTES);
        this.presenceBits.clearAll();
    }

    private void ensureWritable(int fieldIndex, String fieldName) {
        if (built) {
            throw new IllegalStateException("Builder already finalized");
        }
        if (written[fieldIndex]) {
            throw new IllegalStateException("Field '" + fieldName + "' already written");
        }
    }

    private void markWritten(int fieldIndex) {
        written[fieldIndex] = true;
    }

    private void verifyRequiredFields() {
        for (int idx : REQUIRED_FIELD_INDEXES) {
            if (!written[idx]) {
                throw new IllegalStateException("Missing required field: " + FIELD_NAMES[idx]);
            }
        }
    }

    private long bodySize() {
        if (varWriter == null) {
            return OrderBookSnapshotFlyweight.BLOCK_LENGTH;
        }
        return varWriter.bytesWritten();
    }

    public long frameLength() {
        if (!built) {
            throw new IllegalStateException("Call build() before querying frameLength");
        }
        return this.frameLength;
    }

    public PooledSegment build(short templateId, short schemaVersion) {
        if (built) {
            throw new IllegalStateException("Builder already finalized");
        }
        if (inline) {
            throw new IllegalStateException("Inline builders cannot call build()");
        }
        verifyRequiredFields();
        long payloadSize = bodySize();
        long targetLength = MessageHeader.HEADER_SIZE + payloadSize;
        encoder.getWriter(segment).position(targetLength);
        this.frameLength = encoder.finalizeMessage(segment, templateId, schemaVersion);
        this.built = true;
        return new PooledSegment(segment, encoder.pool());
    }

    final long finishInline() {
        if (!inline) {
            throw new IllegalStateException("finishInline() is only valid for inline builders");
        }
        if (built) {
            throw new IllegalStateException("Builder already finalized");
        }
        verifyRequiredFields();
        long payloadSize = bodySize();
        this.built = true;
        return payloadSize;
    }

    public OrderBookSnapshotBuilder setTimestamp(String value, MemorySegment scratchBuffer) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(TIMESTAMP_INDEX, "timestamp");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        varWriter.writeVarField(TIMESTAMP_VAR_SLOT, value, scratchBuffer);
        markWritten(TIMESTAMP_INDEX);
        return this;
    }

    public OrderBookSnapshotBuilder setVenue(String value, MemorySegment scratchBuffer) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(VENUE_INDEX, "venue");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        varWriter.writeVarField(VENUE_VAR_SLOT, value, scratchBuffer);
        markWritten(VENUE_INDEX);
        return this;
    }

    public OrderBookSnapshotBuilder setSymbol(String value, MemorySegment scratchBuffer) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(SYMBOL_INDEX, "symbol");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        varWriter.writeVarField(SYMBOL_VAR_SLOT, value, scratchBuffer);
        markWritten(SYMBOL_INDEX);
        return this;
    }

    public OrderBookSnapshotBuilder setInstrumentId(int value) {
        ensureWritable(INSTRUMENTID_INDEX, "instrumentId");
        segment.set(Layouts.INT_BE, payloadBase + OrderBookSnapshotFlyweight.INSTRUMENTID_OFFSET, value);
        markWritten(INSTRUMENTID_INDEX);
        return this;
    }

    public OrderBookSnapshotBuilder setSequence(long value) {
        ensureWritable(SEQUENCE_INDEX, "sequence");
        segment.set(Layouts.LONG_BE, payloadBase + OrderBookSnapshotFlyweight.SEQUENCE_OFFSET, value);
        markWritten(SEQUENCE_INDEX);
        return this;
    }

    public OrderBookSnapshotBuilder setIsTrading(boolean value) {
        ensureWritable(ISTRADING_INDEX, "isTrading");
        segment.set(Layouts.BOOLEAN, payloadBase + OrderBookSnapshotFlyweight.ISTRADING_OFFSET, value);
        markWritten(ISTRADING_INDEX);
        return this;
    }

    public OrderBookSnapshotBuilder setTradingStatus(byte value) {
        ensureWritable(TRADINGSTATUS_INDEX, "tradingStatus");
        segment.set(Layouts.BYTE, payloadBase + OrderBookSnapshotFlyweight.TRADINGSTATUS_OFFSET, value);
        markWritten(TRADINGSTATUS_INDEX);
        presenceBits.set(TRADINGSTATUS_OPT_BIT);
        return this;
    }

    public OrderBookSnapshotBuilder setTradingStatus(TradingStatus value) {
        Objects.requireNonNull(value, "value");
        return setTradingStatus((byte) value.id());
    }

    public OrderBookSnapshotBuilder setLastTrade(Consumer<TradeBuilder> encoder) {
        Objects.requireNonNull(encoder, "encoder");
        ensureWritable(LASTTRADE_INDEX, "lastTrade");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(LASTTRADE_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        TradeBuilder nestedBuilder = this.lastTradeReusableBuilder;
        if (nestedBuilder == null) {
            nestedBuilder = TradeBuilder.inline(segment.asSlice(absoluteOffset, segment.byteSize() - absoluteOffset));
            this.lastTradeReusableBuilder = nestedBuilder;
        } else {
            nestedBuilder.resetInline(segment, absoluteOffset);
        }
        encoder.accept(nestedBuilder);
        long nestedSize = nestedBuilder.finishInline();
        handle.finish(nestedSize);
        markWritten(LASTTRADE_INDEX);
        presenceBits.set(LASTTRADE_OPT_BIT);
        return this;
    }

    public OrderBookSnapshotBuilder setLastTrade(TradeWriter writer) {
        Objects.requireNonNull(writer, "writer");
        ensureWritable(LASTTRADE_INDEX, "lastTrade");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(LASTTRADE_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        TradeBuilder nestedBuilder = this.lastTradeReusableBuilder;
        if (nestedBuilder == null) {
            nestedBuilder = TradeBuilder.inline(segment.asSlice(absoluteOffset, segment.byteSize() - absoluteOffset));
            this.lastTradeReusableBuilder = nestedBuilder;
        } else {
            nestedBuilder.resetInline(segment, absoluteOffset);
        }
        writer.writeTo(nestedBuilder);
        long nestedSize = nestedBuilder.finishInline();
        handle.finish(nestedSize);
        markWritten(LASTTRADE_INDEX);
        presenceBits.set(LASTTRADE_OPT_BIT);
        return this;
    }

    /**
     * Sets the repeated bids field with the given count.
     * The consumer is called for each element to populate it.
     * @param count the number of elements
     * @param elementWriter the consumer to populate each element
     * @return this builder for chaining
     */
    public OrderBookSnapshotBuilder setBids(int count, Consumer<LevelBuilder> elementWriter) {
        Objects.requireNonNull(elementWriter, "elementWriter");
        ensureWritable(BIDS_INDEX, "bids");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(BIDS_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        VariableSizeRepeatingGroupBuilder groupBuilder = new VariableSizeRepeatingGroupBuilder();
        groupBuilder.beginWithCount(segment, absoluteOffset, count);
        LevelBuilder nestedBuilder = this.bidsReusableBuilder;
        for (int i = 0; i < count; i++) {
            long elementStart = groupBuilder.beginElement();
            if (nestedBuilder == null) {
                nestedBuilder = LevelBuilder.inline(segment.asSlice(elementStart, segment.byteSize() - elementStart));
                this.bidsReusableBuilder = nestedBuilder;
            } else {
                nestedBuilder.resetInline(segment, elementStart);
            }
            elementWriter.accept(nestedBuilder);
            long nestedSize = nestedBuilder.finishInline();
            groupBuilder.endElement((int) nestedSize);
        }
        int bytesWritten = groupBuilder.finish();
        handle.finish(bytesWritten);
        markWritten(BIDS_INDEX);
        return this;
    }

    /**
     * Sets the repeated bids field with the given count.
     * The writer is called for each element to populate it.
     * @param count the number of elements
     * @param elementWriter the writer to populate each element
     * @return this builder for chaining
     */
    public OrderBookSnapshotBuilder setBids(int count, LevelWriter elementWriter) {
        Objects.requireNonNull(elementWriter, "elementWriter");
        ensureWritable(BIDS_INDEX, "bids");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(BIDS_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        VariableSizeRepeatingGroupBuilder groupBuilder = new VariableSizeRepeatingGroupBuilder();
        groupBuilder.beginWithCount(segment, absoluteOffset, count);
        LevelBuilder nestedBuilder = this.bidsReusableBuilder;
        for (int i = 0; i < count; i++) {
            long elementStart = groupBuilder.beginElement();
            if (nestedBuilder == null) {
                nestedBuilder = LevelBuilder.inline(segment.asSlice(elementStart, segment.byteSize() - elementStart));
                this.bidsReusableBuilder = nestedBuilder;
            } else {
                nestedBuilder.resetInline(segment, elementStart);
            }
            elementWriter.writeTo(nestedBuilder, i);
            long nestedSize = nestedBuilder.finishInline();
            groupBuilder.endElement((int) nestedSize);
        }
        int bytesWritten = groupBuilder.finish();
        handle.finish(bytesWritten);
        markWritten(BIDS_INDEX);
        return this;
    }

    /**
     * Sets the repeated asks field with the given count.
     * The consumer is called for each element to populate it.
     * @param count the number of elements
     * @param elementWriter the consumer to populate each element
     * @return this builder for chaining
     */
    public OrderBookSnapshotBuilder setAsks(int count, Consumer<LevelBuilder> elementWriter) {
        Objects.requireNonNull(elementWriter, "elementWriter");
        ensureWritable(ASKS_INDEX, "asks");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(ASKS_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        VariableSizeRepeatingGroupBuilder groupBuilder = new VariableSizeRepeatingGroupBuilder();
        groupBuilder.beginWithCount(segment, absoluteOffset, count);
        LevelBuilder nestedBuilder = this.asksReusableBuilder;
        for (int i = 0; i < count; i++) {
            long elementStart = groupBuilder.beginElement();
            if (nestedBuilder == null) {
                nestedBuilder = LevelBuilder.inline(segment.asSlice(elementStart, segment.byteSize() - elementStart));
                this.asksReusableBuilder = nestedBuilder;
            } else {
                nestedBuilder.resetInline(segment, elementStart);
            }
            elementWriter.accept(nestedBuilder);
            long nestedSize = nestedBuilder.finishInline();
            groupBuilder.endElement((int) nestedSize);
        }
        int bytesWritten = groupBuilder.finish();
        handle.finish(bytesWritten);
        markWritten(ASKS_INDEX);
        return this;
    }

    /**
     * Sets the repeated asks field with the given count.
     * The writer is called for each element to populate it.
     * @param count the number of elements
     * @param elementWriter the writer to populate each element
     * @return this builder for chaining
     */
    public OrderBookSnapshotBuilder setAsks(int count, LevelWriter elementWriter) {
        Objects.requireNonNull(elementWriter, "elementWriter");
        ensureWritable(ASKS_INDEX, "asks");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(ASKS_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        VariableSizeRepeatingGroupBuilder groupBuilder = new VariableSizeRepeatingGroupBuilder();
        groupBuilder.beginWithCount(segment, absoluteOffset, count);
        LevelBuilder nestedBuilder = this.asksReusableBuilder;
        for (int i = 0; i < count; i++) {
            long elementStart = groupBuilder.beginElement();
            if (nestedBuilder == null) {
                nestedBuilder = LevelBuilder.inline(segment.asSlice(elementStart, segment.byteSize() - elementStart));
                this.asksReusableBuilder = nestedBuilder;
            } else {
                nestedBuilder.resetInline(segment, elementStart);
            }
            elementWriter.writeTo(nestedBuilder, i);
            long nestedSize = nestedBuilder.finishInline();
            groupBuilder.endElement((int) nestedSize);
        }
        int bytesWritten = groupBuilder.finish();
        handle.finish(bytesWritten);
        markWritten(ASKS_INDEX);
        return this;
    }

    /**
     * Sets the repeated metadata field with the given count.
     * The consumer is called for each element to populate it.
     * @param count the number of elements
     * @param elementWriter the consumer to populate each element
     * @return this builder for chaining
     */
    public OrderBookSnapshotBuilder setMetadata(int count,
            Consumer<MetadataEntryBuilder> elementWriter) {
        Objects.requireNonNull(elementWriter, "elementWriter");
        ensureWritable(METADATA_INDEX, "metadata");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(METADATA_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        VariableSizeRepeatingGroupBuilder groupBuilder = new VariableSizeRepeatingGroupBuilder();
        groupBuilder.beginWithCount(segment, absoluteOffset, count);
        MetadataEntryBuilder nestedBuilder = this.metadataReusableBuilder;
        for (int i = 0; i < count; i++) {
            long elementStart = groupBuilder.beginElement();
            if (nestedBuilder == null) {
                nestedBuilder = MetadataEntryBuilder.inline(segment.asSlice(elementStart, segment.byteSize() - elementStart));
                this.metadataReusableBuilder = nestedBuilder;
            } else {
                nestedBuilder.resetInline(segment, elementStart);
            }
            elementWriter.accept(nestedBuilder);
            long nestedSize = nestedBuilder.finishInline();
            groupBuilder.endElement((int) nestedSize);
        }
        int bytesWritten = groupBuilder.finish();
        handle.finish(bytesWritten);
        markWritten(METADATA_INDEX);
        return this;
    }

    /**
     * Sets the repeated metadata field with the given count.
     * The writer is called for each element to populate it.
     * @param count the number of elements
     * @param elementWriter the writer to populate each element
     * @return this builder for chaining
     */
    public OrderBookSnapshotBuilder setMetadata(int count, MetadataEntryWriter elementWriter) {
        Objects.requireNonNull(elementWriter, "elementWriter");
        ensureWritable(METADATA_INDEX, "metadata");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(METADATA_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        VariableSizeRepeatingGroupBuilder groupBuilder = new VariableSizeRepeatingGroupBuilder();
        groupBuilder.beginWithCount(segment, absoluteOffset, count);
        MetadataEntryBuilder nestedBuilder = this.metadataReusableBuilder;
        for (int i = 0; i < count; i++) {
            long elementStart = groupBuilder.beginElement();
            if (nestedBuilder == null) {
                nestedBuilder = MetadataEntryBuilder.inline(segment.asSlice(elementStart, segment.byteSize() - elementStart));
                this.metadataReusableBuilder = nestedBuilder;
            } else {
                nestedBuilder.resetInline(segment, elementStart);
            }
            elementWriter.writeTo(nestedBuilder, i);
            long nestedSize = nestedBuilder.finishInline();
            groupBuilder.endElement((int) nestedSize);
        }
        int bytesWritten = groupBuilder.finish();
        handle.finish(bytesWritten);
        markWritten(METADATA_INDEX);
        return this;
    }
}
