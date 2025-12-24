package express.mvp.myra.codec.runtime.struct;

import static express.mvp.roray.ffm.utils.memory.Layouts.*;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * A builder for writing repeating groups of fixed-size primitive elements.
 *
 * <p><b>Wire Format (Inline Encoding):</b>
 *
 * <pre>
 * [count:int32][element0][element1]...[elementN-1]
 * </pre>
 *
 * <p>This builder writes elements directly to a pre-allocated memory segment. The count is written
 * first as a placeholder, then elements are appended, and finally the count is updated with the
 * actual number of elements written.
 *
 * <p><b>Thread Safety:</b> This class is NOT thread-safe.
 *
 * <p><b>Example Usage:</b>
 *
 * <pre>{@code
 * RepeatingGroupBuilder builder = new RepeatingGroupBuilder(8); // 8 bytes per long
 * builder.wrap(segment, offset);
 * builder.addLong(100L);
 * builder.addLong(200L);
 * builder.addLong(300L);
 * int bytesWritten = builder.finish(); // Writes count=3 and returns total bytes
 * }</pre>
 */
public final class RepeatingGroupBuilder {

    /** Size in bytes of the count field (int32). */
    public static final int COUNT_SIZE = 4;

    private final int elementSize;
    @Nullable private MemorySegment segment;
    private long baseOffset;
    private long writeOffset;
    private int count;

    /**
     * Creates a new builder for fixed-size elements.
     *
     * @param elementSize the size in bytes of each element (e.g., 8 for int64, 4 for int32)
     * @throws IllegalArgumentException if elementSize is not positive
     */
    public RepeatingGroupBuilder(int elementSize) {
        if (elementSize <= 0) {
            throw new IllegalArgumentException("Element size must be positive: " + elementSize);
        }
        this.elementSize = elementSize;
    }

    /**
     * Wraps this builder around a memory segment at the specified offset. Resets the element count
     * to zero.
     *
     * @param segment the memory segment to write to
     * @param offset the offset within the segment where the group starts
     * @throws NullPointerException if segment is null
     */
    public void wrap(@NonNull MemorySegment segment, long offset) {
        this.segment = Objects.requireNonNull(segment, "segment");
        this.baseOffset = offset;
        this.writeOffset = offset + COUNT_SIZE; // Skip count field initially
        this.count = 0;
    }

    /**
     * Returns the current element count.
     *
     * @return the number of elements written so far
     */
    public int count() {
        return count;
    }

    /**
     * Returns the element size in bytes.
     *
     * @return the size of each element in bytes
     */
    public int elementSize() {
        return elementSize;
    }

    /**
     * Returns the current write position (next element offset).
     *
     * @return the absolute offset where the next element will be written
     */
    public long currentOffset() {
        return writeOffset;
    }

    /**
     * Returns the total bytes that will be written when finished (count field + all elements).
     *
     * @return total byte size = COUNT_SIZE + (count * elementSize)
     */
    public int currentByteSize() {
        return COUNT_SIZE + (count * elementSize);
    }

    // =========================================================================
    // Element Writers
    // =========================================================================

    /**
     * Adds a byte (int8) element.
     *
     * @param value the byte value to add
     * @return this builder for chaining
     */
    public RepeatingGroupBuilder addByte(byte value) {
        segment.set(BYTE, writeOffset, value);
        writeOffset += elementSize;
        count++;
        return this;
    }

    /**
     * Adds a short (int16) element.
     *
     * @param value the short value to add
     * @return this builder for chaining
     */
    public RepeatingGroupBuilder addShort(short value) {
        segment.set(SHORT_BE, writeOffset, value);
        writeOffset += elementSize;
        count++;
        return this;
    }

    /**
     * Adds an int (int32) element.
     *
     * @param value the int value to add
     * @return this builder for chaining
     */
    public RepeatingGroupBuilder addInt(int value) {
        segment.set(INT_BE, writeOffset, value);
        writeOffset += elementSize;
        count++;
        return this;
    }

    /**
     * Adds a long (int64) element.
     *
     * @param value the long value to add
     * @return this builder for chaining
     */
    public RepeatingGroupBuilder addLong(long value) {
        segment.set(LONG_BE, writeOffset, value);
        writeOffset += elementSize;
        count++;
        return this;
    }

    /**
     * Adds a float (float32) element.
     *
     * @param value the float value to add
     * @return this builder for chaining
     */
    public RepeatingGroupBuilder addFloat(float value) {
        segment.set(FLOAT_BE, writeOffset, value);
        writeOffset += elementSize;
        count++;
        return this;
    }

    /**
     * Adds a double (float64) element.
     *
     * @param value the double value to add
     * @return this builder for chaining
     */
    public RepeatingGroupBuilder addDouble(double value) {
        segment.set(DOUBLE_BE, writeOffset, value);
        writeOffset += elementSize;
        count++;
        return this;
    }

    /**
     * Adds a boolean element.
     *
     * @param value the boolean value to add
     * @return this builder for chaining
     */
    public RepeatingGroupBuilder addBoolean(boolean value) {
        segment.set(BOOLEAN, writeOffset, value);
        writeOffset += elementSize;
        count++;
        return this;
    }

    /**
     * Adds multiple long values from an array.
     *
     * @param values the array of long values to add
     * @return this builder for chaining
     */
    public RepeatingGroupBuilder addLongs(long[] values) {
        for (long value : values) {
            addLong(value);
        }
        return this;
    }

    /**
     * Adds multiple int values from an array.
     *
     * @param values the array of int values to add
     * @return this builder for chaining
     */
    public RepeatingGroupBuilder addInts(int[] values) {
        for (int value : values) {
            addInt(value);
        }
        return this;
    }

    // =========================================================================
    // Finalization
    // =========================================================================

    /**
     * Finishes writing the repeating group by updating the count field. Must be called after all
     * elements have been added.
     *
     * @return the total number of bytes written (count + elements)
     */
    public int finish() {
        // Write the count at the beginning
        segment.set(INT_BE, baseOffset, count);
        return currentByteSize();
    }

    /** Resets this builder, releasing the reference to the segment. */
    public void reset() {
        this.segment = null;
        this.baseOffset = 0;
        this.writeOffset = 0;
        this.count = 0;
    }
}
