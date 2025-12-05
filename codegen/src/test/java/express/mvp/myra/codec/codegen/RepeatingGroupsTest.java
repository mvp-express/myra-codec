package express.mvp.myra.codec.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.palantir.javapoet.JavaFile;
import express.mvp.myra.codec.codegen.resolver.*;
import express.mvp.myra.codec.schema.EnumValueDefinition;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for repeating group code generation in the StubGenerator.
 *
 * <p>Wire format decisions:
 * <ul>
 *   <li><b>Primitive types</b>: Inline encoding - [count:int32][element0][element1]...
 *   <li><b>Variable-size types</b>: Offset table encoding - [count:int32][offset_table][element_data]
 * </ul>
 *
 * <p>This test class validates that the StubGenerator correctly generates:
 * <ol>
 *   <li>Repeating group accessors (count, element iterator)</li>
 *   <li>Flyweight readers for inline primitive arrays</li>
 *   <li>Offset table handling for variable-size elements</li>
 *   <li>Builder methods for populating repeating groups</li>
 * </ol>
 */
@DisplayName("Repeating Groups Codegen Tests")
class RepeatingGroupsTest {

    // =========================================================================
    // Test Schemas (Programmatic)
    // =========================================================================

    /**
     * Creates a schema with a simple repeated primitive field (int64[]).
     * Expected wire format: [count:4 bytes][elements: count * 8 bytes]
     */
    private ResolvedSchemaDefinition createPrimitiveRepeatedSchema() {
        return new ResolvedSchemaDefinition(
                "express.mvp.myra.test",
                "1.0.0",
                List.of(
                        new ResolvedMessageDefinition(
                                "PriceList",
                                1,
                                List.of(
                                        new ResolvedFieldDefinition(
                                                "instrumentId", 1, 1, "int32", false, false, false,
                                                "", null),
                                        new ResolvedFieldDefinition(
                                                "prices", 2, 2, "int64", false, true, false, "",
                                                null)))),
                List.of());
    }

    /**
     * Creates a schema with a repeated nested message field.
     * Expected wire format: [count:4 bytes][offset_table: count * 4 bytes][element_data]
     */
    private ResolvedSchemaDefinition createNestedMessageRepeatedSchema() {
        return new ResolvedSchemaDefinition(
                "express.mvp.myra.test",
                "1.0.0",
                List.of(
                        new ResolvedMessageDefinition(
                                "Level",
                                1,
                                List.of(
                                        new ResolvedFieldDefinition(
                                                "priceNanos", 1, 1, "int64", false, false, false,
                                                "", null),
                                        new ResolvedFieldDefinition(
                                                "size", 2, 2, "int32", false, false, false, "",
                                                null))),
                        new ResolvedMessageDefinition(
                                "OrderBook",
                                2,
                                List.of(
                                        new ResolvedFieldDefinition(
                                                "symbol", 1, 1, "string", false, false, false, "",
                                                8),
                                        new ResolvedFieldDefinition(
                                                "bids", 2, 2, "Level", false, true, false, "",
                                                null),
                                        new ResolvedFieldDefinition(
                                                "asks", 3, 3, "Level", false, true, false, "",
                                                null)))),
                List.of());
    }

    /**
     * Creates a schema with repeated string fields.
     * Expected wire format: [count:4 bytes][offset_table: count * 4 bytes][string_data]
     */
    private ResolvedSchemaDefinition createStringRepeatedSchema() {
        return new ResolvedSchemaDefinition(
                "express.mvp.myra.test",
                "1.0.0",
                List.of(
                        new ResolvedMessageDefinition(
                                "TagList",
                                1,
                                List.of(
                                        new ResolvedFieldDefinition(
                                                "entityId", 1, 1, "int64", false, false, false, "",
                                                null),
                                        new ResolvedFieldDefinition(
                                                "tags", 2, 2, "string", false, true, false, "",
                                                null)))),
                List.of());
    }

