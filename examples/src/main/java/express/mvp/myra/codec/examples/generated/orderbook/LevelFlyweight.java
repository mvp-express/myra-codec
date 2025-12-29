package express.mvp.myra.codec.examples.generated.orderbook;

import express.mvp.roray.ffm.utils.memory.BinaryWriter;
import express.mvp.roray.ffm.utils.memory.BitSetView;
import express.mvp.roray.ffm.utils.memory.FlyweightAccessor;
import express.mvp.roray.ffm.utils.memory.Layouts;
import java.lang.Override;
import java.lang.foreign.MemorySegment;

/**
 * Auto-generated, zero-copy flyweight for the Level message.
 *
 * Provides direct access to binary data without deserialization overhead.
 * Thread-safe for read operations when properly synchronized.
 *
 * @see FlyweightAccessor
 */
public final class LevelFlyweight implements FlyweightAccessor {
    public static final int PRESENCE_BYTES = 1;

    public static final int MAKER_OPT_BIT = 0;

    public static final int PRICENANOS_OFFSET = 1;

    public static final int SIZE_OFFSET = 9;

    public static final int ORDERCOUNT_OFFSET = 13;

    public static final int MAKER_OFFSET = 17;

    public static final int TEMPLATE_ID = 2;

    /**
     * Schema version in wire format: 1.0
     */
    public static final short SCHEMA_VERSION = (short) 256;

    public static final int BLOCK_LENGTH = 18;

    private MemorySegment segment;

    private long offset;

    private final BitSetView presenceBits = new BitSetView();

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

    public long getPriceNanos() {
        return segment.get(Layouts.LONG_BE, this.offset + PRICENANOS_OFFSET);
    }

    public void setPriceNanos(long value) {
        segment.set(Layouts.LONG_BE, this.offset + PRICENANOS_OFFSET, value);
    }

    public int getSize() {
        return segment.get(Layouts.INT_BE, this.offset + SIZE_OFFSET);
    }

    public void setSize(int value) {
        segment.set(Layouts.INT_BE, this.offset + SIZE_OFFSET, value);
    }

    public int getOrderCount() {
        return segment.get(Layouts.INT_BE, this.offset + ORDERCOUNT_OFFSET);
    }

    public void setOrderCount(int value) {
        segment.set(Layouts.INT_BE, this.offset + ORDERCOUNT_OFFSET, value);
    }

    public boolean getMaker() {
        return segment.get(Layouts.BOOLEAN, this.offset + MAKER_OFFSET);
    }

    public void setMaker(boolean value) {
        segment.set(Layouts.BOOLEAN, this.offset + MAKER_OFFSET, value);
    }

    public boolean hasMaker() {
        return this.presenceBits.get(0);
    }

    @Override
    public void writeTo(BinaryWriter writer) {
        writer.writeLongBE(this.getPriceNanos());
        writer.writeIntBE(this.getSize());
        writer.writeIntBE(this.getOrderCount());
        writer.writeBoolean(this.getMaker());
    }
}
