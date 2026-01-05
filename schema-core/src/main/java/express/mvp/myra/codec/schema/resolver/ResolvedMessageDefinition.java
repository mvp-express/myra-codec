package express.mvp.myra.codec.schema.resolver;

import java.util.List;

/**
 * Resolved message definition with stable ids.
 *
 * @param name the message name
 * @param id the stable message id
 * @param fields the resolved field definitions
 */
public record ResolvedMessageDefinition(String name, int id, List<ResolvedFieldDefinition> fields) {
    public ResolvedMessageDefinition {
        fields = fields == null ? List.of() : List.copyOf(fields);
    }
}