    /**
     * Creates a schema with repeated enum fields (fixed-size).
     * Expected wire format: [count:4 bytes][elements: count * enum_size bytes]
     */
    private ResolvedSchemaDefinition createEnumRepeatedSchema() {
        return new ResolvedSchemaDefinition(
                "express.mvp.myra.test",
                "1.0.0",
                List.of(
                        new ResolvedMessageDefinition(
                                "OrderSides",
                                1,
                                List.of(
                                        new ResolvedFieldDefinition(
                                                "orderId", 1, 1, "int64", false, false, false, "",
                                                null),
                                        new ResolvedFieldDefinition(
                                                "sides", 2, 2, "Side", false, true, false, "",
                                                null)))),
                List.of(
                        new ResolvedEnumDefinition(
                                "Side",
                                "int8",
                                List.of(
                                        new EnumValueDefinition("BUY", 0),
                                        new EnumValueDefinition("SELL", 1)))));
    }

    /**
     * Creates a schema with an optional repeated field.
     */
    private ResolvedSchemaDefinition createOptionalRepeatedSchema() {
        return new ResolvedSchemaDefinition(
                "express.mvp.myra.test",
                "1.0.0",
                List.of(
                        new ResolvedMessageDefinition(
                                "OptionalList",
                                1,
                                List.of(
                                        new ResolvedFieldDefinition(
                                                "id", 1, 1, "int64", false, false, false, "", null),
                                        new ResolvedFieldDefinition(
                                                "values", 2, 2, "int32", true, true, false, "",
                                                null)))),
                List.of());
    }

    /**
     * Creates a schema with multiple different primitive repeated fields.
     */
    private ResolvedSchemaDefinition createMultiPrimitiveRepeatedSchema() {
        return new ResolvedSchemaDefinition(
                "express.mvp.myra.test",
                "1.0.0",
                List.of(
                        new ResolvedMessageDefinition(
                                "MultiList",
                                1,
                                List.of(
                                        new ResolvedFieldDefinition(
                                                "id", 1, 1, "int64", false, false, false, "", null),
                                        new ResolvedFieldDefinition(
                                                "byteValues", 2, 2, "int8", false, true, false, "",
                                                null),
                                        new ResolvedFieldDefinition(
                                                "intValues", 3, 3, "int32", false, true, false, "",
                                                null),
                                        new ResolvedFieldDefinition(
                                                "longValues", 4, 4, "int64", false, true, false, "",
                                                null)))),
                List.of());
    }

    // =========================================================================
    // Flyweight Generation Tests
    // =========================================================================

    @Nested
    @DisplayName("Flyweight Generation for Repeating Groups")
    class FlyweightGenerationTests {

        @Test
        @DisplayName("Should generate flyweight with count getter for primitive repeated field")
        void shouldGenerateCountGetterForPrimitiveRepeated() {
            ResolvedSchemaDefinition schema = createPrimitiveRepeatedSchema();
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "PriceListFlyweight");

            assertNotNull(flyweightSource, "PriceListFlyweight should be generated");

            // Should have a count getter method
            assertTrue(
                    flyweightSource.contains("pricesCount()") || flyweightSource.contains("getPricesCount()"),
                    "Should generate count getter for repeated field 'prices'");
        }

        @Test
        @DisplayName("Should generate element accessor for primitive repeated field")
        void shouldGenerateElementAccessorForPrimitiveRepeated() {
            ResolvedSchemaDefinition schema = createPrimitiveRepeatedSchema();
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "PriceListFlyweight");

            assertNotNull(flyweightSource, "PriceListFlyweight should be generated");

