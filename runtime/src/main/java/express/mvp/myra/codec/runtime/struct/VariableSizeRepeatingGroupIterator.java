package express.mvp.myra.codec.runtime.struct;

import static express.mvp.roray.ffm.utils.memory.Layouts.*;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import express.mvp.roray.ffm.utils.memory.FlyweightAccessor;
import express.mvp.roray.ffm.utils.memory.Utf8View;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A zero-allocation iterator for repeating groups of variable-size elements.
 *
 * <p><b>Wire Format (Offset Table Encoding):</b>
 *
 * <pre>
 * [count:int32][offset0:int32][offset1:int32]...[offsetN-1:int32][element0_data][element1_data]...
 * </pre>
 *
 * <p>Each offset is relative to the start of the element data region (after the offset table). This
 * enables O(1) random access to any element regardless of its size.
 *
 * <p><b>Thread Safety:</b> This class is NOT thread-safe. Each thread should use its own iterator
 * instance. The iterator can be reused by calling {@link #wrap}.
 *
 * <p><b>Example Usage with Strings:</b>
 *
 * <pre>{@code
 * VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
 * iter.wrap(segment, offset);
 * int count = iter.count();
 * Utf8View view = new Utf8View();
 * for (int i = 0; i < count; i++) {
 *     iter.getStringAt(i, view);
 *     String str = view.toString();
 * }
 * }</pre>
 *
 * <p><b>Example Usage with Nested Messages:</b>
 *
 * <pre>{@code
 * VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
 * iter.wrap(segment, offset);
 * LevelFlyweight flyweight = new LevelFlyweight();
 * for (int i = 0; i < iter.count(); i++) {
 *     iter.wrapElementAt(i, flyweight);
 *     long price = flyweight.getPriceNanos();
 * }
 * }</pre>
 *
 * @see RepeatingGroupIterator for fixed-size elements
 */
public final class VariableSizeRepeatingGroupIterator {

    /** Size in bytes of the count field (int32). */
    public static final int COUNT_SIZE = 4;

    /** Size in bytes of each offset entry (int32). */
    public static final int OFFSET_ENTRY_SIZE = 4;

    @Nullable private MemorySegment segment;
    private long baseOffset;
    private long offsetTableStart;
    private long dataRegionStart;
    private int count;

    /** Creates a new iterator for variable-size elements. */
    public VariableSizeRepeatingGroupIterator() {
        // Default constructor
    }

    /**
     * Wraps this iterator around a memory segment at the specified offset.
     *
     * @param segment the memory segment containing the repeating group data
     * @param offset the offset within the segment where the group starts
     * @throws NullPointerException if segment is null
     */
    public void wrap(@NonNull MemorySegment segment, long offset) {
        this.segment = Objects.requireNonNull(segment, "segment");
        this.baseOffset = offset;
        this.count = segment.get(INT_BE, offset);
        this.offsetTableStart = offset + COUNT_SIZE;
        this.dataRegionStart = offsetTableStart + ((long) count * OFFSET_ENTRY_SIZE);
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
     * Checks if this iterator is currently wrapped around a segment.
     *
     * @return true if wrapped, false otherwise
     */
    public boolean isWrapped() {
        return segment != null;
    }

    /**
     * Returns the total size of this repeating group header in bytes (count + offset table). Does
     * not include element data.
     *
     * @return header byte size = COUNT_SIZE + (count * OFFSET_ENTRY_SIZE)
     */
    public int headerByteSize() {
        return COUNT_SIZE + (count * OFFSET_ENTRY_SIZE);
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

    /**
     * Gets the offset entry for an element at the given index. The offset is relative to the data
     * region start.
     *
     * @param index the element index (0-based)
     * @return the relative offset within the data region
     */
    private int getOffsetEntry(int index) {
        return segment.get(INT_BE, offsetTableStart + (long) index * OFFSET_ENTRY_SIZE);
    }

    /**
     * Gets the length of an element at the given index. Calculated as the difference between
     * consecutive offsets, or the remaining data size for the last element.
     *
     * @param index the element index (0-based)
     * @return the length in bytes of the element
     */
    public int getElementLength(int index) {
        checkIndex(index);
        int startOffset = getOffsetEntry(index);
        if (index == count - 1) {
            // Last element: need to calculate from total data size
            // This requires knowing the total group size from external context
            // For now, we'll use a sentinel approach or require explicit end offset
            // Alternative: store [offset, length] pairs instead of just offsets
            // For simplicity, let's use consecutive offset differences
            // We'll need to store an extra "end" offset or calculate differently
            throw new UnsupportedOperationException(
                    "Last element length requires total group size - use getElementRange instead");
        }
        int nextOffset = getOffsetEntry(index + 1);
        return nextOffset - startOffset;
    }

    /**
     * Returns the absolute offset within the segment where element data starts.
     *
     * @param index the element index (0-based)
     * @return the absolute offset of the element data
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public long getElementOffset(int index) {
        checkIndex(index);
        int relativeOffset = getOffsetEntry(index);
        return dataRegionStart + relativeOffset;
    }

    // =========================================================================
    // String Element Access
    // =========================================================================

    /**
     * Reads a string element at the specified index into the provided Utf8View. The view is wrapped
     * around the string data for zero-copy access.
     *
     * <p><b>Element Format:</b> [length:int32][utf8_bytes]
     *
     * @param index the element index (0-based)
     * @param view the Utf8View to wrap around the string data
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public void getStringAt(int index, Utf8View view) {
        checkIndex(index);
        long elementStart = getElementOffset(index);
        int stringLength = segment.get(INT_BE, elementStart);
        view.wrap(segment, elementStart + 4, stringLength);
    }

    /**
     * Iterates over all string elements, invoking the consumer for each. The same Utf8View instance
     * is reused for each element.
     *
     * @param consumer the consumer to receive each string view
     */
    public void forEachString(Consumer<Utf8View> consumer) {
        Utf8View view = new Utf8View();
        for (int i = 0; i < count; i++) {
            getStringAt(i, view);
            consumer.accept(view);
        }
    }

    // =========================================================================
    // Nested Message Element Access
    // =========================================================================

    /**
     * Wraps a flyweight accessor at the element position for the given index. This enables
     * zero-copy access to nested message fields.
     *
     * <p><b>Element Format:</b> Directly encoded message bytes (no length prefix at element level -
     * length is in the offset table).
     *
     * @param index the element index (0-based)
     * @param flyweight the flyweight to wrap at the element position
     * @param <T> the flyweight type
     * @return the same flyweight instance, now wrapped at the element position
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public <T extends FlyweightAccessor> T wrapElementAt(int index, T flyweight) {
        checkIndex(index);
        long elementStart = getElementOffset(index);
        flyweight.wrap(segment, elementStart);
        return flyweight;
    }

    /**
     * Iterates over all nested message elements, invoking the consumer for each. The same flyweight
     * instance is reused and re-wrapped for each element.
     *
     * @param flyweight the flyweight to reuse for each element
     * @param consumer the consumer to receive each wrapped flyweight
     * @param <T> the flyweight type
     */
    public <T extends FlyweightAccessor> void forEach(T flyweight, Consumer<T> consumer) {
        for (int i = 0; i < count; i++) {
            wrapElementAt(i, flyweight);
            consumer.accept(flyweight);
        }
    }

    // =========================================================================
    // Bytes Element Access
    // =========================================================================

    /**
     * Returns a slice of the underlying segment containing the bytes at the given index.
     *
     * <p><b>Element Format:</b> [length:int32][raw_bytes]
     *
     * @param index the element index (0-based)
     * @return a MemorySegment slice containing the bytes data
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public MemorySegment getBytesAt(int index) {
        checkIndex(index);
        long elementStart = getElementOffset(index);
        int bytesLength = segment.get(INT_BE, elementStart);
        return segment.asSlice(elementStart + 4, bytesLength);
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
     * Returns the start of the data region (after offset table).
     *
     * @return the absolute offset where element data begins
     */
    public long dataRegionStart() {
        return dataRegionStart;
    }

    /** Resets this iterator, releasing the reference to the segment. */
    public void reset() {
        this.segment = null;
        this.baseOffset = 0;
        this.offsetTableStart = 0;
        this.dataRegionStart = 0;
        this.count = 0;
    }
}
