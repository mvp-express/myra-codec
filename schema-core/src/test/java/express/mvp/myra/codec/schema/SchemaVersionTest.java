package express.mvp.myra.codec.schema;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for SchemaVersion parsing, wire format encoding, and compatibility checks. */
@DisplayName("SchemaVersion")
class SchemaVersionTest {

    @Nested
    @DisplayName("Parsing")
    class ParsingTests {

        @ParameterizedTest
        @CsvSource({
            "1.0.0,    1, 0, 0",
            "2.3.5,    2, 3, 5",
            "0.1.0,    0, 1, 0",
            "127.255.999, 127, 255, 999",
            "1.0,      1, 0, 0",
            "2.5,      2, 5, 0",
        })
        @DisplayName("Should parse valid version strings")
        void shouldParseValidVersionStrings(String versionStr, int major, int minor, int patch) {
            SchemaVersion v = SchemaVersion.parse(versionStr);
            assertEquals(major, v.major());
            assertEquals(minor, v.minor());
            assertEquals(patch, v.patch());
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "", "1", "1.2.3.4", "abc", "1.x.0", "-1.0.0", "v1.0.0",
                })
        @DisplayName("Should reject invalid version strings")
        void shouldRejectInvalidVersionStrings(String versionStr) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaVersion.parse(versionStr),
                    "Should reject: " + versionStr);
        }

        @Test
        @DisplayName("Should reject null version string")
        void shouldRejectNullVersionString() {
            assertThrows(NullPointerException.class, () -> SchemaVersion.parse(null));
        }

        @Test
        @DisplayName("Should trim whitespace from version string")
        void shouldTrimWhitespace() {
            SchemaVersion v = SchemaVersion.parse("  1.2.3  ");
            assertEquals(1, v.major());
            assertEquals(2, v.minor());
            assertEquals(3, v.patch());
        }
    }

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorTests {

        @Test
        @DisplayName("Should create valid version")
        void shouldCreateValidVersion() {
            SchemaVersion v = new SchemaVersion(1, 2, 3);
            assertEquals(1, v.major());
            assertEquals(2, v.minor());
            assertEquals(3, v.patch());
        }

        @Test
        @DisplayName("Should reject major version > 127")
        void shouldRejectMajorOverLimit() {
            assertThrows(IllegalArgumentException.class, () -> new SchemaVersion(128, 0, 0));
        }

        @Test
        @DisplayName("Should reject minor version > 255")
        void shouldRejectMinorOverLimit() {
            assertThrows(IllegalArgumentException.class, () -> new SchemaVersion(1, 256, 0));
        }

        @Test
        @DisplayName("Should reject negative versions")
        void shouldRejectNegativeVersions() {
            assertThrows(IllegalArgumentException.class, () -> new SchemaVersion(-1, 0, 0));
            assertThrows(IllegalArgumentException.class, () -> new SchemaVersion(0, -1, 0));
            assertThrows(IllegalArgumentException.class, () -> new SchemaVersion(0, 0, -1));
        }

        @Test
        @DisplayName("Should allow two-arg constructor with default patch")
        void shouldAllowTwoArgConstructor() {
            SchemaVersion v = new SchemaVersion(5, 10);
            assertEquals(5, v.major());
            assertEquals(10, v.minor());
            assertEquals(0, v.patch());
        }
    }

    @Nested
    @DisplayName("Wire Format")
    class WireFormatTests {

        @ParameterizedTest
        @CsvSource({
            "0, 0, 0",
            "0, 1, 1",
            "1, 0, 256",
            "1, 1, 257",
            "2, 5, 517",
            "127, 255, 32767",
        })
        @DisplayName("Should encode to wire format correctly")
        void shouldEncodeToWireFormat(int major, int minor, int expected) {
            SchemaVersion v = new SchemaVersion(major, minor);
            assertEquals((short) expected, v.toWireFormat());
        }

        @ParameterizedTest
        @CsvSource({
            "0, 0, 0",
            "1, 0, 1",
            "256, 1, 0",
            "257, 1, 1",
            "517, 2, 5",
            "32767, 127, 255",
        })
        @DisplayName("Should decode from wire format correctly")
        void shouldDecodeFromWireFormat(short wireVersion, int major, int minor) {
            SchemaVersion v = SchemaVersion.fromWireFormat(wireVersion);
            assertEquals(major, v.major());
            assertEquals(minor, v.minor());
            assertEquals(0, v.patch());
        }

        @Test
        @DisplayName("Should round-trip through wire format")
        void shouldRoundTripThroughWireFormat() {
            SchemaVersion original = new SchemaVersion(5, 25, 100);
            short wire = original.toWireFormat();
            SchemaVersion decoded = SchemaVersion.fromWireFormat(wire);

            assertEquals(original.major(), decoded.major());
            assertEquals(original.minor(), decoded.minor());
            // Note: patch is not preserved in wire format
            assertEquals(0, decoded.patch());
        }
    }

    @Nested
    @DisplayName("Compatibility")
    class CompatibilityTests {

        @Test
        @DisplayName("Same version should be compatible")
        void sameVersionShouldBeCompatible() {
            SchemaVersion v1 = new SchemaVersion(1, 2, 3);
            SchemaVersion v2 = new SchemaVersion(1, 2, 3);
            assertTrue(v1.isCompatibleWith(v2));
            assertTrue(v2.isCompatibleWith(v1));
        }

        @Test
        @DisplayName("Different major versions should NOT be compatible")
        void differentMajorShouldNotBeCompatible() {
            SchemaVersion v1 = new SchemaVersion(1, 5, 0);
            SchemaVersion v2 = new SchemaVersion(2, 5, 0);
            assertFalse(v1.isCompatibleWith(v2));
            assertFalse(v2.isCompatibleWith(v1));
        }

        @Test
        @DisplayName("Older minor should be compatible with newer decoder")
        void olderMinorCompatibleWithNewerDecoder() {
            SchemaVersion message = new SchemaVersion(1, 3, 0);
            SchemaVersion decoder = new SchemaVersion(1, 5, 0);
            assertTrue(
                    message.isCompatibleWith(decoder),
                    "Message v1.3 should be readable by decoder v1.5");
        }

        @Test
        @DisplayName("Newer minor should NOT be compatible with older decoder")
        void newerMinorNotCompatibleWithOlderDecoder() {
            SchemaVersion message = new SchemaVersion(1, 5, 0);
            SchemaVersion decoder = new SchemaVersion(1, 3, 0);
            assertFalse(
                    message.isCompatibleWith(decoder),
                    "Message v1.5 should NOT be readable by decoder v1.3");
        }

        @Test
        @DisplayName("Patch differences should not affect compatibility")
        void patchDifferencesShouldNotAffectCompatibility() {
            SchemaVersion v1 = new SchemaVersion(1, 2, 0);
            SchemaVersion v2 = new SchemaVersion(1, 2, 99);
            assertTrue(v1.isCompatibleWith(v2));
            assertTrue(v2.isCompatibleWith(v1));
        }

        @Test
        @DisplayName("isBreakingChangeFrom should detect major version changes")
        void shouldDetectBreakingChanges() {
            SchemaVersion v1 = new SchemaVersion(1, 0, 0);
            SchemaVersion v2 = new SchemaVersion(2, 0, 0);
            SchemaVersion v3 = new SchemaVersion(1, 5, 0);

            assertTrue(v2.isBreakingChangeFrom(v1));
            assertFalse(v3.isBreakingChangeFrom(v1));
        }
    }

    @Nested
    @DisplayName("Comparison")
    class ComparisonTests {

        @Test
        @DisplayName("Should compare versions correctly")
        void shouldCompareVersionsCorrectly() {
            SchemaVersion v100 = new SchemaVersion(1, 0, 0);
            SchemaVersion v101 = new SchemaVersion(1, 0, 1);
            SchemaVersion v110 = new SchemaVersion(1, 1, 0);
            SchemaVersion v200 = new SchemaVersion(2, 0, 0);

            assertTrue(v100.compareTo(v101) < 0);
            assertTrue(v101.compareTo(v100) > 0);
            assertTrue(v100.compareTo(v110) < 0);
            assertTrue(v110.compareTo(v200) < 0);
            assertEquals(0, v100.compareTo(new SchemaVersion(1, 0, 0)));
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringTests {

        @Test
        @DisplayName("toString should return full version string")
        void toStringShouldReturnFullVersion() {
            assertEquals("1.2.3", new SchemaVersion(1, 2, 3).toString());
            assertEquals("0.0.0", new SchemaVersion(0, 0, 0).toString());
        }

        @Test
        @DisplayName("toShortString should return major.minor")
        void toShortStringShouldReturnMajorMinor() {
            assertEquals("1.2", new SchemaVersion(1, 2, 3).toShortString());
            assertEquals("0.0", new SchemaVersion(0, 0, 5).toShortString());
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityTests {

        @Test
        @DisplayName("Equal versions should be equal")
        void equalVersionsShouldBeEqual() {
            SchemaVersion v1 = new SchemaVersion(1, 2, 3);
            SchemaVersion v2 = new SchemaVersion(1, 2, 3);
            assertEquals(v1, v2);
            assertEquals(v1.hashCode(), v2.hashCode());
        }

        @Test
        @DisplayName("Different versions should not be equal")
        void differentVersionsShouldNotBeEqual() {
            assertNotEquals(new SchemaVersion(1, 2, 3), new SchemaVersion(1, 2, 4));
            assertNotEquals(new SchemaVersion(1, 2, 3), new SchemaVersion(1, 3, 3));
            assertNotEquals(new SchemaVersion(1, 2, 3), new SchemaVersion(2, 2, 3));
        }
    }
}
