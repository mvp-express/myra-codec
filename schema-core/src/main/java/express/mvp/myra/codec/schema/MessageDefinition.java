package express.mvp.myra.codec.schema;

import java.util.List;

/**
 * Declares a message type and its fields.
 *
 * @param name the message name
 * @param fields the ordered field definitions
 */
public record MessageDefinition(String name, List<FieldDefinition> fields) {
    public MessageDefinition {
        fields = fields == null ? List.of() : List.copyOf(fields);
    }
}
