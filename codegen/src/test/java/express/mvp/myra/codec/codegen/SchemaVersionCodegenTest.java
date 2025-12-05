package express.mvp.myra.codec.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.palantir.javapoet.JavaFile;
import express.mvp.myra.codec.codegen.resolver.ResolvedFieldDefinition;
import express.mvp.myra.codec.codegen.resolver.ResolvedMessageDefinition;
import express.mvp.myra.codec.codegen.resolver.ResolvedSchemaDefinition;
import express.mvp.myra.codec.schema.SchemaVersion;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for schema version constant generation in StubGenerator.
 */
@DisplayName("Schema Version Codegen")
class SchemaVersionCodegenTest {

    private static final String NAMESPACE = "test.schema";

    private ResolvedSchemaDefinition createSchema(String version) {
        return new ResolvedSchemaDefinition(
                NAMESPACE,
                version,
                List.of(
                        new ResolvedMessageDefinition(
                                "TestMessage",
                                1,
                                List.of(
                                        new ResolvedFieldDefinition(
                                                "id", 1, 1, "int64", false, false, false, "",
                                                null)))),
                List.of());
    }

    private String findFlyweightSource(List<JavaFile> files, String className) {
        for (JavaFile file : files) {
            String source = file.toString();
            if (source.contains("class " + className)) {
                return source;
            }
        }
        return null;
    }

    @Nested
    @DisplayName("SCHEMA_VERSION Constant Generation")
    class SchemaVersionConstantTests {

        @Test
        @DisplayName("Should generate SCHEMA_VERSION constant in flyweight")
        void shouldGenerateSchemaVersionConstant() {
            ResolvedSchemaDefinition schema = createSchema("1.0.0");
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "TestMessageFlyweight");

            assertNotNull(flyweightSource);
            assertTrue(flyweightSource.contains("SCHEMA_VERSION"),
                    "Should generate SCHEMA_VERSION constant");
            assertTrue(flyweightSource.contains("public static final short SCHEMA_VERSION"),
                    "SCHEMA_VERSION should be public static final short");
        }

        @Test
        @DisplayName("Should encode version 1.0 as wire format 256")
        void shouldEncodeVersion1_0Correctly() {
            ResolvedSchemaDefinition schema = createSchema("1.0.0");
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "TestMessageFlyweight");

            assertNotNull(flyweightSource);
            // version 1.0 = (1 * 256) + 0 = 256
            assertTrue(flyweightSource.contains("(short) 256"),
                    "Version 1.0 should be encoded as 256");
        }

        @Test
        @DisplayName("Should encode version 2.5 as wire format 517")
        void shouldEncodeVersion2_5Correctly() {
            ResolvedSchemaDefinition schema = createSchema("2.5.0");
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "TestMessageFlyweight");

            assertNotNull(flyweightSource);
            // version 2.5 = (2 * 256) + 5 = 517
            assertTrue(flyweightSource.contains("(short) 517"),
                    "Version 2.5 should be encoded as 517");
        }

        @Test
        @DisplayName("Should include version in Javadoc comment")
        void shouldIncludeVersionInJavadoc() {
            ResolvedSchemaDefinition schema = createSchema("3.10.5");
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "TestMessageFlyweight");

            assertNotNull(flyweightSource);
            assertTrue(flyweightSource.contains("3.10"),
                    "Should include version string in Javadoc");
        }
    }

    @Nested
    @DisplayName("Version Format Variations")
    class VersionFormatTests {

        @Test
        @DisplayName("Should handle version without patch")
        void shouldHandleVersionWithoutPatch() {
            ResolvedSchemaDefinition schema = createSchema("1.5");
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "TestMessageFlyweight");

            assertNotNull(flyweightSource);
            // version 1.5 = (1 * 256) + 5 = 261
            assertTrue(flyweightSource.contains("(short) 261"),
                    "Version 1.5 should be encoded as 261");
        }

        @Test
        @DisplayName("Should handle version 0.1.0")
        void shouldHandleVersionZeroOne() {
            ResolvedSchemaDefinition schema = createSchema("0.1.0");
            StubGenerator generator = new StubGenerator(schema);

            List<JavaFile> files = generator.generate();
            String flyweightSource = findFlyweightSource(files, "TestMessageFlyweight");

            assertNotNull(flyweightSource);
            // version 0.1 = (0 * 256) + 1 = 1
            assertTrue(flyweightSource.contains("(short) 1"),
                    "Version 0.1 should be encoded as 1");
        }
    }

    @Nested
    @DisplayName("Runtime Compatibility Check Pattern")
    class CompatibilityPatternTests {

        @Test
        @DisplayName("Wire format can be used to check compatibility at runtime")
        void wireFormatCanBeUsedForCompatibilityCheck() {
            // Simulates runtime compatibility check pattern
            SchemaVersion decoderSchema = SchemaVersion.parse("2.5.0");
            short decoderWireVersion = decoderSchema.toWireFormat();

            // Message from older schema
            SchemaVersion messageSchema = SchemaVersion.parse("2.3.0");
            short messageWireVersion = messageSchema.toWireFormat();

            // Check major versions match (high byte)
            int decoderMajor = decoderWireVersion / 256;
            int messageMajor = messageWireVersion / 256;
            assertEquals(decoderMajor, messageMajor, "Major versions should match");

            // Check message minor <= decoder minor
            int decoderMinor = decoderWireVersion % 256;
            int messageMinor = messageWireVersion % 256;
            assertTrue(messageMinor <= decoderMinor,
                    "Message minor version should be <= decoder minor version");
        }
    }
}
