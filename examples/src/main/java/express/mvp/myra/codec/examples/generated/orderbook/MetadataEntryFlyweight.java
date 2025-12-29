package express.mvp.myra.codec.examples.generated.orderbook;

import express.mvp.roray.ffm.utils.memory.BinaryWriter;
import express.mvp.roray.ffm.utils.memory.FlyweightAccessor;
import express.mvp.roray.ffm.utils.memory.Layouts;
import express.mvp.roray.ffm.utils.memory.Utf8View;
import java.lang.Override;
import java.lang.foreign.MemorySegment;

/**
 * Auto-generated, zero-copy flyweight for the MetadataEntry message.
 *
 * Provides direct access to binary data without deserialization overhead.
 * Thread-safe for read operations when properly synchronized.
 *
 * @see FlyweightAccessor
 */
public final class MetadataEntryFlyweight implements FlyweightAccessor {
    public static final int KEY_OFFSET = 0;

    public static final int VALUE_OFFSET = 8;

    public static final int TEMPLATE_ID = 3;

    /**
     * Schema version in wire format: 1.0
     */
    public static final short SCHEMA_VERSION = (short) 256;

    public static final int BLOCK_LENGTH = 16;

    private MemorySegment segment;

    private long offset;

    private final Utf8View keyView = new Utf8View();

    private final Utf8View valueView = new Utf8View();

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

    public Utf8View getKey() {
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + KEY_OFFSET);
        final int dataLength = segment.get(Layouts.INT_BE, this.offset + KEY_OFFSET + 4);
        this.keyView.wrap(this.segment, this.offset + relativeOffset, dataLength);
        return this.keyView;
    }

    public Utf8View getValue() {
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + VALUE_OFFSET);
        final int dataLength = segment.get(Layouts.INT_BE, this.offset + VALUE_OFFSET + 4);
        this.valueView.wrap(this.segment, this.offset + relativeOffset, dataLength);
        return this.valueView;
    }

    @Override
    public void writeTo(BinaryWriter writer) {
        Utf8View keyViewTmp = this.getKey();
        byte[] keyBytesTmp = new byte[(int)keyViewTmp.byteSize()];
        MemorySegment.copy(keyViewTmp.segment(), keyViewTmp.offset(), MemorySegment.ofArray(keyBytesTmp), 0, keyViewTmp.byteSize());
        writer.writeBytes(keyBytesTmp);
        Utf8View valueViewTmp = this.getValue();
        byte[] valueBytesTmp = new byte[(int)valueViewTmp.byteSize()];
        MemorySegment.copy(valueViewTmp.segment(), valueViewTmp.offset(), MemorySegment.ofArray(valueBytesTmp), 0, valueViewTmp.byteSize());
        writer.writeBytes(valueBytesTmp);
    }
}
