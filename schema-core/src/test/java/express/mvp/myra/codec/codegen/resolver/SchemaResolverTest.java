package express.mvp.myra.codec.codegen.resolver;

import static org.junit.jupiter.api.Assertions.*;

import express.mvp.myra.codec.schema.EnumDefinition;
import express.mvp.myra.codec.schema.EnumValueDefinition;
import express.mvp.myra.codec.schema.FieldDefinition;
import express.mvp.myra.codec.schema.MessageDefinition;
import express.mvp.myra.codec.schema.SchemaDefinition;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SchemaResolverTest {

    @TempDir Path tempDir;

    @Test
    void resolve_WithNewSchema_ShouldAssignSequentialIds() {
        SchemaDefinition schema = createTestSchema();

        ResolutionResult result =
                SchemaResolver.resolve(schema, null, tempDir.resolve("test.myra.yml"));

        assertNotNull(result);
        assertNotNull(result.resolvedSchema());
        assertNotNull(result.updatedLockFile());

        assertEquals(2, result.resolvedSchema().messages().size());
        ResolvedMessageDefinition msg1 = result.resolvedSchema().messages().getFirst();
        assertEquals("TestMessage", msg1.name());
        assertEquals(1, msg1.id());
        assertEquals(2, msg1.fields().size());
        assertEquals(1, msg1.fields().getFirst().id());
        assertEquals(1, msg1.fields().getFirst().tag());
        assertEquals(2, msg1.fields().get(1).id());
        assertEquals(2, msg1.fields().get(1).tag());
    }

    @Test
    void resolve_WithExistingLockFile_ShouldPreserveIds() {
        SchemaDefinition schema = createTestSchema();

        LockFile existingLock = LockFile.empty();
        LockFile.MessageLock msgLock = new LockFile.MessageLock();
        msgLock.id = 10;
        msgLock.fields = Map.of("field1", 5, "field2", 6);
        existingLock.messages = Map.of("TestMessage", msgLock);

        ResolutionResult result =
                SchemaResolver.resolve(schema, existingLock, tempDir.resolve("test.myra.yml"));

        ResolvedMessageDefinition msg =
                result.resolvedSchema().messages().stream()
                        .filter(m -> m.name().equals("TestMessage"))
                        .findFirst()
                        .orElseThrow();

        assertEquals(10, msg.id());
        assertEquals(5, msg.fields().getFirst().id());
        assertEquals(6, msg.fields().get(1).id());
    }

    @Test
    void resolve_WithNewField_ShouldAssignNewId() {
        SchemaDefinition schema =
                new SchemaDefinition(
                        "com.test",
                        "1.0.0",
                        List.of(
                                new MessageDefinition(
                                        "TestMessage",
                                        List.of(
                                                new FieldDefinition(
                                                        1, "field1", "int32", false, false, false,
                                                        "", null),
                                                new FieldDefinition(
                                                        2, "field2", "string", false, false, false,
                                                        "", null),
                                                new FieldDefinition(
                                                        3,
                                                        "newField",
                                                        "bool",
                                                        false,
                                                        false,
                                                        false,
                                                        "",
                                                        null)))),
                        List.of());

        LockFile existingLock = LockFile.empty();
        LockFile.MessageLock msgLock = new LockFile.MessageLock();
        msgLock.id = 1;
        msgLock.fields = Map.of("field1", 1, "field2", 2);
        existingLock.messages = Map.of("TestMessage", msgLock);

        ResolutionResult result =
                SchemaResolver.resolve(schema, existingLock, tempDir.resolve("test.myra.yml"));

        ResolvedMessageDefinition msg = result.resolvedSchema().messages().getFirst();
        ResolvedFieldDefinition newField =
                msg.fields().stream()
                        .filter(f -> f.name().equals("newField"))
                        .findFirst()
                        .orElseThrow();

        assertEquals(3, newField.id());
    }

    @Test
    void resolve_WithRemovedField_ShouldReserveFieldId() {
        SchemaDefinition schema =
                new SchemaDefinition(
                        "com.test",
                        "1.0.0",
                        List.of(
                                new MessageDefinition(
                                        "TestMessage",
                                        List.of(
                                                new FieldDefinition(
                                                        1, "field1", "int32", false, false, false,
                                                        "", null)))),
                        List.of());

        LockFile existingLock = LockFile.empty();
        LockFile.MessageLock msgLock = new LockFile.MessageLock();
        msgLock.id = 7;
        msgLock.fields = Map.of("field1", 1, "legacy", 2);
        existingLock.messages = Map.of("TestMessage", msgLock);

        ResolutionResult result =
                SchemaResolver.resolve(schema, existingLock, tempDir.resolve("test.myra.yml"));
        assertTrue(result.updatedLockFile().reservedIds.containsKey("fields"));
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Integer>> reserved =
                (Map<String, Map<String, Integer>>)
                        result.updatedLockFile().reservedIds.get("fields");
        assertEquals(Map.of("legacy", 2), reserved.get("TestMessage"));
    }

    @Test
    void resolve_WithDeletedMessage_ShouldReserveId() {
        SchemaDefinition schema =
                new SchemaDefinition(
                        "com.test",
                        "1.0.0",
                        List.of(
                                new MessageDefinition(
                                        "RemainingMessage",
                                        List.of(
                                                new FieldDefinition(
                                                        1, "field1", "int32", false, false, false,
                                                        "", null)))),
                        List.of());

        LockFile existingLock = LockFile.empty();
        LockFile.MessageLock deletedMsg = new LockFile.MessageLock();
        deletedMsg.id = 5;
        deletedMsg.fields = Map.of("field1", 1);
        existingLock.messages = Map.of("DeletedMessage", deletedMsg);

        ResolutionResult result =
                SchemaResolver.resolve(schema, existingLock, tempDir.resolve("test.myra.yml"));

        assertFalse(result.updatedLockFile().messages.containsKey("DeletedMessage"));
        assertNotNull(result.updatedLockFile().reservedIds);
    }

    @Test
    void resolve_ShouldThrowWhenReservedFieldNameReused() {
        SchemaDefinition schema =
                new SchemaDefinition(
                        "com.test",
                        "1.0.0",
                        List.of(
                                new MessageDefinition(
                                        "TestMessage",
                                        List.of(
                                                new FieldDefinition(
                                                        1, "legacy", "int32", false, false, false,
                                                        "", null)))),
                        List.of());

        LockFile existingLock = LockFile.empty();
        existingLock.messages = Map.of();
        existingLock.reservedIds = Map.of("fields", Map.of("TestMessage", Map.of("legacy", 2)));

        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                SchemaResolver.resolve(
                                        schema, existingLock, tempDir.resolve("test.myra.yml")));
        assertTrue(ex.getMessage().contains("legacy"));
    }

    @Test
    void resolve_ShouldRejectDuplicateTags() {
        SchemaDefinition schema =
                new SchemaDefinition(
                        "com.test",
                        "1.0.0",
                        List.of(
                                new MessageDefinition(
                                        "TestMessage",
                                        List.of(
                                                new FieldDefinition(
                                                        1, "field1", "int32", false, false, false,
                                                        "", null),
                                                new FieldDefinition(
                                                        1, "field2", "int32", false, false, false,
                                                        "", null)))),
                        List.of());

        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                SchemaResolver.resolve(
                                        schema, null, tempDir.resolve("dup.myra.yml")));
        assertTrue(ex.getMessage().contains("reuses tag"));
    }

    @Test
    void resolve_ShouldRejectNonSequentialTags() {
        SchemaDefinition schema =
                new SchemaDefinition(
                        "com.test",
                        "1.0.0",
                        List.of(
                                new MessageDefinition(
                                        "TestMessage",
                                        List.of(
                                                new FieldDefinition(
                                                        1, "field1", "int32", false, false, false,
                                                        "", null),
                                                new FieldDefinition(
                                                        3, "field2", "int32", false, false, false,
                                                        "", null)))),
                        List.of());

        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                SchemaResolver.resolve(
                                        schema, null, tempDir.resolve("gap.myra.yml")));
        assertTrue(ex.getMessage().contains("sequential field tags"));
    }

    @Test
    void resolve_WithEnums_ShouldPreserveEnumValues() {
        SchemaDefinition schema =
                new SchemaDefinition(
                        "com.test",
                        "1.0.0",
                        List.of(),
                        List.of(
                                new EnumDefinition(
                                        "Status",
                                        "int8",
                                        List.of(
                                                new EnumValueDefinition("OK", 0),
                                                new EnumValueDefinition("ERROR", 1)))));

        ResolutionResult result =
                SchemaResolver.resolve(schema, null, tempDir.resolve("test.myra.yml"));

        assertEquals(1, result.resolvedSchema().enums().size());
        ResolvedEnumDefinition enumDef = result.resolvedSchema().enums().getFirst();
        assertEquals("Status", enumDef.name());
        assertEquals(2, enumDef.values().size());
        assertEquals(0, enumDef.values().getFirst().id());
    }

        @Test
        void resolve_WithNullEnums_ShouldHandleMissingEnums() {
                SchemaDefinition schema =
                                new SchemaDefinition("com.test", "1.0.0", List.of(), null);

                // Should not throw NPE when enums() is null; should produce an empty resolved enum list
                ResolutionResult result =
                                SchemaResolver.resolve(schema, null, tempDir.resolve("test.myra.yml"));

                assertNotNull(result);
                assertNotNull(result.resolvedSchema());
                assertNotNull(result.resolvedSchema().enums());
                assertEquals(0, result.resolvedSchema().enums().size());
        }

    private SchemaDefinition createTestSchema() {
        return new SchemaDefinition(
                "com.test",
                "1.0.0",
                List.of(
                        new MessageDefinition(
                                "TestMessage",
                                List.of(
                                        new FieldDefinition(
                                                1, "field1", "int32", false, false, false, "",
                                                null),
                                        new FieldDefinition(
                                                2, "field2", "string", false, false, false, "",
                                                null))),
                        new MessageDefinition(
                                "TestResponse",
                                List.of(
                                        new FieldDefinition(
                                                1, "result", "bool", false, false, false, "",
                                                null)))),
                List.of());
    }
}