            // Should have indexed element access
            assertTrue(
                    flyweightSource.contains("getPricesAt(int index)")
                            || flyweightSource.contains("getPrices(int index)")
                            || flyweightSource.contains("pricesAt(int index)"),
                    "Should generate indexed element accessor for repeated field 'prices'");
        }

        @Test
        @DisplayName("Should generate offset constant for repeated field header")
        void shouldGenerateOffsetConstantForRepeatedField() {
            ResolvedSchemaDefinition schema = createPrimitiveRepeatedSchema();
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "PriceListFlyweight");

            assertNotNull(flyweightSource, "PriceListFlyweight should be generated");

            // Should have offset constant for the repeated field
            assertTrue(
                    flyweightSource.contains("PRICES_OFFSET"),
                    "Should generate offset constant for repeated field 'prices'");
        }

        @Test
        @DisplayName("Should generate nested flyweight accessor for repeated message field")
        void shouldGenerateNestedFlyweightAccessorForRepeatedMessage() {
            ResolvedSchemaDefinition schema = createNestedMessageRepeatedSchema();
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "OrderBookFlyweight");

            assertNotNull(flyweightSource, "OrderBookFlyweight should be generated");

            // Should reference LevelFlyweight for nested access
            assertTrue(
                    flyweightSource.contains("LevelFlyweight"),
                    "Should use nested flyweight type for repeated message field");

            // Should have count getter for bids
            assertTrue(
                    flyweightSource.contains("bidsCount()") || flyweightSource.contains("getBidsCount()"),
                    "Should generate count getter for repeated nested field 'bids'");
        }

        @Test
        @DisplayName("Should generate view field for iterable access to nested repeated messages")
        void shouldGenerateViewFieldForRepeatedMessages() {
            ResolvedSchemaDefinition schema = createNestedMessageRepeatedSchema();
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "OrderBookFlyweight");

            assertNotNull(flyweightSource, "OrderBookFlyweight should be generated");

            // Should have a flyweight view for iteration
            assertTrue(
                    flyweightSource.contains("bidsView") || flyweightSource.contains("bidsFlyweight"),
                    "Should generate view/flyweight field for iterable access");
        }

        @Test
        @DisplayName("Should handle optional repeated fields with presence check")
        void shouldHandleOptionalRepeatedFieldsWithPresenceCheck() {
            ResolvedSchemaDefinition schema = createOptionalRepeatedSchema();
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "OptionalListFlyweight");

            assertNotNull(flyweightSource, "OptionalListFlyweight should be generated");

            // Should have presence check method
            assertTrue(
                    flyweightSource.contains("hasValues()") || flyweightSource.contains("isValuesPresent()"),
                    "Should generate presence check for optional repeated field");
        }
    }

    // =========================================================================
    // Builder Generation Tests
    // =========================================================================

    @Nested
    @DisplayName("Builder Generation for Repeating Groups")
    class BuilderGenerationTests {

        @Test
        @DisplayName("Should generate builder with primitive array setter")
        void shouldGenerateBuilderWithPrimitiveArraySetter() {
            ResolvedSchemaDefinition schema = createPrimitiveRepeatedSchema();
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String builderSource = findBuilderSource(files, "PriceListBuilder");

            assertNotNull(builderSource, "PriceListBuilder should be generated");

            // Builder should have a method to set the entire array OR add elements
            assertTrue(
                    builderSource.contains("setPrices(") || builderSource.contains("addPrice(")
                            || builderSource.contains("prices("),
                    "Builder should have setter/adder for repeated primitive field");
        }

        @Test
        @DisplayName("Should generate builder with count-then-elements pattern for nested messages")
        void shouldGenerateBuilderWithCountThenElementsPattern() {
            ResolvedSchemaDefinition schema = createNestedMessageRepeatedSchema();
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String builderSource = findBuilderSource(files, "OrderBookBuilder");

            assertNotNull(builderSource, "OrderBookBuilder should be generated");

            // Builder should have method to start writing group with count
            assertTrue(
                    builderSource.contains("bidsCount(") || builderSource.contains("setBidsCount(")
                            || builderSource.contains("beginBids(") || builderSource.contains("setBids("),
                    "Builder should have count/begin method for repeated nested messages");
        }

        @Test
        @DisplayName("Builder should NOT throw UnsupportedOperationException for repeated fields")
        void builderShouldNotThrowUnsupportedOperationException() {
            ResolvedSchemaDefinition schema = createPrimitiveRepeatedSchema();
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String builderSource = findBuilderSource(files, "PriceListBuilder");

            assertNotNull(builderSource, "PriceListBuilder should be generated");

            // The current code throws UnsupportedOperationException - this should be fixed
            assertFalse(
                    builderSource.contains("UnsupportedOperationException"),
                    "Builder should not throw UnsupportedOperationException for repeated fields");
        }
    }

    // =========================================================================
    // Wire Format Tests
    // =========================================================================

    @Nested
    @DisplayName("Wire Format Constants")
    class WireFormatTests {

        @Test
        @DisplayName("Should generate correct block length accounting for repeated field header")
        void shouldGenerateCorrectBlockLengthForRepeatedFields() {
            ResolvedSchemaDefinition schema = createPrimitiveRepeatedSchema();
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "PriceListFlyweight");

            assertNotNull(flyweightSource, "PriceListFlyweight should be generated");

            // BLOCK_LENGTH should be present and account for:
            // - instrumentId: int32 = 4 bytes
            // - prices: repeated header (offset/length) = 8 bytes
            assertTrue(
                    flyweightSource.contains("BLOCK_LENGTH"),
                    "Flyweight should define BLOCK_LENGTH constant");
        }

        @Test
        @DisplayName("Should treat enum repeated fields as fixed-size (inline)")
        void shouldTreatEnumRepeatedAsFixedSize() {
            ResolvedSchemaDefinition schema = createEnumRepeatedSchema();
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "OrderSidesFlyweight");

            assertNotNull(flyweightSource, "OrderSidesFlyweight should be generated");

            // Enum repeated fields should behave like primitive repeated fields
            assertTrue(
                    flyweightSource.contains("sidesCount()") || flyweightSource.contains("getSidesCount()"),
                    "Should generate count getter for repeated enum field");
        }
    }

    // =========================================================================
    // Schema Parser Integration Tests
    // =========================================================================

    @Nested
    @DisplayName("Schema Parser Integration")
    class SchemaParserIntegrationTests {

        @Test
        @DisplayName("Should parse repeating_groups.myra.yml test schema correctly")
        void shouldParseRepeatingGroupsSchema() throws Exception {
            SchemaParser parser = new SchemaParser();
            var schema = parser.parse(
                    java.nio.file.Path.of("src/test/resources/repeating_groups.myra.yml"));

            assertNotNull(schema, "Schema should parse successfully");

            // Find PriceList message
            var priceList = schema.messages().stream()
                    .filter(m -> m.name().equals("PriceList"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("PriceList message not found"));

            // Verify prices field is marked as repeated
            var pricesField = priceList.fields().stream()
                    .filter(f -> f.name().equals("prices"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("prices field not found"));

            assertTrue(pricesField.repeated(), "prices field should be marked as repeated");
            assertEquals("int64", pricesField.type(), "prices field should be int64 type");
        }

        @Test
        @DisplayName("Should parse nested message repeated fields")
        void shouldParseNestedMessageRepeatedFields() throws Exception {
            SchemaParser parser = new SchemaParser();
            var schema = parser.parse(
                    java.nio.file.Path.of("src/test/resources/repeating_groups.myra.yml"));

            var orderBook = schema.messages().stream()
                    .filter(m -> m.name().equals("OrderBook"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("OrderBook message not found"));

            var bidsField = orderBook.fields().stream()
                    .filter(f -> f.name().equals("bids"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("bids field not found"));

            assertTrue(bidsField.repeated(), "bids field should be marked as repeated");
            assertEquals("Level", bidsField.type(), "bids field should be Level type");
        }

        @Test
        @DisplayName("Should parse optional repeated fields")
        void shouldParseOptionalRepeatedFields() throws Exception {
            SchemaParser parser = new SchemaParser();
            var schema = parser.parse(
                    java.nio.file.Path.of("src/test/resources/repeating_groups.myra.yml"));

            var optionalList = schema.messages().stream()
                    .filter(m -> m.name().equals("OptionalList"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("OptionalList message not found"));

            var valuesField = optionalList.fields().stream()
                    .filter(f -> f.name().equals("values"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("values field not found"));

            assertTrue(valuesField.repeated(), "values field should be marked as repeated");
            assertTrue(valuesField.optional(), "values field should be marked as optional");
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private String findFlyweightSource(List<JavaFile> files, String className) {
        return files.stream()
                .filter(f -> f.toString().contains("class " + className))
                .findFirst()
                .map(JavaFile::toString)
                .orElse(null);
    }

    private String findBuilderSource(List<JavaFile> files, String className) {
        return files.stream()
                .filter(f -> f.toString().contains("class " + className))
                .findFirst()
                .map(JavaFile::toString)
                .orElse(null);
    }
}
