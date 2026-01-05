package express.mvp.myra.codec.examples.generated.telemetry;

import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.myra.codec.runtime.struct.RepeatingGroupBuilder;
import express.mvp.roray.ffm.utils.memory.BitSetView;
import express.mvp.roray.ffm.utils.memory.Layouts;
import express.mvp.roray.ffm.utils.memory.PooledSegment;
import express.mvp.roray.ffm.utils.memory.VarFieldWriter;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Single-pass, write-once builder for {@code Telemetry}.
 */
public final class TelemetryBuilder {
    private static final int TOTAL_FIELDS = 6;

    private static final int VAR_FIELD_COUNT = 3;

    private static final int PRESENCE_BYTES = 1;

    private static final String[] FIELD_NAMES = {
        "deviceId",
        "sequence",
        "health",
        "latencies",
        "note",
        "payload"
    };

    private static final int NOTE_OPT_BIT = 0;

    private static final int PAYLOAD_OPT_BIT = 1;

    private static final int DEVICEID_INDEX = 0;

    private static final int DEVICEID_FIXED_CAPACITY = 16;

    private static final int SEQUENCE_INDEX = 1;

    private static final int HEALTH_INDEX = 2;

    private static final int LATENCIES_INDEX = 3;

    private static final int LATENCIES_VAR_SLOT = 0;

    private static final int NOTE_INDEX = 4;

    private static final int NOTE_VAR_SLOT = 1;

    private static final int PAYLOAD_INDEX = 5;

    private static final int PAYLOAD_VAR_SLOT = 2;

    private static final int[] REQUIRED_FIELD_INDEXES = new int[] { 0, 1, 2, 3 };

    private final MessageEncoder encoder;

    private MemorySegment segment;

    private long payloadBase;

    private final boolean inline;

    private final boolean[] written;

    private VarFieldWriter varWriter;

    private final BitSetView presenceBits;

    private boolean built;

    private long frameLength;

    private TelemetryBuilder(MessageEncoder encoder, MemorySegment segment, boolean inlineMode) {
        this.inline = inlineMode;
        this.encoder = inline ? encoder : Objects.requireNonNull(encoder, "encoder");
        this.segment = Objects.requireNonNull(segment, "segment");
        this.payloadBase = inline ? 0L : MessageHeader.HEADER_SIZE;
        this.written = new boolean[TOTAL_FIELDS];
        MemorySegment body = segment.asSlice(this.payloadBase, segment.byteSize() - this.payloadBase);
        this.varWriter = new VarFieldWriter(body, TelemetryFlyweight.BLOCK_LENGTH - (VAR_FIELD_COUNT * 8), VAR_FIELD_COUNT);
        for (int i = 0; i < VAR_FIELD_COUNT; i++) {
                    this.varWriter.reserveVarField();
                };
        this.presenceBits = new BitSetView();
        this.presenceBits.wrap(segment, this.payloadBase, PRESENCE_BYTES);
        this.presenceBits.clearAll();
    }

    public static TelemetryBuilder allocate(MessageEncoder encoder, int capacity) {
        Objects.requireNonNull(encoder, "encoder");
        MemorySegment segment = encoder.acquire(capacity);
        return new TelemetryBuilder(encoder, segment, false);
    }

    static TelemetryBuilder inline(MemorySegment target) {
        Objects.requireNonNull(target, "target");
        return new TelemetryBuilder(null, target, true);
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
        this.varWriter = new VarFieldWriter(body, TelemetryFlyweight.BLOCK_LENGTH - (VAR_FIELD_COUNT * 8), VAR_FIELD_COUNT);
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
            return TelemetryFlyweight.BLOCK_LENGTH;
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

    public TelemetryBuilder setDeviceId(String value, MemorySegment scratchBuffer) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(DEVICEID_INDEX, "deviceId");
        int encodedLength = VarFieldWriter.utf8Length(value);
        if (encodedLength > DEVICEID_FIXED_CAPACITY) {
            throw new IllegalArgumentException("Field '" + "deviceId" + "' exceeds fixed_capacity of " + 16);
        }
        VarFieldWriter.encodeUtf8(value, scratchBuffer);
        long base = payloadBase + TelemetryFlyweight.DEVICEID_OFFSET;
        segment.set(Layouts.INT_BE, base, encodedLength);
        MemorySegment.copy(scratchBuffer, 0, segment, base + 4, encodedLength);
        if (encodedLength < DEVICEID_FIXED_CAPACITY) {
            segment.asSlice(base + 4 + encodedLength, DEVICEID_FIXED_CAPACITY - encodedLength).fill((byte) 0);
        }
        markWritten(DEVICEID_INDEX);
        return this;
    }

    public TelemetryBuilder setSequence(int value) {
        ensureWritable(SEQUENCE_INDEX, "sequence");
        segment.set(Layouts.INT_BE, payloadBase + TelemetryFlyweight.SEQUENCE_OFFSET, value);
        markWritten(SEQUENCE_INDEX);
        return this;
    }

    public TelemetryBuilder setHealth(byte value) {
        ensureWritable(HEALTH_INDEX, "health");
        segment.set(Layouts.BYTE, payloadBase + TelemetryFlyweight.HEALTH_OFFSET, value);
        markWritten(HEALTH_INDEX);
        return this;
    }

    public TelemetryBuilder setHealth(Health value) {
        Objects.requireNonNull(value, "value");
        return setHealth((byte) value.id());
    }

    /**
     * Sets the repeated latencies field with the given values.
     * @param values the array of values to write
     * @return this builder for chaining
     */
    public TelemetryBuilder setLatencies(long[] values) {
        Objects.requireNonNull(values, "values");
        ensureWritable(LATENCIES_INDEX, "latencies");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(LATENCIES_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        RepeatingGroupBuilder groupBuilder = new RepeatingGroupBuilder(8);
        groupBuilder.wrap(segment, absoluteOffset);
        for (long value : values) {
            groupBuilder.addLong(value);
        }
        int bytesWritten = groupBuilder.finish();
        handle.finish(bytesWritten);
        markWritten(LATENCIES_INDEX);
        return this;
    }

    public TelemetryBuilder setNote(String value, MemorySegment scratchBuffer) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(NOTE_INDEX, "note");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        varWriter.writeVarField(NOTE_VAR_SLOT, value, scratchBuffer);
        markWritten(NOTE_INDEX);
        presenceBits.set(NOTE_OPT_BIT);
        return this;
    }

    public TelemetryBuilder setPayload(MemorySegment source) {
        Objects.requireNonNull(source, "source");
        ensureWritable(PAYLOAD_INDEX, "payload");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        varWriter.writeVarField(PAYLOAD_VAR_SLOT, source);
        markWritten(PAYLOAD_INDEX);
        presenceBits.set(PAYLOAD_OPT_BIT);
        return this;
    }
}
