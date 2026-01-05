package express.mvp.myra.codec.examples.generated.portfolio;

import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.roray.ffm.utils.memory.BitSetView;
import express.mvp.roray.ffm.utils.memory.Layouts;
import express.mvp.roray.ffm.utils.memory.PooledSegment;
import express.mvp.roray.ffm.utils.memory.VarFieldWriter;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Single-pass, write-once builder for {@code Leg}.
 */
public final class LegBuilder {
    private static final int TOTAL_FIELDS = 3;

    private static final int VAR_FIELD_COUNT = 0;

    private static final int PRESENCE_BYTES = 0;

    private static final String[] FIELD_NAMES = {
        "symbol",
        "side",
        "quantity"
    };

    private static final int SYMBOL_INDEX = 0;

    private static final int SYMBOL_FIXED_CAPACITY = 8;

    private static final int SIDE_INDEX = 1;

    private static final int QUANTITY_INDEX = 2;

    private static final int[] REQUIRED_FIELD_INDEXES = new int[] { 0, 1, 2 };

    private final MessageEncoder encoder;

    private MemorySegment segment;

    private long payloadBase;

    private final boolean inline;

    private final boolean[] written;

    private VarFieldWriter varWriter;

    private final BitSetView presenceBits;

    private boolean built;

    private long frameLength;

    private LegBuilder(MessageEncoder encoder, MemorySegment segment, boolean inlineMode) {
        this.inline = inlineMode;
        this.encoder = inline ? encoder : Objects.requireNonNull(encoder, "encoder");
        this.segment = Objects.requireNonNull(segment, "segment");
        this.payloadBase = inline ? 0L : MessageHeader.HEADER_SIZE;
        this.written = new boolean[TOTAL_FIELDS];
        this.varWriter = null;
        this.presenceBits = null;
    }

    public static LegBuilder allocate(MessageEncoder encoder, int capacity) {
        Objects.requireNonNull(encoder, "encoder");
        MemorySegment segment = encoder.acquire(capacity);
        return new LegBuilder(encoder, segment, false);
    }

    static LegBuilder inline(MemorySegment target) {
        Objects.requireNonNull(target, "target");
        return new LegBuilder(null, target, true);
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
            return LegFlyweight.BLOCK_LENGTH;
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

    public LegBuilder setSymbol(String value, MemorySegment scratchBuffer) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(SYMBOL_INDEX, "symbol");
        int encodedLength = VarFieldWriter.utf8Length(value);
        if (encodedLength > SYMBOL_FIXED_CAPACITY) {
            throw new IllegalArgumentException("Field '" + "symbol" + "' exceeds fixed_capacity of " + 8);
        }
        VarFieldWriter.encodeUtf8(value, scratchBuffer);
        long base = payloadBase + LegFlyweight.SYMBOL_OFFSET;
        segment.set(Layouts.INT_BE, base, encodedLength);
        MemorySegment.copy(scratchBuffer, 0, segment, base + 4, encodedLength);
        if (encodedLength < SYMBOL_FIXED_CAPACITY) {
            segment.asSlice(base + 4 + encodedLength, SYMBOL_FIXED_CAPACITY - encodedLength).fill((byte) 0);
        }
        markWritten(SYMBOL_INDEX);
        return this;
    }

    public LegBuilder setSide(byte value) {
        ensureWritable(SIDE_INDEX, "side");
        segment.set(Layouts.BYTE, payloadBase + LegFlyweight.SIDE_OFFSET, value);
        markWritten(SIDE_INDEX);
        return this;
    }

    public LegBuilder setSide(Side value) {
        Objects.requireNonNull(value, "value");
        return setSide((byte) value.id());
    }

    public LegBuilder setQuantity(int value) {
        ensureWritable(QUANTITY_INDEX, "quantity");
        segment.set(Layouts.INT_BE, payloadBase + LegFlyweight.QUANTITY_OFFSET, value);
        markWritten(QUANTITY_INDEX);
        return this;
    }
}
