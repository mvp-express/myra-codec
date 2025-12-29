package express.mvp.myra.codec.examples.generated.orderbook;

import express.mvp.roray.ffm.utils.memory.BinaryWriter;
import express.mvp.roray.ffm.utils.memory.BitSetView;
import express.mvp.roray.ffm.utils.memory.FlyweightAccessor;
import express.mvp.roray.ffm.utils.memory.Layouts;
import express.mvp.roray.ffm.utils.memory.Utf8View;
import java.lang.Override;
import java.lang.foreign.MemorySegment;

/**
 * Auto-generated, zero-copy flyweight for the Trade message.
 *
 * Provides direct access to binary data without deserialization overhead.
 * Thread-safe for read operations when properly synchronized.
 *
 * @see FlyweightAccessor
 */
public final class TradeFlyweight implements FlyweightAccessor {
    public static final int PRESENCE_BYTES = 1;

    public static final int AGGRESSOR_OPT_BIT = 0;

    public static final int PRICENANOS_OFFSET = 1;

    public static final int SIZE_OFFSET = 9;

    public static final int AGGRESSOR_OFFSET = 13;

    public static final int TEMPLATE_ID = 1;

    /**
     * Schema version in wire format: 1.0
     */
    public static final short SCHEMA_VERSION = (short) 256;

    public static final int BLOCK_LENGTH = 21;

    private MemorySegment segment;

    private long offset;

    private final BitSetView presenceBits = new BitSetView();

    private final Utf8View aggressorView = new Utf8View();

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

    public Utf8View getAggressor() {
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + AGGRESSOR_OFFSET);
        final int dataLength = segment.get(Layouts.INT_BE, this.offset + AGGRESSOR_OFFSET + 4);
        this.aggressorView.wrap(this.segment, this.offset + relativeOffset, dataLength);
        return this.aggressorView;
    }

    public boolean hasAggressor() {
        return this.presenceBits.get(0);
    }

    @Override
    public void writeTo(BinaryWriter writer) {
        writer.writeLongBE(this.getPriceNanos());
        writer.writeIntBE(this.getSize());
        Utf8View aggressorViewTmp = this.getAggressor();
        byte[] aggressorBytesTmp = new byte[(int)aggressorViewTmp.byteSize()];
        MemorySegment.copy(aggressorViewTmp.segment(), aggressorViewTmp.offset(), MemorySegment.ofArray(aggressorBytesTmp), 0, aggressorViewTmp.byteSize());
        writer.writeBytes(aggressorBytesTmp);
    }
}
