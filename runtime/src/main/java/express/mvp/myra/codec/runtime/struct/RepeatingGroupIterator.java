package express.mvp.myra.codec.runtime.struct;

import static express.mvp.roray.utils.memory.Layouts.*;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * A zero-allocation iterator for repeating groups of fixed-size primitive elements.
 *
 * <p><b>Wire Format (Inline Encoding):</b>
 * <pre>
 * [count:int32][element0][element1]...[elementN-1]
 * </pre>
 *
 * <p>This iterator provides direct access to elements at specific indices without
 * allocating intermediate objects. Each element is read directly from the underlying
 * memory segment using the specified element size.
 *
 * <p><b>Thread Safety:</b> This class is NOT thread-safe. Each thread should use
 * its own iterator instance. The iterator can be reused by calling {@link #wrap}.
 *
 * <p><b>Example Usage:</b>
 * <pre>{@code
 * RepeatingGroupIterator iter = new RepeatingGroupIterator(8); // 8 bytes per long
 * iter.wrap(segment, offset);
 * int count = iter.count();
 * for (int i = 0; i < count; i++) {
 *     long value = iter.getLongAt(i);
 *     // process value
 * }
 * }</pre>
 *
 * @see VariableSizeRepeatingGroupIterator for variable-length elements
 */
public final class RepeatingGroupIterator {

    /** Size in bytes of the count field (int32). */
    public static final int COUNT_SIZE = 4;

    private final int elementSize;
    @Nullable private MemorySegment segment;
    private long baseOffset;
    private long dataOffset;
    private int count;

    /**
     * Creates a new iterator for fixed-size elements.
     *
     * @param elementSize the size in bytes of each element (e.g., 8 for int64, 4 for int32)
     * @throws IllegalArgumentException if elementSize is not positive
     */
    public RepeatingGroupIterator(int elementSize) {
        if (elementSize <= 0) {
            throw new IllegalArgumentException("Element size must be positive: " + elementSize);
        }
        this.elementSize = elementSize;
    }

    /**
     * Wraps this iterator around a memory segment at the specified offset.
     *
     * @param segment the memory segment containing the repeating group data
     * @param offset  the offset within the segment where the group starts
     * @throws NullPointerException if segment is null
     */
    public void wrap(@NonNull MemorySegment segment, long offset) {
        this.segment = Objects.requireNonNull(segment, "segment");
        this.baseOffset = offset;
        this.count = segment.get(INT_BE, offset);
        this.dataOffset = offset + COUNT_SIZE;
    }

    /**
     * Returns the number of elements in this repeating group.
     *
     * @return the element count (non-negative)
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
     * Checks if this iterator is currently wrapped around a segment.
     *
     * @return true if wrapped, false otherwise
     */
    public boolean isWrapped() {
        return segment != null;
    }

    /**
     * Returns the total size of this repeating group in bytes,
     * including the count field and all element data.
     *
     * @return total byte size = COUNT_SIZE + (count * elementSize)
     */
    public int byteSize() {
        return COUNT_SIZE + (count * elementSize);
    }

    /**
     * Validates that the index is within bounds.
     *
     * @param index the index to validate
     * @throws IndexOutOfBoundsException if index is out of range
     */
    private void checkIndex(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for count " + count);
        }
    }

    // =========================================================================
    // Element Accessors (Getters)
    // =========================================================================

    /**
     * Gets a byte (int8) element at the specified index.
     *
     * @param index the element index (0-based)
     * @return the byte value at the index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public byte getByteAt(int index) {
        checkIndex(index);
        return segment.get(BYTE, dataOffset + (long) index * elementSize);
    }

    /**
     * Gets a short (int16) element at the specified index.
     *
     * @param index the element index (0-based)
     * @return the short value at the index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public short getShortAt(int index) {
        checkIndex(index);
        return segment.get(SHORT_BE, dataOffset + (long) index * elementSize);
    }

    /**
     * Gets an int (int32) element at the specified index.
     *
     * @param index the element index (0-based)
     * @return the int value at the index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public int getIntAt(int index) {
        checkIndex(index);
        return segment.get(INT_BE, dataOffset + (long) index * elementSize);
    }

    /**
     * Gets a long (int64) element at the specified index.
     *
     * @param index the element index (0-based)
     * @return the long value at the index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public long getLongAt(int index) {
        checkIndex(index);
        return segment.get(LONG_BE, dataOffset + (long) index * elementSize);
    }

    /**
     * Gets a float (float32) element at the specified index.
     *
     * @param index the element index (0-based)
     * @return the float value at the index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public float getFloatAt(int index) {
        checkIndex(index);
        return segment.get(FLOAT_BE, dataOffset + (long) index * elementSize);
    }

    /**
     * Gets a double (float64) element at the specified index.
     *
     * @param index the element index (0-based)
     * @return the double value at the index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public double getDoubleAt(int index) {
        checkIndex(index);
        return segment.get(DOUBLE_BE, dataOffset + (long) index * elementSize);
    }

    /**
     * Gets a boolean element at the specified index.
     *
     * @param index the element index (0-based)
     * @return the boolean value at the index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public boolean getBooleanAt(int index) {
        checkIndex(index);
        return segment.get(BOOLEAN, dataOffset + (long) index * elementSize);
    }

    /**
     * Returns the offset of the element at the given index within the segment.
     * This is useful for wrapping flyweight objects at specific positions.
     *
     * @param index the element index (0-based)
     * @return the absolute offset within the segment
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public long elementOffset(int index) {
        checkIndex(index);
        return dataOffset + (long) index * elementSize;
    }

    /**
     * Returns the underlying memory segment.
     *
     * @return the wrapped segment, or null if not wrapped
     */
    public MemorySegment segment() {
        return segment;
    }

    /**
     * Resets this iterator, releasing the reference to the segment.
     */
    public void reset() {
        this.segment = null;
        this.baseOffset = 0;
        this.dataOffset = 0;
        this.count = 0;
    }
}
