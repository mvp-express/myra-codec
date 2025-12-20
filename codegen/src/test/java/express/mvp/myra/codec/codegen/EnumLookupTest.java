package express.mvp.myra.codec.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.palantir.javapoet.JavaFile;
import express.mvp.myra.codec.codegen.resolver.ResolvedEnumDefinition;
import express.mvp.myra.codec.codegen.resolver.ResolvedSchemaDefinition;
import express.mvp.myra.codec.schema.EnumValueDefinition;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for O(1) enum lookup array generation in StubGenerator. */
@DisplayName("Enum O(1) Lookup Array Generation")
class EnumLookupTest {

    private static final String NAMESPACE = "test.enums";

    private ResolvedSchemaDefinition createSchemaWithEnum(String enumName, int... ids) {
        List<EnumValueDefinition> values = new ArrayList<>();
        for (int i = 0; i < ids.length; i++) {
            values.add(new EnumValueDefinition("VALUE_" + i, ids[i]));
        }

        return new ResolvedSchemaDefinition(
                NAMESPACE,
                "1.0.0",
                List.of(), // No messages
                List.of(new ResolvedEnumDefinition(enumName, "int32", values)));
    }

    private String findEnumSource(List<JavaFile> files, String enumName) {
        for (JavaFile file : files) {
            String source = file.toString();
            if (source.contains("enum " + enumName)) {
                return source;
            }
        }
        return null;
    }

    @Nested
    @DisplayName("O(1) Lookup Array Generation")
    class LookupArrayTests {

        @Test
        @DisplayName("Should generate VALUES_BY_ID array for small ID ranges")
        void shouldGenerateValuesArrayForSmallIdRange() {
            ResolvedSchemaDefinition schema = createSchemaWithEnum("Side", 0, 1);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "Side");

            assertNotNull(enumSource, "Side enum should be generated");
            assertTrue(
                    enumSource.contains("VALUES_BY_ID"),
                    "Should generate VALUES_BY_ID lookup array");
            assertTrue(
                    enumSource.contains("private static final Side[] VALUES_BY_ID"),
                    "VALUES_BY_ID should be private static final array");
        }

        @Test
        @DisplayName("Should generate static initializer to populate lookup array")
        void shouldGenerateStaticInitializer() {
            ResolvedSchemaDefinition schema = createSchemaWithEnum("OrderType", 0, 1, 2);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "OrderType");

