package express.mvp.myra.codec.runtime.struct;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for RepeatingGroupIterator - fixed-size primitive element iteration. */
@DisplayName("RepeatingGroupIterator Tests")
class RepeatingGroupIteratorTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("Should accept positive element size")
        void shouldAcceptPositiveElementSize() {
            assertDoesNotThrow(() -> new RepeatingGroupIterator(8));
            assertDoesNotThrow(() -> new RepeatingGroupIterator(1));
            assertDoesNotThrow(() -> new RepeatingGroupIterator(4));
        }

        @Test
        @DisplayName("Should reject zero element size")
        void shouldRejectZeroElementSize() {
            assertThrows(IllegalArgumentException.class, () -> new RepeatingGroupIterator(0));
        }

        @Test
        @DisplayName("Should reject negative element size")
        void shouldRejectNegativeElementSize() {
            assertThrows(IllegalArgumentException.class, () -> new RepeatingGroupIterator(-1));
        }
    }

    @Nested
    @DisplayName("Wrapping")
    class WrappingTests {

        @Test
        @DisplayName("Should read count from wrapped segment")
        void shouldReadCountFromWrappedSegment() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                // Write count = 5 at offset 0
                segment.set(
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN),
                        0,
                        5);

                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 0);

                assertEquals(5, iter.count());
            }
        }

        @Test
        @DisplayName("Should read count from non-zero offset")
        void shouldReadCountFromNonZeroOffset() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                // Write count = 3 at offset 10
                segment.set(
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN),
                        10,
                        3);

                RepeatingGroupIterator iter = new RepeatingGroupIterator(4);
                iter.wrap(segment, 10);

                assertEquals(3, iter.count());
            }
        }

        @Test
        @DisplayName("Should reject null segment")
        void shouldRejectNullSegment() {
            RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
            assertThrows(NullPointerException.class, () -> iter.wrap(null, 0));
        }

        @Test
        @DisplayName("Should report wrapped state correctly")
        void shouldReportWrappedStateCorrectly() {
            RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
            assertFalse(iter.isWrapped());

            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                segment.set(
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN),
                        0,
                        0);
                iter.wrap(segment, 0);
                assertTrue(iter.isWrapped());

                iter.reset();
                assertFalse(iter.isWrapped());
            }
        }
    }

    @Nested
    @DisplayName("Element Access - Long (int64)")
    class LongAccessTests {

        @Test
        @DisplayName("Should read long elements at correct positions")
        void shouldReadLongElementsAtCorrectPositions() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                var intLayout =
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN);
                var longLayout =
                        java.lang.foreign.ValueLayout.JAVA_LONG_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN);

                // Write count = 3
                segment.set(intLayout, 0, 3);
                // Write elements at offset 4, 12, 20
                segment.set(longLayout, 4, 100L);
                segment.set(longLayout, 12, 200L);
                segment.set(longLayout, 20, 300L);

                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 0);

                assertEquals(3, iter.count());
                assertEquals(100L, iter.getLongAt(0));
                assertEquals(200L, iter.getLongAt(1));
                assertEquals(300L, iter.getLongAt(2));
            }
        }

        @Test
        @DisplayName("Should throw on out-of-bounds access")
        void shouldThrowOnOutOfBoundsAccess() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                var intLayout =
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN);

                segment.set(intLayout, 0, 2); // count = 2

                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 0);

                assertThrows(IndexOutOfBoundsException.class, () -> iter.getLongAt(-1));
                assertThrows(IndexOutOfBoundsException.class, () -> iter.getLongAt(2));
                assertThrows(IndexOutOfBoundsException.class, () -> iter.getLongAt(100));
            }
        }
    }

    @Nested
    @DisplayName("Element Access - Int (int32)")
    class IntAccessTests {

        @Test
        @DisplayName("Should read int elements at correct positions")
        void shouldReadIntElementsAtCorrectPositions() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                var intLayout =
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN);

                // Write count = 4
                segment.set(intLayout, 0, 4);
                // Write elements at offset 4, 8, 12, 16
                segment.set(intLayout, 4, 10);
                segment.set(intLayout, 8, 20);
                segment.set(intLayout, 12, 30);
                segment.set(intLayout, 16, 40);

                RepeatingGroupIterator iter = new RepeatingGroupIterator(4);
                iter.wrap(segment, 0);

                assertEquals(4, iter.count());
                assertEquals(10, iter.getIntAt(0));
                assertEquals(20, iter.getIntAt(1));
                assertEquals(30, iter.getIntAt(2));
                assertEquals(40, iter.getIntAt(3));
            }
        }
    }

    @Nested
    @DisplayName("Element Access - Other Types")
    class OtherTypeAccessTests {

        @Test
        @DisplayName("Should read byte elements")
        void shouldReadByteElements() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                var intLayout =
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN);

                segment.set(intLayout, 0, 3);
                segment.set(java.lang.foreign.ValueLayout.JAVA_BYTE, 4, (byte) 10);
                segment.set(java.lang.foreign.ValueLayout.JAVA_BYTE, 5, (byte) 20);
                segment.set(java.lang.foreign.ValueLayout.JAVA_BYTE, 6, (byte) 30);

                RepeatingGroupIterator iter = new RepeatingGroupIterator(1);
                iter.wrap(segment, 0);

                assertEquals((byte) 10, iter.getByteAt(0));
                assertEquals((byte) 20, iter.getByteAt(1));
                assertEquals((byte) 30, iter.getByteAt(2));
            }
        }

        @Test
        @DisplayName("Should read boolean elements")
        void shouldReadBooleanElements() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                var intLayout =
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN);

                segment.set(intLayout, 0, 3);
                segment.set(java.lang.foreign.ValueLayout.JAVA_BOOLEAN, 4, true);
                segment.set(java.lang.foreign.ValueLayout.JAVA_BOOLEAN, 5, false);
                segment.set(java.lang.foreign.ValueLayout.JAVA_BOOLEAN, 6, true);

                RepeatingGroupIterator iter = new RepeatingGroupIterator(1);
                iter.wrap(segment, 0);

                assertTrue(iter.getBooleanAt(0));
                assertFalse(iter.getBooleanAt(1));
                assertTrue(iter.getBooleanAt(2));
            }
        }

        @Test
        @DisplayName("Should read double elements")
        void shouldReadDoubleElements() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                var intLayout =
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN);
                var doubleLayout =
                        java.lang.foreign.ValueLayout.JAVA_DOUBLE_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN);

                segment.set(intLayout, 0, 2);
                segment.set(doubleLayout, 4, 3.14);
                segment.set(doubleLayout, 12, 2.71);

                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 0);

                assertEquals(3.14, iter.getDoubleAt(0), 0.001);
                assertEquals(2.71, iter.getDoubleAt(1), 0.001);
            }
        }
    }

    @Nested
    @DisplayName("Byte Size Calculation")
    class ByteSizeTests {

        @Test
        @DisplayName("Should calculate byte size correctly")
        void shouldCalculateByteSizeCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                var intLayout =
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN);

                segment.set(intLayout, 0, 5); // count = 5

                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 0);

                // COUNT_SIZE (4) + 5 elements * 8 bytes = 44
                assertEquals(44, iter.byteSize());
            }
        }

        @Test
        @DisplayName("Should calculate byte size for empty group")
        void shouldCalculateByteSizeForEmptyGroup() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                var intLayout =
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN);

                segment.set(intLayout, 0, 0); // count = 0

                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 0);

                // COUNT_SIZE (4) + 0 elements = 4
                assertEquals(4, iter.byteSize());
            }
        }
    }

    @Nested
    @DisplayName("Element Offset")
    class ElementOffsetTests {

        @Test
        @DisplayName("Should return correct element offsets")
        void shouldReturnCorrectElementOffsets() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);
                var intLayout =
                        java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED.withOrder(
                                java.nio.ByteOrder.BIG_ENDIAN);

                segment.set(intLayout, 0, 3);

                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 0);

                // Data starts at offset 4 (after count)
                assertEquals(4, iter.elementOffset(0));
                assertEquals(12, iter.elementOffset(1));
                assertEquals(20, iter.elementOffset(2));
            }
        }
    }
}
