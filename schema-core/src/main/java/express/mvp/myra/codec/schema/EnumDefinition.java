package express.mvp.myra.codec.schema;

import java.util.List;

/**
 * Declares an enum type in a schema.
 *
 * @param name the enum type name
 * @param type the underlying wire type (for example, int32 or int8)
 * @param values the ordered enum values
 */
public record EnumDefinition(String name, String type, List<EnumValueDefinition> values) {
    public EnumDefinition {
        values = values == null ? List.of() : List.copyOf(values);
    }
}
