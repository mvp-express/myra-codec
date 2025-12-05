package express.mvp.myra.codec.codegen;

import static org.junit.jupiter.api.Assertions.*;

import express.mvp.myra.codec.schema.SchemaDefinition;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SchemaParserEdgeCasesTest {

    @TempDir Path tempDir;

    @Test
    void parse_WithNonExistentFile_ShouldThrowException() {
        SchemaParser parser = new SchemaParser();
        Path nonExistent = tempDir.resolve("nonexistent.myra.yml");

        assertThrows(IOException.class, () -> parser.parse(nonExistent));
    }

    @Test
    void parse_WithInvalidYaml_ShouldThrowException() throws Exception {
        Path invalidYaml = tempDir.resolve("invalid.myra.yml");
        Files.writeString(invalidYaml, "invalid: [yaml: content");

        SchemaParser parser = new SchemaParser();
        assertThrows(Exception.class, () -> parser.parse(invalidYaml));
    }

    @Test
    void parse_WithMinimalSchema_ShouldSucceed() throws Exception {
        Path minimalSchema = tempDir.resolve("minimal.myra.yml");
        Files.writeString(
                minimalSchema,
                """
                namespace: "com.test"
                version: "1.0.0"
                messages: []
                enums: []
                """);

        SchemaParser parser = new SchemaParser();
        SchemaDefinition schema = parser.parse(minimalSchema);

        assertNotNull(schema);
        assertEquals("com.test", schema.namespace());
        assertEquals(0, schema.messages().size());
    }

    @Test
    void parse_WithOptionalAndRepeatedFields_ShouldParseCorrectly() throws Exception {
        Path schemaPath = tempDir.resolve("optional.myra.yml");
        Files.writeString(
                schemaPath,
                """
                namespace: "com.test"
                version: "1.0.0"
                messages:
                  - name: "TestMessage"
                    fields:
                      - tag: 1
                        name: "required"
                        type: "int32"
                      - tag: 2
                        name: "optional"
                        type: "string"
                        optional: true
                      - tag: 3
                        name: "repeated"
                        type: "int32"
                        repeated: true
                enums: []
                """);

        SchemaParser parser = new SchemaParser();
        SchemaDefinition schema = parser.parse(schemaPath);

        assertEquals(1, schema.messages().size());
        assertEquals(3, schema.messages().getFirst().fields().size());
        assertTrue(schema.messages().getFirst().fields().get(1).optional());
        assertTrue(schema.messages().getFirst().fields().get(2).repeated());
    }

    @Test
    void parse_WithDeprecatedFields_ShouldParseCorrectly() throws Exception {
        Path schemaPath = tempDir.resolve("deprecated.myra.yml");
        Files.writeString(
                schemaPath,
                """
                namespace: "com.test"
                version: "1.0.0"
                messages:
                  - name: "TestMessage"
                    fields:
                      - tag: 1
                        name: "oldField"
                        type: "int32"
                        deprecated: true
                        deprecationNote: "Use newField instead"
                enums: []
                """);

        SchemaParser parser = new SchemaParser();
        SchemaDefinition schema = parser.parse(schemaPath);

        assertTrue(schema.messages().getFirst().fields().getFirst().deprecated());
        assertEquals(
                "Use newField instead",
                schema.messages().getFirst().fields().getFirst().deprecationNote());
    }

    @Test
    void parse_WithFixedCapacityString_ShouldCaptureMetadata() throws Exception {
        Path schemaPath = tempDir.resolve("fixed-capacity.myra.yml");
        Files.writeString(
                schemaPath,
                """
                namespace: \"com.test\"
                version: \"1.0.0\"
                messages:
                  - name: \"TestMessage\"
                    fields:
                      - tag: 1
                        name: \"symbol\"
                        type: \"string\"
                        fixed_capacity: 12
                enums: []
                """);

        SchemaParser parser = new SchemaParser();
        SchemaDefinition schema = parser.parse(schemaPath);

        assertEquals(12, schema.messages().getFirst().fields().getFirst().fixedCapacity());
    }
}
