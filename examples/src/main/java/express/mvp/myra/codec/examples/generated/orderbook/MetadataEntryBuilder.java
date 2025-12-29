package express.mvp.myra.codec.examples.generated.orderbook;

import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.roray.ffm.utils.memory.PooledSegment;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.roray.ffm.utils.memory.BitSetView;
import express.mvp.roray.ffm.utils.memory.VarFieldWriter;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Single-pass, write-once builder for {@code MetadataEntry}.
 */
public final class MetadataEntryBuilder {
    private static final int TOTAL_FIELDS = 2;

    private static final int VAR_FIELD_COUNT = 2;

    private static final int PRESENCE_BYTES = 0;

    private static final String[] FIELD_NAMES = {
        "key",
        "value"
    };

    private static final int KEY_INDEX = 0;

    private static final int KEY_VAR_SLOT = 0;

    private static final int VALUE_INDEX = 1;

    private static final int VALUE_VAR_SLOT = 1;

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

    private MetadataEntryBuilder(MessageEncoder encoder, MemorySegment segment,
            boolean inlineMode) {
        this.inline = inlineMode;
        this.encoder = inline ? encoder : Objects.requireNonNull(encoder, "encoder");
        this.segment = Objects.requireNonNull(segment, "segment");
        this.payloadBase = inline ? 0L : MessageHeader.HEADER_SIZE;
        this.written = new boolean[TOTAL_FIELDS];
        MemorySegment body = segment.asSlice(this.payloadBase, segment.byteSize() - this.payloadBase);
        this.varWriter = new VarFieldWriter(body, MetadataEntryFlyweight.BLOCK_LENGTH - (VAR_FIELD_COUNT * 8), VAR_FIELD_COUNT);
        for (int i = 0; i < VAR_FIELD_COUNT; i++) {
                    this.varWriter.reserveVarField();
                };
        this.presenceBits = null;
    }

    public static MetadataEntryBuilder allocate(MessageEncoder encoder, int capacity) {
        Objects.requireNonNull(encoder, "encoder");
        MemorySegment segment = encoder.acquire(capacity);
        return new MetadataEntryBuilder(encoder, segment, false);
    }

    static MetadataEntryBuilder inline(MemorySegment target) {
        Objects.requireNonNull(target, "target");
        return new MetadataEntryBuilder(null, target, true);
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
            return MetadataEntryFlyweight.BLOCK_LENGTH;
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

    public MetadataEntryBuilder setKey(String value, MemorySegment scratchBuffer) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(KEY_INDEX, "key");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        varWriter.writeVarField(KEY_VAR_SLOT, value, scratchBuffer);
        markWritten(KEY_INDEX);
        return this;
    }

    public MetadataEntryBuilder setValue(String value, MemorySegment scratchBuffer) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(VALUE_INDEX, "value");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        varWriter.writeVarField(VALUE_VAR_SLOT, value, scratchBuffer);
        markWritten(VALUE_INDEX);
        return this;
    }
}
