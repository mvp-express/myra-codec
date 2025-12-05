package express.mvp.myra.codec.codegen.resolver;

import express.mvp.myra.codec.schema.EnumDefinition;
import express.mvp.myra.codec.schema.EnumValueDefinition;
import java.util.List;

public record ResolvedEnumDefinition(String name, String type, List<EnumValueDefinition> values) {
    public static ResolvedEnumDefinition fromRaw(EnumDefinition rawEnum) {
        return new ResolvedEnumDefinition(rawEnum.name(), rawEnum.type(), rawEnum.values());
    }
}
