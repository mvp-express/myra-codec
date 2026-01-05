package express.mvp.myra.codec.schema;

import java.util.List;

/**
 * Root schema definition for a .myra.yml file.
 *
 * @param namespace the target namespace for generated code
 * @param version the semantic version string for the schema
 * @param messages the message definitions in the schema
 * @param enums the enum definitions in the schema
 */
public record SchemaDefinition(
        String namespace,
        String version,
        List<MessageDefinition> messages,
        List<EnumDefinition> enums) {
    public SchemaDefinition {
        messages = messages == null ? List.of() : List.copyOf(messages);
        enums = enums == null ? List.of() : List.copyOf(enums);
    }
}
