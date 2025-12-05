package express.mvp.myra.codec.runtime.struct;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for RepeatingGroupBuilder - building fixed-size primitive arrays.
 */
@DisplayName("RepeatingGroupBuilder Tests")
class RepeatingGroupBuilderTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("Should accept positive element size")
        void shouldAcceptPositiveElementSize() {
            assertDoesNotThrow(() -> new RepeatingGroupBuilder(8));
            assertDoesNotThrow(() -> new RepeatingGroupBuilder(1));
            assertDoesNotThrow(() -> new RepeatingGroupBuilder(4));
        }

        @Test
        @DisplayName("Should reject invalid element sizes")
        void shouldRejectInvalidElementSizes() {
            assertThrows(IllegalArgumentException.class, () -> new RepeatingGroupBuilder(0));
            assertThrows(IllegalArgumentException.class, () -> new RepeatingGroupBuilder(-1));
        }
    }

    @Nested
    @DisplayName("Writing Long Elements")
    class LongWriteTests {

        @Test
        @DisplayName("Should write long elements and finish correctly")
        void shouldWriteLongElementsAndFinishCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(8);
                builder.wrap(segment, 0);
                builder.addLong(100L);
                builder.addLong(200L);
                builder.addLong(300L);
                int bytesWritten = builder.finish();

                // 4 (count) + 3 * 8 (elements) = 28
                assertEquals(28, bytesWritten);

                // Verify with iterator
                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 0);

                assertEquals(3, iter.count());
                assertEquals(100L, iter.getLongAt(0));
                assertEquals(200L, iter.getLongAt(1));
                assertEquals(300L, iter.getLongAt(2));
            }
        }

        @Test
        @DisplayName("Should write empty group")
        void shouldWriteEmptyGroup() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(8);
                builder.wrap(segment, 0);
                int bytesWritten = builder.finish();

                assertEquals(4, bytesWritten); // Just count field

                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 0);
                assertEquals(0, iter.count());
            }
        }

        @Test
        @DisplayName("Should support bulk long array add")
        void shouldSupportBulkLongArrayAdd() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                long[] values = {10L, 20L, 30L, 40L, 50L};

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(8);
                builder.wrap(segment, 0);
                builder.addLongs(values);
                builder.finish();

                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 0);

                assertEquals(5, iter.count());
                for (int i = 0; i < values.length; i++) {
                    assertEquals(values[i], iter.getLongAt(i));
                }
            }
        }
    }

    @Nested
    @DisplayName("Writing Int Elements")
    class IntWriteTests {

        @Test
        @DisplayName("Should write int elements correctly")
        void shouldWriteIntElementsCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(4);
                builder.wrap(segment, 0);
                builder.addInt(10);
                builder.addInt(20);
                builder.addInt(30);
                builder.addInt(40);
                int bytesWritten = builder.finish();

                // 4 (count) + 4 * 4 (elements) = 20
                assertEquals(20, bytesWritten);

                RepeatingGroupIterator iter = new RepeatingGroupIterator(4);
                iter.wrap(segment, 0);

                assertEquals(4, iter.count());
                assertEquals(10, iter.getIntAt(0));
                assertEquals(20, iter.getIntAt(1));
                assertEquals(30, iter.getIntAt(2));
                assertEquals(40, iter.getIntAt(3));
            }
        }

        @Test
        @DisplayName("Should support bulk int array add")
        void shouldSupportBulkIntArrayAdd() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                int[] values = {1, 2, 3, 4, 5};

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(4);
                builder.wrap(segment, 0);
                builder.addInts(values);
                builder.finish();

                RepeatingGroupIterator iter = new RepeatingGroupIterator(4);
                iter.wrap(segment, 0);

                assertEquals(5, iter.count());
                for (int i = 0; i < values.length; i++) {
                    assertEquals(values[i], iter.getIntAt(i));
                }
            }
        }
    }

    @Nested
    @DisplayName("Writing Other Types")
    class OtherTypeWriteTests {

        @Test
        @DisplayName("Should write byte elements correctly")
        void shouldWriteByteElementsCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(1);
                builder.wrap(segment, 0);
                builder.addByte((byte) 1);
                builder.addByte((byte) 2);
                builder.addByte((byte) 3);
                builder.finish();

                RepeatingGroupIterator iter = new RepeatingGroupIterator(1);
                iter.wrap(segment, 0);

                assertEquals(3, iter.count());
                assertEquals((byte) 1, iter.getByteAt(0));
                assertEquals((byte) 2, iter.getByteAt(1));
                assertEquals((byte) 3, iter.getByteAt(2));
            }
        }

        @Test
        @DisplayName("Should write boolean elements correctly")
        void shouldWriteBooleanElementsCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(1);
                builder.wrap(segment, 0);
                builder.addBoolean(true);
                builder.addBoolean(false);
                builder.addBoolean(true);
                builder.finish();

                RepeatingGroupIterator iter = new RepeatingGroupIterator(1);
                iter.wrap(segment, 0);

                assertEquals(3, iter.count());
                assertTrue(iter.getBooleanAt(0));
                assertFalse(iter.getBooleanAt(1));
                assertTrue(iter.getBooleanAt(2));
            }
        }

        @Test
        @DisplayName("Should write double elements correctly")
        void shouldWriteDoubleElementsCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(8);
                builder.wrap(segment, 0);
                builder.addDouble(3.14);
                builder.addDouble(2.71);
                builder.finish();

                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 0);

                assertEquals(2, iter.count());
                assertEquals(3.14, iter.getDoubleAt(0), 0.001);
                assertEquals(2.71, iter.getDoubleAt(1), 0.001);
            }
        }
    }

    @Nested
    @DisplayName("Writing at Non-Zero Offset")
    class NonZeroOffsetTests {

        @Test
        @DisplayName("Should write at non-zero offset correctly")
        void shouldWriteAtNonZeroOffsetCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                // Write at offset 20
                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(8);
                builder.wrap(segment, 20);
                builder.addLong(100L);
                builder.addLong(200L);
                builder.finish();

                // Read back from same offset
                RepeatingGroupIterator iter = new RepeatingGroupIterator(8);
                iter.wrap(segment, 20);

                assertEquals(2, iter.count());
                assertEquals(100L, iter.getLongAt(0));
                assertEquals(200L, iter.getLongAt(1));
            }
        }
    }

    @Nested
    @DisplayName("Builder State")
    class BuilderStateTests {

        @Test
        @DisplayName("Should track count correctly")
        void shouldTrackCountCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(8);
                builder.wrap(segment, 0);

                assertEquals(0, builder.count());
                builder.addLong(1L);
                assertEquals(1, builder.count());
                builder.addLong(2L);
                assertEquals(2, builder.count());
            }
        }

        @Test
        @DisplayName("Should track current byte size correctly")
        void shouldTrackCurrentByteSizeCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(8);
                builder.wrap(segment, 0);

                assertEquals(4, builder.currentByteSize()); // Just count field
                builder.addLong(1L);
                assertEquals(12, builder.currentByteSize()); // count + 1 element
                builder.addLong(2L);
                assertEquals(20, builder.currentByteSize()); // count + 2 elements
            }
        }

        @Test
        @DisplayName("Should reset state correctly")
        void shouldResetStateCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(8);
                builder.wrap(segment, 0);
                builder.addLong(1L);
                builder.addLong(2L);

                builder.reset();

                assertEquals(0, builder.count());
            }
        }
    }

    @Nested
    @DisplayName("Builder Reuse")
    class BuilderReuseTests {

        @Test
        @DisplayName("Should support reuse after finish")
        void shouldSupportReuseAfterFinish() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                RepeatingGroupBuilder builder = new RepeatingGroupBuilder(4);

                // First use
                builder.wrap(segment, 0);
                builder.addInt(1);
                builder.addInt(2);
                builder.finish();

                // Second use at different offset
                builder.wrap(segment, 50);
                builder.addInt(10);
                builder.addInt(20);
                builder.addInt(30);
                builder.finish();

                // Verify both groups
                RepeatingGroupIterator iter = new RepeatingGroupIterator(4);

                iter.wrap(segment, 0);
                assertEquals(2, iter.count());
                assertEquals(1, iter.getIntAt(0));
                assertEquals(2, iter.getIntAt(1));

                iter.wrap(segment, 50);
                assertEquals(3, iter.count());
                assertEquals(10, iter.getIntAt(0));
                assertEquals(20, iter.getIntAt(1));
                assertEquals(30, iter.getIntAt(2));
            }
        }
    }
}