            assertNotNull(enumSource);
            // Check for static initializer pattern
            assertTrue(
                    enumSource.contains("VALUES_BY_ID = new OrderType["),
                    "Should initialize VALUES_BY_ID array");
            assertTrue(
                    enumSource.contains("for (OrderType e : values())"),
                    "Should iterate over values() in static initializer");
            assertTrue(
                    enumSource.contains("VALUES_BY_ID[e.id] = e"), "Should populate array by id");
        }

        @Test
        @DisplayName("Should generate fromId() method with O(1) lookup")
        void shouldGenerateFromIdMethod() {
            ResolvedSchemaDefinition schema = createSchemaWithEnum("Status", 0, 1, 2, 3);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "Status");

            assertNotNull(enumSource);
            assertTrue(
                    enumSource.contains("public static Status fromId(int id)"),
                    "Should generate fromId method");
            assertTrue(enumSource.contains("VALUES_BY_ID[id]"), "fromId should use array lookup");
        }

        @Test
        @DisplayName("Should generate fromIdOrNull() method for safe lookup")
        void shouldGenerateFromIdOrNullMethod() {
            ResolvedSchemaDefinition schema = createSchemaWithEnum("Priority", 0, 1, 2);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "Priority");

            assertNotNull(enumSource);
            assertTrue(
                    enumSource.contains("public static Priority fromIdOrNull(int id)"),
                    "Should generate fromIdOrNull method");
            assertTrue(
                    enumSource.contains("return null"),
                    "fromIdOrNull should return null for unknown ids");
        }

        @Test
        @DisplayName("Should handle sparse ID ranges correctly")
        void shouldHandleSparseIdRanges() {
            // IDs: 0, 5, 10 - sparse but within reasonable range
            ResolvedSchemaDefinition schema = createSchemaWithEnum("SparseEnum", 0, 5, 10);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "SparseEnum");

            assertNotNull(enumSource);
            // Array size should be maxId + 1 = 11
            assertTrue(
                    enumSource.contains("VALUES_BY_ID = new SparseEnum[11]"),
                    "Array size should accommodate max ID");
            assertTrue(
                    enumSource.contains("VALUES_BY_ID"),
                    "Should still use O(1) lookup for sparse ranges");
        }

        @Test
        @DisplayName("Should handle non-zero starting IDs")
        void shouldHandleNonZeroStartingIds() {
            // IDs starting from 1 (common in FIX protocol)
            ResolvedSchemaDefinition schema = createSchemaWithEnum("Side", 1, 2);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "Side");

            assertNotNull(enumSource);
            assertTrue(
                    enumSource.contains("VALUES_BY_ID"),
                    "Should use O(1) lookup even for non-zero starting IDs");
            assertTrue(enumSource.contains("if (id < 0"), "Should check for negative IDs");
        }
    }

    @Nested
    @DisplayName("Fallback Linear Search")
    class FallbackTests {

        @Test
        @DisplayName("Should fall back to linear search for very large ID ranges")
        void shouldFallbackForLargeIdRanges() {
            // ID 2000 exceeds the 1024 threshold
            ResolvedSchemaDefinition schema = createSchemaWithEnum("LargeIdEnum", 0, 2000);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "LargeIdEnum");

            assertNotNull(enumSource);
            // Should NOT have VALUES_BY_ID array
            assertFalse(
                    enumSource.contains("VALUES_BY_ID"),
                    "Should not generate lookup array for large ID ranges");
            // Should have linear search loop
            assertTrue(
                    enumSource.contains("for (LargeIdEnum e : values())"),
                    "Should use linear search for large ID ranges");
        }

        @Test
        @DisplayName("Should fall back to linear search for negative IDs")
        void shouldFallbackForNegativeIds() {
            // Negative IDs are not compatible with array indexing
            ResolvedSchemaDefinition schema = createSchemaWithEnum("NegativeIdEnum", -1, 0, 1);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "NegativeIdEnum");

            assertNotNull(enumSource);
            // Should NOT have VALUES_BY_ID array
            assertFalse(
                    enumSource.contains("VALUES_BY_ID"),
                    "Should not generate lookup array for negative IDs");
            // Should have linear search
            assertTrue(
                    enumSource.contains("for (NegativeIdEnum e : values())"),
                    "Should use linear search for negative ID ranges");
        }
    }

    @Nested
    @DisplayName("Javadoc Generation")
    class JavadocTests {

        @Test
        @DisplayName("Should include O(1) lookup documentation in enum Javadoc")
        void shouldDocumentO1Lookup() {
            ResolvedSchemaDefinition schema = createSchemaWithEnum("DocumentedEnum", 0, 1);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "DocumentedEnum");

            assertNotNull(enumSource);
            assertTrue(
                    enumSource.contains("O(1) array lookup"),
                    "Should document O(1) lookup capability");
        }

        @Test
        @DisplayName("Should include fromId method documentation")
        void shouldDocumentFromIdMethod() {
            ResolvedSchemaDefinition schema = createSchemaWithEnum("MethodDocEnum", 0, 1);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "MethodDocEnum");

            assertNotNull(enumSource);
            assertTrue(enumSource.contains("@param id"), "Should document id parameter");
            assertTrue(enumSource.contains("@return"), "Should document return value");
            assertTrue(
                    enumSource.contains("@throws IllegalArgumentException"),
                    "Should document exception");
        }
    }

    @Nested
    @DisplayName("Error Handling in Generated Code")
    class ErrorHandlingTests {

        @Test
        @DisplayName("fromId should throw for unknown IDs")
        void fromIdShouldThrowForUnknownIds() {
            ResolvedSchemaDefinition schema = createSchemaWithEnum("ErrorEnum", 0, 1, 2);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "ErrorEnum");

            assertNotNull(enumSource);
            assertTrue(
                    enumSource.contains("throw new IllegalArgumentException"),
                    "fromId should throw IllegalArgumentException for unknown ids");
            assertTrue(
                    enumSource.contains("Unknown enum id"),
                    "Exception message should mention unknown enum id");
        }

        @Test
        @DisplayName("fromId should handle null array slot (sparse IDs)")
        void fromIdShouldHandleNullSlot() {
            ResolvedSchemaDefinition schema = createSchemaWithEnum("SparseCheckEnum", 0, 5);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "SparseCheckEnum");

            assertNotNull(enumSource);
            assertTrue(
                    enumSource.contains("if (result == null)"),
                    "Should check for null result in sparse array");
        }
    }

    @Nested
    @DisplayName("id() Accessor Method")
    class IdAccessorTests {

        @Test
        @DisplayName("Should generate id() accessor with Javadoc")
        void shouldGenerateIdAccessorWithJavadoc() {
            ResolvedSchemaDefinition schema = createSchemaWithEnum("IdAccessorEnum", 0, 1);
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String enumSource = findEnumSource(files, "IdAccessorEnum");

            assertNotNull(enumSource);
            assertTrue(
                    enumSource.contains("public int id()"), "Should generate public id() accessor");
            assertTrue(
                    enumSource.contains("wire-format integer ID"), "Should document id() method");
        }
    }
}
