package express.mvp.myra.codec.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.palantir.javapoet.JavaFile;
import express.mvp.myra.codec.codegen.resolver.*;
import express.mvp.myra.codec.schema.EnumValueDefinition;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StubGeneratorTest {

    @TempDir Path tempDir;

    @Test
    void generate_WithSimpleMessage_ShouldCreateFlyweight() {
        ResolvedSchemaDefinition schema = createSimpleSchema();
        StubGenerator generator = new StubGenerator(schema);

        List<JavaFile> files = generator.generate();

        assertNotNull(files);
        assertFalse(files.isEmpty());
        assertTrue(files.stream().anyMatch(f -> f.toString().contains("TestMessageFlyweight")));
    }

    @Test
    void generate_WithEnum_ShouldCreateEnumClass() {
        ResolvedSchemaDefinition schema = createSchemaWithEnum();
        StubGenerator generator = new StubGenerator(schema);

        List<JavaFile> files = generator.generate();

        assertTrue(files.stream().anyMatch(f -> f.toString().contains("StatusCode")));
    }

    @Test
    void writeFiles_ShouldCreateDirectoryStructure() throws Exception {
        ResolvedSchemaDefinition schema = createSimpleSchema();
        StubGenerator generator = new StubGenerator(schema);

        generator.writeFiles(tempDir);

        Path packageDir = tempDir.resolve("com/test");
        assertTrue(Files.exists(packageDir));
        assertTrue(Files.list(packageDir).findAny().isPresent());
    }

    @Test
    void generate_WithFixedAndVariableFields_ShouldHandleBoth() {
        ResolvedSchemaDefinition schema =
                new ResolvedSchemaDefinition(
                        "com.test",
                        "1.0.0",
                        List.of(
                                new ResolvedMessageDefinition(
                                        "MixedMessage",
                                        1,
                                        List.of(
                                                new ResolvedFieldDefinition(
                                                        "id", 1, 1, "int32", false, false, false,
                                                        "", null),
                                                new ResolvedFieldDefinition(
                                                        "name", 2, 2, "string", false, false, false,
                                                        "", null),
                                                new ResolvedFieldDefinition(
                                                        "flag", 3, 3, "bool", false, false, false,
                                                        "", null)))),
                        List.of());

        StubGenerator generator = new StubGenerator(schema);
        List<JavaFile> files = generator.generate();

        assertTrue(files.stream().anyMatch(f -> f.toString().contains("MixedMessageFlyweight")));
    }

    @Test
    void generate_WithEmptyMessage_ShouldNotGenerateFlyweight() {
        ResolvedSchemaDefinition schema =
                new ResolvedSchemaDefinition(
                        "com.test",
                        "1.0.0",
                        List.of(new ResolvedMessageDefinition("EmptyMessage", 1, List.of())),
                        List.of());

        StubGenerator generator = new StubGenerator(schema);
        List<JavaFile> files = generator.generate();

        assertFalse(files.stream().anyMatch(f -> f.toString().contains("EmptyMessageFlyweight")));
    }

    @Test
    void generate_ShouldEmitBuilderWithPresenceChecksAndVarSupport() {
        ResolvedSchemaDefinition schema =
                new ResolvedSchemaDefinition(
                        "com.test",
                        "1.0.0",
                        List.of(
                                new ResolvedMessageDefinition(
                                        "Trade",
                                        42,
                                        List.of(
                                                new ResolvedFieldDefinition(
                                                        "id", 1, 1, "int64", false, false, false,
                                                        "", null),
                                                new ResolvedFieldDefinition(
                                                        "symbol", 2, 2, "string", false, false,
                                                        false, "", 8),
                                                new ResolvedFieldDefinition(
                                                        "notes", 3, 3, "string", true, false, false,
                                                        "", null)))),
                        List.of());

        assertNotNull(schema.messages().getFirst().fields().get(1).fixedCapacity());

        List<JavaFile> files = new StubGenerator(schema).generate();
        JavaFile builderFile =
                files.stream()
                        .filter(f -> f.toString().contains("class TradeBuilder"))
                        .findFirst()
                        .orElseThrow();

        String source = builderFile.toString();
        assertTrue(source.contains("Single-pass, write-once builder"));
        assertTrue(source.contains("REQUIRED_FIELD_INDEXES"));
        assertTrue(source.contains("Missing required field"));
        assertTrue(source.contains("VarFieldWriter"));
        assertTrue(source.contains("SYMBOL_FIXED_CAPACITY"));
        assertTrue(source.contains("NOTES_VAR_SLOT"));
    }

    @Test
    void generate_ShouldEnforceFixedCapacityStrings() {
        ResolvedSchemaDefinition schema =
                new ResolvedSchemaDefinition(
                        "com.test",
                        "1.0.0",
                        List.of(
                                new ResolvedMessageDefinition(
                                        "Quote",
                                        99,
                                        List.of(
                                                new ResolvedFieldDefinition(
                                                        "symbol", 1, 1, "string", false, false,
                                                        false, "", 4)))),
                        List.of());

        JavaFile builderFile =
                new StubGenerator(schema)
                        .generate().stream()
                                .filter(f -> f.toString().contains("class QuoteBuilder"))
                                .findFirst()
                                .orElseThrow();

        String source = builderFile.toString();
        assertTrue(source.contains("SYMBOL_FIXED_CAPACITY"));
        assertTrue(source.contains("utf8Length"));
        assertTrue(source.contains("INT_BE"));
        assertTrue(source.contains("copy(scratchBuffer"));
    }

    private ResolvedSchemaDefinition createSimpleSchema() {
        return new ResolvedSchemaDefinition(
                "com.test",
                "1.0.0",
                List.of(
                        new ResolvedMessageDefinition(
                                "TestMessage",
                                1,
                                List.of(
                                        new ResolvedFieldDefinition(
                                                "field1", 1, 1, "int32", false, false, false, "",
                                                null),
                                        new ResolvedFieldDefinition(
                                                "field2", 2, 2, "bool", false, false, false, "",
                                                null)))),
                List.of());
    }

    private ResolvedSchemaDefinition createSchemaWithEnum() {
        return new ResolvedSchemaDefinition(
                "com.test",
                "1.0.0",
                List.of(),
                List.of(
                        new ResolvedEnumDefinition(
                                "StatusCode",
                                "int8",
                                List.of(
                                        new EnumValueDefinition("OK", 0),
                                        new EnumValueDefinition("ERROR", 1)))));
    }
}
