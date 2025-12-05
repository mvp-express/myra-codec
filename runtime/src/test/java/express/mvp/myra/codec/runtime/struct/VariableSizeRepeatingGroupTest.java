package express.mvp.myra.codec.runtime.struct;

import static org.junit.jupiter.api.Assertions.*;

import express.mvp.roray.utils.memory.Utf8View;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for VariableSizeRepeatingGroupBuilder and VariableSizeRepeatingGroupIterator.
 */
@DisplayName("Variable Size Repeating Group Tests")
class VariableSizeRepeatingGroupTest {

    @Nested
    @DisplayName("String Groups")
    class StringGroupTests {

        @Test
        @DisplayName("Should write and read string group correctly")
        void shouldWriteAndReadStringGroupCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(500);

                // Write strings
                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 3);
                builder.addString("hello");
                builder.addString("world");
                builder.addString("!");
                int bytesWritten = builder.finish();

                assertTrue(bytesWritten > 0);

                // Read strings back
                VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
                iter.wrap(segment, 0);

                assertEquals(3, iter.count());

                Utf8View view = new Utf8View();

                iter.getStringAt(0, view);
                assertEquals("hello", view.toString());

                iter.getStringAt(1, view);
                assertEquals("world", view.toString());

                iter.getStringAt(2, view);
                assertEquals("!", view.toString());
            }
        }

        @Test
        @DisplayName("Should handle empty string group")
        void shouldHandleEmptyStringGroup() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 0);
                int bytesWritten = builder.finish();

                // count(4) + 0 offsets + 0 data = 4
                assertEquals(4, bytesWritten);

                VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
                iter.wrap(segment, 0);

                assertEquals(0, iter.count());
            }
        }

        @Test
        @DisplayName("Should handle single string")
        void shouldHandleSingleString() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 1);
                builder.addString("test");
                builder.finish();

                VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
                iter.wrap(segment, 0);

                assertEquals(1, iter.count());

                Utf8View view = new Utf8View();
                iter.getStringAt(0, view);
                assertEquals("test", view.toString());
            }
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 3);
                builder.addString("");
                builder.addString("middle");
                builder.addString("");
                builder.finish();

                VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
                iter.wrap(segment, 0);

                assertEquals(3, iter.count());

                Utf8View view = new Utf8View();

                iter.getStringAt(0, view);
                assertEquals("", view.toString());

                iter.getStringAt(1, view);
                assertEquals("middle", view.toString());

                iter.getStringAt(2, view);
                assertEquals("", view.toString());
            }
        }

        @Test
        @DisplayName("Should handle Unicode strings")
        void shouldHandleUnicodeStrings() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(500);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 3);
                builder.addString("日本語");
                builder.addString("中文");
                builder.addString("🚀");
                builder.finish();

                VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
                iter.wrap(segment, 0);

                assertEquals(3, iter.count());

                Utf8View view = new Utf8View();

                iter.getStringAt(0, view);
                assertEquals("日本語", view.toString());

                iter.getStringAt(1, view);
                assertEquals("中文", view.toString());

                iter.getStringAt(2, view);
                assertEquals("🚀", view.toString());
            }
        }

        @Test
        @DisplayName("Should iterate over all strings with forEach")
        void shouldIterateOverAllStringsWithForEach() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                String[] expected = {"one", "two", "three"};

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 3);
                for (String s : expected) {
                    builder.addString(s);
                }
                builder.finish();

                VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
                iter.wrap(segment, 0);

                int[] index = {0};
                iter.forEachString(view -> {
                    assertEquals(expected[index[0]], view.toString());
                    index[0]++;
                });

                assertEquals(3, index[0]);
            }
        }
    }

    @Nested
    @DisplayName("Bytes Groups")
    class BytesGroupTests {

        @Test
        @DisplayName("Should write and read bytes group correctly")
        void shouldWriteAndReadBytesGroupCorrectly() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(200);

                byte[] bytes1 = {1, 2, 3, 4, 5};
                byte[] bytes2 = {10, 20};
                byte[] bytes3 = {100};

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 3);
                builder.addBytes(bytes1);
                builder.addBytes(bytes2);
                builder.addBytes(bytes3);
                builder.finish();

                VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
                iter.wrap(segment, 0);

                assertEquals(3, iter.count());

                MemorySegment slice1 = iter.getBytesAt(0);
                assertEquals(5, slice1.byteSize());
                assertEquals((byte) 1, slice1.get(java.lang.foreign.ValueLayout.JAVA_BYTE, 0));
                assertEquals((byte) 5, slice1.get(java.lang.foreign.ValueLayout.JAVA_BYTE, 4));

                MemorySegment slice2 = iter.getBytesAt(1);
                assertEquals(2, slice2.byteSize());
                assertEquals((byte) 10, slice2.get(java.lang.foreign.ValueLayout.JAVA_BYTE, 0));
                assertEquals((byte) 20, slice2.get(java.lang.foreign.ValueLayout.JAVA_BYTE, 1));

                MemorySegment slice3 = iter.getBytesAt(2);
                assertEquals(1, slice3.byteSize());
                assertEquals((byte) 100, slice3.get(java.lang.foreign.ValueLayout.JAVA_BYTE, 0));
            }
        }
    }

    @Nested
    @DisplayName("Builder Validation")
    class BuilderValidationTests {

        @Test
        @DisplayName("Should reject negative count")
        void shouldRejectNegativeCount() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                assertThrows(IllegalArgumentException.class,
                        () -> builder.beginWithCount(segment, 0, -1));
            }
        }

        @Test
        @DisplayName("Should throw when adding more elements than expected")
        void shouldThrowWhenAddingMoreElementsThanExpected() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 2);
                builder.addString("one");
                builder.addString("two");

                assertThrows(IllegalStateException.class, () -> builder.addString("three"));
            }
        }

        @Test
        @DisplayName("Should throw when finishing with fewer elements than expected")
        void shouldThrowWhenFinishingWithFewerElements() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 3);
                builder.addString("one");
                builder.addString("two");

                assertThrows(IllegalStateException.class, builder::finish);
            }
        }

        @Test
        @DisplayName("Should allow partial finish")
        void shouldAllowPartialFinish() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 5);
                builder.addString("one");
                builder.addString("two");
                int bytesWritten = builder.finishPartial();

                assertTrue(bytesWritten > 0);

                // Verify the count was updated to 2
                VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
                iter.wrap(segment, 0);
                assertEquals(2, iter.count());
            }
        }
    }

    @Nested
    @DisplayName("Iterator Validation")
    class IteratorValidationTests {

        @Test
        @DisplayName("Should throw on out-of-bounds string access")
        void shouldThrowOnOutOfBoundsStringAccess() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 2);
                builder.addString("one");
                builder.addString("two");
                builder.finish();

                VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
                iter.wrap(segment, 0);

                Utf8View view = new Utf8View();
                assertThrows(IndexOutOfBoundsException.class, () -> iter.getStringAt(-1, view));
                assertThrows(IndexOutOfBoundsException.class, () -> iter.getStringAt(2, view));
                assertThrows(IndexOutOfBoundsException.class, () -> iter.getStringAt(100, view));
            }
        }

        @Test
        @DisplayName("Should report wrapped state correctly")
        void shouldReportWrappedStateCorrectly() {
            VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
            assertFalse(iter.isWrapped());

            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(100);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 0);
                builder.finish();

                iter.wrap(segment, 0);
                assertTrue(iter.isWrapped());

                iter.reset();
                assertFalse(iter.isWrapped());
            }
        }
    }

    @Nested
    @DisplayName("Non-Zero Offset")
    class NonZeroOffsetTests {

        @Test
        @DisplayName("Should write and read at non-zero offset")
        void shouldWriteAndReadAtNonZeroOffset() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(500);

                // Write at offset 100
                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 100, 2);
                builder.addString("offset");
                builder.addString("test");
                builder.finish();

                // Read from same offset
                VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
                iter.wrap(segment, 100);

                assertEquals(2, iter.count());

                Utf8View view = new Utf8View();
                iter.getStringAt(0, view);
                assertEquals("offset", view.toString());

                iter.getStringAt(1, view);
                assertEquals("test", view.toString());
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
                MemorySegment segment = arena.allocate(500);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();

                // First use
                builder.beginWithCount(segment, 0, 2);
                builder.addString("first");
                builder.addString("group");
                int firstSize = builder.finish();

                // Second use at different offset
                builder.beginWithCount(segment, 200, 3);
                builder.addString("second");
                builder.addString("different");
                builder.addString("group");
                builder.finish();

                // Verify both groups
                VariableSizeRepeatingGroupIterator iter = new VariableSizeRepeatingGroupIterator();
                Utf8View view = new Utf8View();

                iter.wrap(segment, 0);
                assertEquals(2, iter.count());
                iter.getStringAt(0, view);
                assertEquals("first", view.toString());

                iter.wrap(segment, 200);
                assertEquals(3, iter.count());
                iter.getStringAt(2, view);
                assertEquals("group", view.toString());
            }
        }
    }

    @Nested
    @DisplayName("Builder State Tracking")
    class BuilderStateTrackingTests {

        @Test
        @DisplayName("Should track expected and current count")
        void shouldTrackExpectedAndCurrentCount() {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment segment = arena.allocate(200);

                VariableSizeRepeatingGroupBuilder builder = new VariableSizeRepeatingGroupBuilder();
                builder.beginWithCount(segment, 0, 5);

                assertEquals(5, builder.expectedCount());
                assertEquals(0, builder.currentCount());

                builder.addString("one");
                assertEquals(1, builder.currentCount());

                builder.addString("two");
                assertEquals(2, builder.currentCount());
            }
        }
    }
}
