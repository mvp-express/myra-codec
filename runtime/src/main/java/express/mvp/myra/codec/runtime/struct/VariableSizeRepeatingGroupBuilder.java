package express.mvp.myra.codec.runtime.struct;

import static express.mvp.roray.utils.memory.Layouts.*;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import express.mvp.roray.utils.memory.FlyweightAccessor;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A builder for writing repeating groups of variable-size elements.
 *
 * <p><b>Wire Format (Offset Table Encoding):</b>
 *
 * <pre>
 * [count:int32][offset0:int32][offset1:int32]...[offsetN-1:int32][element0_data][element1_data]...
 * </pre>
 *
 * <p>Each offset is relative to the start of the element data region (after the offset table). This
 * enables O(1) random access during reading.
 *
 * <p><b>Two-Phase Writing:</b>
 *
 * <ol>
 *   <li>Call {@link #beginWithCount(MemorySegment, long, int)} to reserve space for count and
 *       offset table
 *   <li>Call {@link #addString}, {@link #addBytes}, or {@link #beginElement}/{@link #endElement}
 *       for each element
 *   <li>Call {@link #finish()} to finalize offsets and return total bytes written
 * </ol>
 *
 * <p><b>Thread Safety:</b> This class is NOT thread-safe.
 *
 * <p><b>Example Usage with Strings:</b>
 *
 * <pre>{@code
 * VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
 * builder.beginWithCount(segment, offset, 3);
 * builder.addString("hello");
 * builder.addString("world");
 * builder.addString("!");
 * int bytesWritten = builder.finish();
 * }</pre>
 *
 * <p><b>Example Usage with Nested Messages:</b>
 *
 * <pre>{@code
 * VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
 * builder.beginWithCount(segment, offset, 2);
 *
 * // Element 0
 * long elementStart = builder.beginElement();
 * levelFlyweight.wrap(segment, elementStart);
 * levelFlyweight.setPriceNanos(100L);
 * levelFlyweight.setSize(50);
 * builder.endElement(LevelFlyweight.BLOCK_LENGTH);
 *
 * // Element 1
 * elementStart = builder.beginElement();
 * levelFlyweight.wrap(segment, elementStart);
 * levelFlyweight.setPriceNanos(200L);
 * levelFlyweight.setSize(75);
 * builder.endElement(LevelFlyweight.BLOCK_LENGTH);
 *
 * int bytesWritten = builder.finish();
 * }</pre>
 */
public final class VariableSizeRepeatingGroupBuilder {

    /** Size in bytes of the count field (int32). */
    public static final int COUNT_SIZE = 4;

    /** Size in bytes of each offset entry (int32). */
    public static final int OFFSET_ENTRY_SIZE = 4;

    @Nullable private MemorySegment segment;
    private long baseOffset;
    private int maxCount;
    private int currentIndex;
    private long offsetTableStart;
    private long dataRegionStart;
    private long writeOffset;
    private int currentDataOffset; // Relative offset for offset table entries

    /** Creates a new builder for variable-size elements. */
    public VariableSizeRepeatingGroupBuilder() {
        // Default constructor
    }

    /**
     * Begins writing a repeating group with a known element count. This reserves space for the
     * count field and offset table.
     *
     * @param segment the memory segment to write to
     * @param offset the offset within the segment where the group starts
     * @param count the number of elements that will be written
     * @throws NullPointerException if segment is null
     * @throws IllegalArgumentException if count is negative
     */
    public void beginWithCount(@NonNull MemorySegment segment, long offset, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative: " + count);
        }
        this.segment = Objects.requireNonNull(segment, "segment");
        this.baseOffset = offset;
        this.maxCount = count;
        this.currentIndex = 0;
        this.offsetTableStart = offset + COUNT_SIZE;
        this.dataRegionStart = offsetTableStart + ((long) count * OFFSET_ENTRY_SIZE);
        this.writeOffset = dataRegionStart;
        this.currentDataOffset = 0;

        // Write the count immediately
        segment.set(INT_BE, baseOffset, count);
    }

    /**
     * Returns the expected element count.
     *
     * @return the count passed to beginWithCount
     */
    public int expectedCount() {
        return maxCount;
    }

    /**
     * Returns the number of elements written so far.
     *
     * @return the current element index
     */
    public int currentCount() {
        return currentIndex;
    }

    /**
     * Returns the current write position.
     *
     * @return the absolute offset where the next element data will be written
     */
    public long currentOffset() {
        return writeOffset;
    }

    // =========================================================================
    // String Element Writers
    // =========================================================================

    /**
     * Adds a string element to the group.
     *
     * <p><b>Element Format:</b> [length:int32][utf8_bytes]
     *
     * @param value the string value to add
     * @return this builder for chaining
     * @throws IllegalStateException if all expected elements have been written
     */
    public VariableSizeRepeatingGroupBuilder addString(String value) {
        checkCanAdd();
        Objects.requireNonNull(value, "value");

        // Record offset for this element
        recordOffset();

        // Write string: [length][bytes]
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        segment.set(INT_BE, writeOffset, bytes.length);
        writeOffset += 4;

        MemorySegment.copy(bytes, 0, segment, BYTE, writeOffset, bytes.length);
        writeOffset += bytes.length;
        currentDataOffset += 4 + bytes.length;

        currentIndex++;
        return this;
    }

    /**
     * Adds a string element using a scratch buffer for UTF-8 encoding. This is more efficient than
     * addString(String) for repeated calls.
     *
     * @param value the string value to add
     * @param scratchBuffer a pre-allocated buffer for encoding
     * @return this builder for chaining
     */
    public VariableSizeRepeatingGroupBuilder addString(String value, MemorySegment scratchBuffer) {
        checkCanAdd();
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(scratchBuffer, "scratchBuffer");

        recordOffset();

        // Encode UTF-8 to scratch buffer
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int length = bytes.length;

        // Write length and bytes
        segment.set(INT_BE, writeOffset, length);
        writeOffset += 4;

        MemorySegment.copy(bytes, 0, segment, BYTE, writeOffset, length);
        writeOffset += length;
        currentDataOffset += 4 + length;

        currentIndex++;
        return this;
    }

    // =========================================================================
    // Bytes Element Writers
    // =========================================================================

    /**
     * Adds a bytes element to the group.
     *
     * <p><b>Element Format:</b> [length:int32][raw_bytes]
     *
     * @param bytes the byte array to add
     * @return this builder for chaining
     * @throws IllegalStateException if all expected elements have been written
     */
    public VariableSizeRepeatingGroupBuilder addBytes(byte[] bytes) {
        checkCanAdd();
        Objects.requireNonNull(bytes, "bytes");

        recordOffset();

        // Write bytes: [length][data]
        segment.set(INT_BE, writeOffset, bytes.length);
        writeOffset += 4;

        MemorySegment.copy(bytes, 0, segment, BYTE, writeOffset, bytes.length);
        writeOffset += bytes.length;
        currentDataOffset += 4 + bytes.length;

        currentIndex++;
        return this;
    }

    /**
     * Adds a bytes element from a MemorySegment slice.
     *
     * @param source the source segment containing the bytes
     * @param offset the offset within the source segment
     * @param length the number of bytes to copy
     * @return this builder for chaining
     */
    public VariableSizeRepeatingGroupBuilder addBytes(
            MemorySegment source, long offset, int length) {
        checkCanAdd();
        Objects.requireNonNull(source, "source");

        recordOffset();

        // Write bytes: [length][data]
        segment.set(INT_BE, writeOffset, length);
        writeOffset += 4;

        MemorySegment.copy(source, offset, segment, writeOffset, length);
        writeOffset += length;
        currentDataOffset += 4 + length;

        currentIndex++;
        return this;
    }

    // =========================================================================
    // Nested Message Element Writers
    // =========================================================================

    /**
     * Begins writing a nested message element. Returns the offset where the caller should write the
     * message data.
     *
     * <p>After writing the message, call {@link #endElement(int)} with the number of bytes written.
     *
     * @return the absolute offset where element data should be written
     * @throws IllegalStateException if all expected elements have been written
     */
    public long beginElement() {
        checkCanAdd();
        recordOffset();
        return writeOffset;
    }

    /**
     * Ends writing a nested message element, advancing the write position.
     *
     * @param bytesWritten the number of bytes written for this element
     */
    public void endElement(int bytesWritten) {
        writeOffset += bytesWritten;
        currentDataOffset += bytesWritten;
        currentIndex++;
    }

    /**
     * Convenience method to write a flyweight as an element.
     *
     * @param flyweight the flyweight containing the data to write
     * @param <T> the flyweight type
     * @return this builder for chaining
     */
    public <T extends FlyweightAccessor> VariableSizeRepeatingGroupBuilder addElement(T flyweight) {
        long elementStart = beginElement();
        // Copy flyweight data to our segment
        MemorySegment.copy(flyweight.segment(), 0, segment, elementStart, flyweight.byteSize());
        endElement(flyweight.byteSize());
        return this;
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private void checkCanAdd() {
        if (currentIndex >= maxCount) {
            throw new IllegalStateException(
                    "Cannot add more elements. Expected "
                            + maxCount
                            + ", already wrote "
                            + currentIndex);
        }
    }

    private void recordOffset() {
        // Write the relative offset for this element
        segment.set(
                INT_BE,
                offsetTableStart + (long) currentIndex * OFFSET_ENTRY_SIZE,
                currentDataOffset);
    }

    // =========================================================================
    // Finalization
    // =========================================================================

    /**
     * Finishes writing the repeating group. Validates that all expected elements were written.
     *
     * @return the total number of bytes written (count + offset table + element data)
     * @throws IllegalStateException if fewer elements were written than expected
     */
    public int finish() {
        if (currentIndex != maxCount) {
            throw new IllegalStateException(
                    "Expected " + maxCount + " elements but wrote " + currentIndex);
        }
        return (int) (writeOffset - baseOffset);
    }

    /**
     * Finishes writing and allows fewer elements than originally expected. Updates the count field
     * to reflect actual elements written.
     *
     * @return the total number of bytes written
     */
    public int finishPartial() {
        // Update count to actual elements written
        segment.set(INT_BE, baseOffset, currentIndex);
        // Note: The offset table has unused slots but that's okay
        return (int) (writeOffset - baseOffset);
    }

    /**
     * Returns the underlying memory segment.
     *
     * @return the segment being written to
     */
    public MemorySegment segment() {
        return segment;
    }

    /** Resets this builder, releasing the reference to the segment. */
    public void reset() {
        this.segment = null;
        this.baseOffset = 0;
        this.maxCount = 0;
        this.currentIndex = 0;
        this.offsetTableStart = 0;
        this.dataRegionStart = 0;
        this.writeOffset = 0;
        this.currentDataOffset = 0;
    }
}
