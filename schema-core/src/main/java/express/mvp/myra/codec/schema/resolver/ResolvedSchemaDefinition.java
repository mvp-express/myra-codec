package express.mvp.myra.codec.schema.resolver;

import java.util.List;

/**
 * Resolved schema definition with stable ids for messages and fields.
 *
 * @param namespace the schema namespace
 * @param version the schema version string
 * @param messages the resolved message definitions
 * @param enums the resolved enum definitions
 */
public record ResolvedSchemaDefinition(
        String namespace,
        String version,
        List<ResolvedMessageDefinition> messages,
        List<ResolvedEnumDefinition> enums) {
    public ResolvedSchemaDefinition {
        messages = messages == null ? List.of() : List.copyOf(messages);
        enums = enums == null ? List.of() : List.copyOf(enums);
    }
}
