package express.mvp.myra.codec.examples.generated.portfolio;

import express.mvp.myra.codec.runtime.struct.VariableSizeRepeatingGroupIterator;
import express.mvp.roray.ffm.utils.memory.BinaryWriter;
import express.mvp.roray.ffm.utils.memory.BitSetView;
import express.mvp.roray.ffm.utils.memory.FlyweightAccessor;
import express.mvp.roray.ffm.utils.memory.Layouts;
import express.mvp.roray.ffm.utils.memory.Utf8View;
import java.lang.Override;
import java.lang.foreign.MemorySegment;

/**
 * Auto-generated, zero-copy flyweight for the Portfolio message.
 *
 * Provides direct access to binary data without deserialization overhead.
 * Thread-safe for read operations when properly synchronized.
 *
 * @see FlyweightAccessor
 */
public final class PortfolioFlyweight implements FlyweightAccessor {
    public static final int PRESENCE_BYTES = 1;

    public static final int COMMENT_OPT_BIT = 0;

    public static final int ACCOUNTID_OFFSET = 1;

    public static final int LEGS_OFFSET = 9;

    public static final int TAGS_OFFSET = 17;

    public static final int ATTACHMENTS_OFFSET = 25;

    public static final int COMMENT_OFFSET = 33;

    public static final int TEMPLATE_ID = 2;

    /**
     * Schema version in wire format: 1.0
     */
    public static final short SCHEMA_VERSION = (short) 256;

    public static final int BLOCK_LENGTH = 41;

    private MemorySegment segment;

    private long offset;

    private final BitSetView presenceBits = new BitSetView();

    private final Utf8View accountIdView = new Utf8View();

    private final VariableSizeRepeatingGroupIterator legsIterator = new VariableSizeRepeatingGroupIterator();

    private final LegFlyweight legsView = new LegFlyweight();

    private final VariableSizeRepeatingGroupIterator tagsIterator = new VariableSizeRepeatingGroupIterator();

    private final VariableSizeRepeatingGroupIterator attachmentsIterator = new VariableSizeRepeatingGroupIterator();

    private final Utf8View commentView = new Utf8View();

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

    public Utf8View getAccountId() {
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + ACCOUNTID_OFFSET);
        final int dataLength = segment.get(Layouts.INT_BE, this.offset + ACCOUNTID_OFFSET + 4);
        this.accountIdView.wrap(this.segment, this.offset + relativeOffset, dataLength);
        return this.accountIdView;
    }

    public int getLegsCount() {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + LEGS_OFFSET);
        this.legsIterator.wrap(this.segment, dataOffset);
        return this.legsIterator.count();
    }

    /**
     * Returns the nested message at the given index, wrapped in a reusable flyweight.
     * @param index the element index (0-based)
     * @return the flyweight wrapper (reused instance)
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public LegFlyweight getLegsAt(int index) {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + LEGS_OFFSET);
        this.legsIterator.wrap(this.segment, dataOffset);
        return this.legsIterator.wrapElementAt(index, this.legsView);
    }

    public int getTagsCount() {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + TAGS_OFFSET);
        this.tagsIterator.wrap(this.segment, dataOffset);
        return this.tagsIterator.count();
    }

    /**
     * Reads the string at the given index into the provided Utf8View.
     * @param index the element index (0-based)
     * @param view the view to wrap around the string data
     */
    public void getTagsAt(int index, Utf8View view) {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + TAGS_OFFSET);
        this.tagsIterator.wrap(this.segment, dataOffset);
        this.tagsIterator.getStringAt(index, view);
    }

    public int getAttachmentsCount() {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + ATTACHMENTS_OFFSET);
        this.attachmentsIterator.wrap(this.segment, dataOffset);
        return this.attachmentsIterator.count();
    }

    /**
     * Returns a slice of the bytes at the given index.
     * @param index the element index (0-based)
     * @return a MemorySegment slice containing the bytes
     */
    public MemorySegment getAttachmentsAt(int index) {
        final long dataOffset = this.offset + segment.get(Layouts.INT_BE, this.offset + ATTACHMENTS_OFFSET);
        this.attachmentsIterator.wrap(this.segment, dataOffset);
        return this.attachmentsIterator.getBytesAt(index);
    }

    public Utf8View getComment() {
        final int relativeOffset = segment.get(Layouts.INT_BE, this.offset + COMMENT_OFFSET);
        final int dataLength = segment.get(Layouts.INT_BE, this.offset + COMMENT_OFFSET + 4);
        this.commentView.wrap(this.segment, this.offset + relativeOffset, dataLength);
        return this.commentView;
    }

    public boolean hasComment() {
        return this.presenceBits.get(0);
    }

    @Override
    public void writeTo(BinaryWriter writer) {
        Utf8View accountIdViewTmp = this.getAccountId();
        byte[] accountIdBytesTmp = new byte[(int)accountIdViewTmp.byteSize()];
        MemorySegment.copy(accountIdViewTmp.segment(), accountIdViewTmp.offset(), MemorySegment.ofArray(accountIdBytesTmp), 0, accountIdViewTmp.byteSize());
        writer.writeBytes(accountIdBytesTmp);
        final int legsRelativeOffset = this.segment.get(Layouts.INT_BE, this.offset + LEGS_OFFSET);
        final int legsDataLength = this.segment.get(Layouts.INT_BE, this.offset + LEGS_OFFSET + 4);
        writer.writeVarInt(legsDataLength);
        writer.writeSegmentRaw(this.segment, this.offset + legsRelativeOffset, legsDataLength);
        final int tagsRelativeOffset = this.segment.get(Layouts.INT_BE, this.offset + TAGS_OFFSET);
        final int tagsDataLength = this.segment.get(Layouts.INT_BE, this.offset + TAGS_OFFSET + 4);
        writer.writeVarInt(tagsDataLength);
        writer.writeSegmentRaw(this.segment, this.offset + tagsRelativeOffset, tagsDataLength);
        final int attachmentsRelativeOffset = this.segment.get(Layouts.INT_BE, this.offset + ATTACHMENTS_OFFSET);
        final int attachmentsDataLength = this.segment.get(Layouts.INT_BE, this.offset + ATTACHMENTS_OFFSET + 4);
        writer.writeVarInt(attachmentsDataLength);
        writer.writeSegmentRaw(this.segment, this.offset + attachmentsRelativeOffset, attachmentsDataLength);
        Utf8View commentViewTmp = this.getComment();
        byte[] commentBytesTmp = new byte[(int)commentViewTmp.byteSize()];
        MemorySegment.copy(commentViewTmp.segment(), commentViewTmp.offset(), MemorySegment.ofArray(commentBytesTmp), 0, commentViewTmp.byteSize());
        writer.writeBytes(commentBytesTmp);
    }
}
