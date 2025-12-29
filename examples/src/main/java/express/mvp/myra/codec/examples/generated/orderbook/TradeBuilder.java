package express.mvp.myra.codec.examples.generated.orderbook;

import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.roray.ffm.utils.memory.PooledSegment;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.roray.ffm.utils.memory.BitSetView;
import express.mvp.roray.ffm.utils.memory.Layouts;
import express.mvp.roray.ffm.utils.memory.VarFieldWriter;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Single-pass, write-once builder for {@code Trade}.
 */
public final class TradeBuilder {
    private static final int TOTAL_FIELDS = 3;

    private static final int VAR_FIELD_COUNT = 1;

    private static final int PRESENCE_BYTES = 1;

    private static final String[] FIELD_NAMES = {
        "priceNanos",
        "size",
        "aggressor"
    };

    private static final int AGGRESSOR_OPT_BIT = 0;

    private static final int PRICENANOS_INDEX = 0;

    private static final int SIZE_INDEX = 1;

    private static final int AGGRESSOR_INDEX = 2;

    private static final int AGGRESSOR_VAR_SLOT = 0;

    private static final int[] REQUIRED_FIELD_INDEXES = new int[] { 0, 1 };

    private final MessageEncoder encoder;

    private final MemorySegment segment;

    private final long payloadBase;

    private final boolean inline;

    private final boolean[] written;

    private final VarFieldWriter varWriter;

    private final BitSetView presenceBits;

    private boolean built;

    private long frameLength;

    private TradeBuilder(MessageEncoder encoder, MemorySegment segment, boolean inlineMode) {
        this.inline = inlineMode;
        this.encoder = inline ? encoder : Objects.requireNonNull(encoder, "encoder");
        this.segment = Objects.requireNonNull(segment, "segment");
        this.payloadBase = inline ? 0L : MessageHeader.HEADER_SIZE;
        this.written = new boolean[TOTAL_FIELDS];
        MemorySegment body = segment.asSlice(this.payloadBase, segment.byteSize() - this.payloadBase);
        this.varWriter = new VarFieldWriter(body, TradeFlyweight.BLOCK_LENGTH - (VAR_FIELD_COUNT * 8), VAR_FIELD_COUNT);
        for (int i = 0; i < VAR_FIELD_COUNT; i++) {
                    this.varWriter.reserveVarField();
                };
        this.presenceBits = new BitSetView();
        this.presenceBits.wrap(segment, this.payloadBase, PRESENCE_BYTES);
        this.presenceBits.clearAll();
    }

    public static TradeBuilder allocate(MessageEncoder encoder, int capacity) {
        Objects.requireNonNull(encoder, "encoder");
        MemorySegment segment = encoder.acquire(capacity);
        return new TradeBuilder(encoder, segment, false);
    }

    static TradeBuilder inline(MemorySegment target) {
        Objects.requireNonNull(target, "target");
        return new TradeBuilder(null, target, true);
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
            return TradeFlyweight.BLOCK_LENGTH;
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

    public TradeBuilder setPriceNanos(long value) {
        ensureWritable(PRICENANOS_INDEX, "priceNanos");
        segment.set(Layouts.LONG_BE, payloadBase + TradeFlyweight.PRICENANOS_OFFSET, value);
        markWritten(PRICENANOS_INDEX);
        return this;
    }

    public TradeBuilder setSize(int value) {
        ensureWritable(SIZE_INDEX, "size");
        segment.set(Layouts.INT_BE, payloadBase + TradeFlyweight.SIZE_OFFSET, value);
        markWritten(SIZE_INDEX);
        return this;
    }

    public TradeBuilder setAggressor(String value, MemorySegment scratchBuffer) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(AGGRESSOR_INDEX, "aggressor");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        varWriter.writeVarField(AGGRESSOR_VAR_SLOT, value, scratchBuffer);
        markWritten(AGGRESSOR_INDEX);
        presenceBits.set(AGGRESSOR_OPT_BIT);
        return this;
    }
}
