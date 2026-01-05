package express.mvp.myra.codec.examples.generated.portfolio;

import express.mvp.roray.ffm.utils.memory.BinaryWriter;
import express.mvp.roray.ffm.utils.memory.FlyweightAccessor;
import express.mvp.roray.ffm.utils.memory.Layouts;
import express.mvp.roray.ffm.utils.memory.Utf8View;
import java.lang.Override;
import java.lang.foreign.MemorySegment;

/**
 * Auto-generated, zero-copy flyweight for the Leg message.
 *
 * Provides direct access to binary data without deserialization overhead.
 * Thread-safe for read operations when properly synchronized.
 *
 * @see FlyweightAccessor
 */
public final class LegFlyweight implements FlyweightAccessor {
    public static final int SYMBOL_OFFSET = 0;

    public static final int SIDE_OFFSET = 12;

    public static final int QUANTITY_OFFSET = 13;

    public static final int TEMPLATE_ID = 1;

    /**
     * Schema version in wire format: 1.0
     */
    public static final short SCHEMA_VERSION = (short) 256;

    public static final int BLOCK_LENGTH = 17;

    private MemorySegment segment;

    private long offset;

    private final Utf8View symbolView = new Utf8View();

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

    public Utf8View getSymbol() {
        final long base = this.offset + SYMBOL_OFFSET;
        final int dataLength = segment.get(Layouts.INT_BE, base);
        this.symbolView.wrap(this.segment, base + 4, dataLength);
        return this.symbolView;
    }

    public byte getSide() {
        return segment.get(Layouts.BYTE, this.offset + SIDE_OFFSET);
    }

    public void setSide(byte value) {
        segment.set(Layouts.BYTE, this.offset + SIDE_OFFSET, value);
    }

    public int getQuantity() {
        return segment.get(Layouts.INT_BE, this.offset + QUANTITY_OFFSET);
    }

    public void setQuantity(int value) {
        segment.set(Layouts.INT_BE, this.offset + QUANTITY_OFFSET, value);
    }

    @Override
    public void writeTo(BinaryWriter writer) {
        Utf8View symbolViewTmp = this.getSymbol();
        byte[] symbolBytesTmp = new byte[(int)symbolViewTmp.byteSize()];
        MemorySegment.copy(symbolViewTmp.segment(), symbolViewTmp.offset(), MemorySegment.ofArray(symbolBytesTmp), 0, symbolViewTmp.byteSize());
        writer.writeBytes(symbolBytesTmp);
        writer.writeByte(this.getSide());
        writer.writeIntBE(this.getQuantity());
    }
}
