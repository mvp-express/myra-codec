package express.mvp.myra.codec.codegen.resolver;

import java.util.List;

public record ResolvedSchemaDefinition(
        String namespace,
        String version,
        List<ResolvedMessageDefinition> messages,
        List<ResolvedEnumDefinition> enums) {}
