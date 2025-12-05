package express.mvp.myra.codec.runtime.struct;

import static express.mvp.roray.utils.memory.Layouts.*;

import express.mvp.roray.utils.memory.BinaryWriter;
import express.mvp.roray.utils.memory.FlyweightAccessor;
import java.lang.foreign.MemorySegment;

/**
 * A flyweight for the standard 16-byte MyraCodec message header. This is a foundational,
 * fixed-structure component of the MyraCodec runtime.
 *
 * <h2>Wire Format</h2>
 * <pre>
 * +-------------+-------------+----------------+-------+----------+----------+
 * | Frame Length| Template ID | Schema Version | Flags | Reserved | Checksum |
 * |  (4 bytes)  |  (2 bytes)  |    (2 bytes)   |(1 byte)|(3 bytes)| (4 bytes)|
 * +-------------+-------------+----------------+-------+----------+----------+
 * </pre>
 *
 * <h2>Thread Safety</h2>
 * <p><b>This class is NOT thread-safe.</b> Each thread should use its own {@code MessageHeader}
 * instance. The flyweight pattern is designed for single-threaded access with instance reuse
 * via {@link #wrap(MemorySegment, long)}.
 *
 * <p>The underlying {@code MemorySegment} may be shared, but concurrent access to the same
 * memory region requires external synchronization.
 *
 * <p><b>Typical usage pattern:</b>
 * <pre>{@code
 * // Reusable flyweight (per-thread)
 * MessageHeader header = new MessageHeader();
 *
 * // Wrap around received data
 * header.wrap(receivedSegment, 0);
 * int templateId = header.getTemplateId();
 * int frameLen = header.getFrameLength();
 * }</pre>
 */
public final class MessageHeader implements FlyweightAccessor {

    // --- Field Offsets and Layout Constants ---
    public static final int FRAME_LENGTH_OFFSET = 0;
    public static final int TEMPLATE_ID_OFFSET = 4;
    public static final int SCHEMA_VERSION_OFFSET = 6;
    public static final int FLAGS_OFFSET = 8;
    public static final int CHECKSUM_OFFSET = 12;
    public static final int HEADER_SIZE = 16;

    private MemorySegment segment;
    private long offset;

    @Override
    public void wrap(MemorySegment segment, long offset) {
        this.segment = segment;
        this.offset = offset;
    }

    @Override
    public MemorySegment segment() {
        return this.segment;
    }

    @Override
    public int byteSize() {
        return HEADER_SIZE;
    }

    @Override
    public boolean isWrapped() {
        return this.segment != null;
    }

    /**
     * Writes the current state of this flyweight to the given writer. This is used to copy or
     * serialize the header.
     */
    @Override
    public void writeTo(BinaryWriter writer) {
        writer.writeIntBE(getFrameLength());
        writer.writeShortBE(getTemplateId());
        writer.writeShortBE(getSchemaVersion());
        writer.writeByte(getFlags());
        writer.skip(3); // Skip the 3 reserved bytes
        writer.writeIntBE(getChecksum());
    }

    // --- Accessor (Getter) Methods ---

    public int getFrameLength() {
        return segment.get(INT_BE, offset + FRAME_LENGTH_OFFSET);
    }

    public short getTemplateId() {
        return segment.get(SHORT_BE, offset + TEMPLATE_ID_OFFSET);
    }

    public short getSchemaVersion() {
        return segment.get(SHORT_BE, offset + SCHEMA_VERSION_OFFSET);
    }

    public byte getFlags() {
        return segment.get(BYTE, offset + FLAGS_OFFSET);
    }

    public int getChecksum() {
        return segment.get(INT_BE, offset + CHECKSUM_OFFSET);
    }

    // --- Mutator (Setter) Methods ---

    public void setFrameLength(int value) {
        segment.set(INT_BE, offset + FRAME_LENGTH_OFFSET, value);
    }

    public void setTemplateId(short value) {
        segment.set(SHORT_BE, offset + TEMPLATE_ID_OFFSET, value);
    }

    public void setSchemaVersion(short value) {
        segment.set(SHORT_BE, offset + SCHEMA_VERSION_OFFSET, value);
    }

    public void setFlags(byte value) {
        segment.set(BYTE, offset + FLAGS_OFFSET, value);
    }

    public void setChecksum(int value) {
        segment.set(INT_BE, offset + CHECKSUM_OFFSET, value);
    }

    @Override
    public void validate() {
        if (!isWrapped()) {
            throw new IllegalStateException("MessageHeader is not wrapped");
        }
        long remaining = segment.byteSize() - offset;
        if (remaining < HEADER_SIZE) {
            throw new IllegalStateException(
                    "MessageHeader requires "
                            + HEADER_SIZE
                            + " bytes but only "
                            + remaining
                            + " available");
        }
    }
}
