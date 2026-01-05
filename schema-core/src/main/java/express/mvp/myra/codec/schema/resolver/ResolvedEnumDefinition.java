package express.mvp.myra.codec.schema.resolver;

import express.mvp.myra.codec.schema.EnumDefinition;
import express.mvp.myra.codec.schema.EnumValueDefinition;
import java.util.List;

/**
 * Resolved enum definition with stable ids preserved.
 *
 * @param name the enum name
 * @param type the underlying wire type
 * @param values the resolved enum values
 */
public record ResolvedEnumDefinition(String name, String type, List<EnumValueDefinition> values) {
    public ResolvedEnumDefinition {
        values = values == null ? List.of() : List.copyOf(values);
    }

    /**
     * Build a resolved enum definition from the raw schema enum.
     *
     * @param rawEnum the raw enum definition
     * @return the resolved enum definition
     */
    public static ResolvedEnumDefinition fromRaw(EnumDefinition rawEnum) {
        return new ResolvedEnumDefinition(rawEnum.name(), rawEnum.type(), rawEnum.values());
    }
}
