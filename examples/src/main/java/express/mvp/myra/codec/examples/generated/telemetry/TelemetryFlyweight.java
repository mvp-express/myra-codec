package express.mvp.myra.codec.examples.generated.telemetry;

import express.mvp.myra.codec.runtime.struct.RepeatingGroupIterator;
import express.mvp.roray.ffm.utils.memory.BinaryWriter;
import express.mvp.roray.ffm.utils.memory.BitSetView;
import express.mvp.roray.ffm.utils.memory.FlyweightAccessor;
import express.mvp.roray.ffm.utils.memory.Layouts;
import express.mvp.roray.ffm.utils.memory.Utf8View;
import java.lang.Override;
import java.lang.foreign.MemorySegment;

/**
 * Auto-generated, zero-copy flyweight for the Telemetry message.
 *
 * Provides direct access to binary data without deserialization overhead.
 * Thread-safe for read operations when properly synchronized.
 *
 * @see FlyweightAccessor
 */
public final class TelemetryFlyweight implements FlyweightAccessor {
    public static final int PRESENCE_BYTES = 1;

    public static final int NOTE_OPT_BIT = 0;

    public static final int PAYLOAD_OPT_BIT = 1;

    public static final int DEVICEID_OFFSET = 1;

    public static final int SEQUENCE_OFFSET = 21;

    public static final int HEALTH_OFFSET = 25;

    public static final int LATENCIES_OFFSET = 26;

    public static final int NOTE_OFFSET = 34;

    public static final int PAYLOAD_OFFSET = 42;

    public static final int TEMPLATE_ID = 1;

    /**
     * Schema version in wire format: 1.0
     */
    public static final short SCHEMA_VERSION = (short) 256;

    public static final int BLOCK_LENGTH = 50;

    private MemorySegment segment;

    private long offset;

    private final BitSetView presenceBits = new BitSetView();

    private final Utf8View deviceIdView = new Utf8View();

    private final RepeatingGroupIterator latenciesIterator = new RepeatingGroupIterator(8);

    private final Utf8View noteView = new Utf8View();

    private final Utf8View payloadView = new Utf8View();

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

    public Utf8View getDeviceId() {
        final long base = this.offset + DEVICEID_OFFSET;
        final int dataLength = segment.get(Layouts.INT_BE, base);
        this.deviceIdView.wrap(this.segment, base + 4, dataLength);
        return this.deviceIdView;
    }

    public int getSequence() {
        return segment.get(Layouts.INT_BE, this.offset + SEQUENCE_OFFSET);
    }

    public void setSequence(int value) {
        segment.set(Layouts.INT_BE, this.offset + SEQUENCE_OFFSET, value);
    }

    public byte getHealth() {
        return segment.get(Layouts.BYTE, this.offset + HEALTH_OFFSET);
    }

    public void setHealth(byte value) {
        segment.set(Layouts.BYTE, this.offset + HEALTH_OFFSET, value);
    }

    public int getLatenciesCount() {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + LATENCIES_OFFSET);
        this.latenciesIterator.wrap(this.segment, dataOffset);
        return this.latenciesIterator.count();
    }

    /**
     * Returns the element at the given index.
     * @param index the element index (0-based)
     * @return the element value
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public long getLatenciesAt(int index) {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + LATENCIES_OFFSET);
        this.latenciesIterator.wrap(this.segment, dataOffset);
        return this.latenciesIterator.getLongAt(index);
    }

    public Utf8View getNote() {
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + NOTE_OFFSET);
        final int dataLength = segment.get(Layouts.INT_BE, this.offset + NOTE_OFFSET + 4);
        this.noteView.wrap(this.segment, this.offset + relativeOffset, dataLength);
        return this.noteView;
    }

    public Utf8View getPayload() {
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + PAYLOAD_OFFSET);
        final int dataLength = segment.get(Layouts.INT_BE, this.offset + PAYLOAD_OFFSET + 4);
        this.payloadView.wrap(this.segment, this.offset + relativeOffset, dataLength);
        return this.payloadView;
    }

    public boolean hasNote() {
        return this.presenceBits.get(0);
    }

    public boolean hasPayload() {
        return this.presenceBits.get(1);
    }

    @Override
    public void writeTo(BinaryWriter writer) {
        Utf8View deviceIdViewTmp = this.getDeviceId();
        byte[] deviceIdBytesTmp = new byte[(int)deviceIdViewTmp.byteSize()];
        MemorySegment.copy(deviceIdViewTmp.segment(), deviceIdViewTmp.offset(), MemorySegment.ofArray(deviceIdBytesTmp), 0, deviceIdViewTmp.byteSize());
        writer.writeBytes(deviceIdBytesTmp);
        writer.writeIntBE(this.getSequence());
        writer.writeByte(this.getHealth());
        final int latenciesRelativeOffset = this.segment.get(Layouts.INT_BE, this.offset + LATENCIES_OFFSET);
        final int latenciesDataLength = this.segment.get(Layouts.INT_BE, this.offset + LATENCIES_OFFSET + 4);
        writer.writeVarInt(latenciesDataLength);
        writer.writeSegmentRaw(this.segment, this.offset + latenciesRelativeOffset, latenciesDataLength);
        Utf8View noteViewTmp = this.getNote();
        byte[] noteBytesTmp = new byte[(int)noteViewTmp.byteSize()];
        MemorySegment.copy(noteViewTmp.segment(), noteViewTmp.offset(), MemorySegment.ofArray(noteBytesTmp), 0, noteViewTmp.byteSize());
        writer.writeBytes(noteBytesTmp);
        Utf8View payloadViewTmp = this.getPayload();
        byte[] payloadBytesTmp = new byte[(int)payloadViewTmp.byteSize()];
        MemorySegment.copy(payloadViewTmp.segment(), payloadViewTmp.offset(), MemorySegment.ofArray(payloadBytesTmp), 0, payloadViewTmp.byteSize());
        writer.writeBytes(payloadBytesTmp);
    }
}
