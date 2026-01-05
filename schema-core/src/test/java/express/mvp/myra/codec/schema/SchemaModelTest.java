package express.mvp.myra.codec.schema;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for schema model record validations and defensive copies. */
class SchemaModelTest {

    @Test
    void enumDefinition_ShouldCopyValuesAndDefaultToEmpty() {
        List<EnumValueDefinition> values = new ArrayList<>();
        values.add(new EnumValueDefinition("OK", 0));

        EnumDefinition def = new EnumDefinition("Status", "int8", values);
        values.add(new EnumValueDefinition("WARN", 1));

        assertEquals(1, def.values().size());
        assertThrows(UnsupportedOperationException.class, () -> def.values().add(values.get(1)));

        EnumDefinition empty = new EnumDefinition("Empty", "int8", null);
        assertNotNull(empty.values());
        assertTrue(empty.values().isEmpty());
    }

    @Test
    void messageDefinition_ShouldCopyFieldsAndDefaultToEmpty() {
        List<FieldDefinition> fields =
                new ArrayList<>(
                        List.of(
                                new FieldDefinition(
                                        1, "field1", "int32", false, false, false, "note", null)));
        MessageDefinition message = new MessageDefinition("TestMessage", fields);
        fields.add(new FieldDefinition(2, "field2", "int32", false, false, false, "note", null));

        assertEquals(1, message.fields().size());
        assertThrows(
                UnsupportedOperationException.class, () -> message.fields().add(fields.get(1)));

        MessageDefinition empty = new MessageDefinition("EmptyMessage", null);
        assertNotNull(empty.fields());
        assertTrue(empty.fields().isEmpty());
    }

    @Test
    void schemaDefinition_ShouldCopyListsAndDefaultToEmpty() {
        List<MessageDefinition> messages =
                new ArrayList<>(
                        List.of(
                                new MessageDefinition(
                                        "Msg",
                                        List.of(
                                                new FieldDefinition(
                                                        1, "field", "int32", false, false, false,
                                                        "note", null)))));
        List<EnumDefinition> enums =
                new ArrayList<>(List.of(new EnumDefinition("Status", "int8", List.of())));

        SchemaDefinition schema = new SchemaDefinition("com.test", "1.0.0", messages, enums);
        messages.clear();
        enums.clear();

        assertEquals(1, schema.messages().size());
        assertEquals(1, schema.enums().size());
        assertThrows(UnsupportedOperationException.class, schema.messages()::clear);
        assertThrows(UnsupportedOperationException.class, schema.enums()::clear);

        SchemaDefinition empty = new SchemaDefinition("com.test", "1.0.0", null, null);
        assertNotNull(empty.messages());
        assertNotNull(empty.enums());
        assertTrue(empty.messages().isEmpty());
        assertTrue(empty.enums().isEmpty());
    }

    @Test
    void fieldDefinition_ShouldValidateAndDefaultDeprecationNote() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FieldDefinition(0, "bad", "int32", false, false, false, "note", null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new FieldDefinition(1, "bad", "string", false, false, false, "note", -1));

        FieldDefinition field =
                new FieldDefinition(1, "field", "int32", false, false, false, null, null);
        assertEquals("Deprecated field", field.deprecationNote());
    }
}
