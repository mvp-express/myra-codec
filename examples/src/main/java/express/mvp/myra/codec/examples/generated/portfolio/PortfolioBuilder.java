package express.mvp.myra.codec.examples.generated.portfolio;

import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.myra.codec.runtime.struct.VariableSizeRepeatingGroupBuilder;
import express.mvp.roray.ffm.utils.memory.BitSetView;
import express.mvp.roray.ffm.utils.memory.PooledSegment;
import express.mvp.roray.ffm.utils.memory.VarFieldWriter;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Single-pass, write-once builder for {@code Portfolio}.
 */
public final class PortfolioBuilder {
    private static final int TOTAL_FIELDS = 5;

    private static final int VAR_FIELD_COUNT = 5;

    private static final int PRESENCE_BYTES = 1;

    private static final String[] FIELD_NAMES = {
        "accountId",
        "legs",
        "tags",
        "attachments",
        "comment"
    };

    private static final int COMMENT_OPT_BIT = 0;

    private static final int ACCOUNTID_INDEX = 0;

    private static final int ACCOUNTID_VAR_SLOT = 0;

    private static final int LEGS_INDEX = 1;

    private static final int LEGS_VAR_SLOT = 1;

    private static final int TAGS_INDEX = 2;

    private static final int TAGS_VAR_SLOT = 2;

    private static final int ATTACHMENTS_INDEX = 3;

    private static final int ATTACHMENTS_VAR_SLOT = 3;

    private static final int COMMENT_INDEX = 4;

    private static final int COMMENT_VAR_SLOT = 4;

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

    private LegBuilder legsReusableBuilder;

    private PortfolioBuilder(MessageEncoder encoder, MemorySegment segment, boolean inlineMode) {
        this.inline = inlineMode;
        this.encoder = inline ? encoder : Objects.requireNonNull(encoder, "encoder");
        this.segment = Objects.requireNonNull(segment, "segment");
        this.payloadBase = inline ? 0L : MessageHeader.HEADER_SIZE;
        this.written = new boolean[TOTAL_FIELDS];
        MemorySegment body = segment.asSlice(this.payloadBase, segment.byteSize() - this.payloadBase);
        this.varWriter = new VarFieldWriter(body, PortfolioFlyweight.BLOCK_LENGTH - (VAR_FIELD_COUNT * 8), VAR_FIELD_COUNT);
        for (int i = 0; i < VAR_FIELD_COUNT; i++) {
                    this.varWriter.reserveVarField();
                };
        this.presenceBits = new BitSetView();
        this.presenceBits.wrap(segment, this.payloadBase, PRESENCE_BYTES);
        this.presenceBits.clearAll();
    }

    public static PortfolioBuilder allocate(MessageEncoder encoder, int capacity) {
        Objects.requireNonNull(encoder, "encoder");
        MemorySegment segment = encoder.acquire(capacity);
        return new PortfolioBuilder(encoder, segment, false);
    }

    static PortfolioBuilder inline(MemorySegment target) {
        Objects.requireNonNull(target, "target");
        return new PortfolioBuilder(null, target, true);
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
        this.varWriter = new VarFieldWriter(body, PortfolioFlyweight.BLOCK_LENGTH - (VAR_FIELD_COUNT * 8), VAR_FIELD_COUNT);
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
            return PortfolioFlyweight.BLOCK_LENGTH;
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

    public PortfolioBuilder setAccountId(String value, MemorySegment scratchBuffer) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(ACCOUNTID_INDEX, "accountId");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        varWriter.writeVarField(ACCOUNTID_VAR_SLOT, value, scratchBuffer);
        markWritten(ACCOUNTID_INDEX);
        return this;
    }

    /**
     * Sets the repeated legs field with the given count.
     * The consumer is called for each element to populate it.
     * @param count the number of elements
     * @param elementWriter the consumer to populate each element
     * @return this builder for chaining
     */
    public PortfolioBuilder setLegs(int count, Consumer<LegBuilder> elementWriter) {
        Objects.requireNonNull(elementWriter, "elementWriter");
        ensureWritable(LEGS_INDEX, "legs");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(LEGS_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        VariableSizeRepeatingGroupBuilder groupBuilder = new VariableSizeRepeatingGroupBuilder();
        groupBuilder.beginWithCount(segment, absoluteOffset, count);
        LegBuilder nestedBuilder = this.legsReusableBuilder;
        for (int i = 0; i < count; i++) {
            long elementStart = groupBuilder.beginElement();
            if (nestedBuilder == null) {
                nestedBuilder = LegBuilder.inline(segment.asSlice(elementStart, segment.byteSize() - elementStart));
                this.legsReusableBuilder = nestedBuilder;
            } else {
                nestedBuilder.resetInline(segment, elementStart);
            }
            elementWriter.accept(nestedBuilder);
            long nestedSize = nestedBuilder.finishInline();
            groupBuilder.endElement((int) nestedSize);
        }
        int bytesWritten = groupBuilder.finish();
        handle.finish(bytesWritten);
        markWritten(LEGS_INDEX);
        return this;
    }

    /**
     * Sets the repeated legs field with the given count.
     * The writer is called for each element to populate it.
     * @param count the number of elements
     * @param elementWriter the writer to populate each element
     * @return this builder for chaining
     */
    public PortfolioBuilder setLegs(int count, LegWriter elementWriter) {
        Objects.requireNonNull(elementWriter, "elementWriter");
        ensureWritable(LEGS_INDEX, "legs");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(LEGS_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        VariableSizeRepeatingGroupBuilder groupBuilder = new VariableSizeRepeatingGroupBuilder();
        groupBuilder.beginWithCount(segment, absoluteOffset, count);
        LegBuilder nestedBuilder = this.legsReusableBuilder;
        for (int i = 0; i < count; i++) {
            long elementStart = groupBuilder.beginElement();
            if (nestedBuilder == null) {
                nestedBuilder = LegBuilder.inline(segment.asSlice(elementStart, segment.byteSize() - elementStart));
                this.legsReusableBuilder = nestedBuilder;
            } else {
                nestedBuilder.resetInline(segment, elementStart);
            }
            elementWriter.writeTo(nestedBuilder, i);
            long nestedSize = nestedBuilder.finishInline();
            groupBuilder.endElement((int) nestedSize);
        }
        int bytesWritten = groupBuilder.finish();
        handle.finish(bytesWritten);
        markWritten(LEGS_INDEX);
        return this;
    }

    /**
     * Sets the repeated tags field with the given string values.
     * @param values the array of string values
     * @param scratchBuffer scratch buffer for encoding
     * @return this builder for chaining
     */
    public PortfolioBuilder setTags(String[] values, MemorySegment scratchBuffer) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(TAGS_INDEX, "tags");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(TAGS_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        VariableSizeRepeatingGroupBuilder groupBuilder = new VariableSizeRepeatingGroupBuilder();
        groupBuilder.beginWithCount(segment, absoluteOffset, values.length);
        for (String value : values) {
            groupBuilder.addString(value, scratchBuffer);
        }
        int bytesWritten = groupBuilder.finish();
        handle.finish(bytesWritten);
        markWritten(TAGS_INDEX);
        return this;
    }

    /**
     * Sets the repeated attachments field with the given byte arrays.
     * @param values the array of byte arrays
     * @return this builder for chaining
     */
    public PortfolioBuilder setAttachments(byte[][] values) {
        Objects.requireNonNull(values, "values");
        ensureWritable(ATTACHMENTS_INDEX, "attachments");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        VarFieldWriter.NestedFieldHandle handle = varWriter.beginNestedField(ATTACHMENTS_VAR_SLOT);
        long absoluteOffset = payloadBase + handle.relativeOffset();
        VariableSizeRepeatingGroupBuilder groupBuilder = new VariableSizeRepeatingGroupBuilder();
        groupBuilder.beginWithCount(segment, absoluteOffset, values.length);
        for (byte[] value : values) {
            groupBuilder.addBytes(value);
        }
        int bytesWritten = groupBuilder.finish();
        handle.finish(bytesWritten);
        markWritten(ATTACHMENTS_INDEX);
        return this;
    }

    public PortfolioBuilder setComment(String value, MemorySegment scratchBuffer) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");
        ensureWritable(COMMENT_INDEX, "comment");
        if (varWriter == null) {
            throw new IllegalStateException("Message has no variable fields");
        }
        varWriter.writeVarField(COMMENT_VAR_SLOT, value, scratchBuffer);
        markWritten(COMMENT_INDEX);
        presenceBits.set(COMMENT_OPT_BIT);
        return this;
    }
}
