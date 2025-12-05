package express.mvp.myra.codec.codegen.resolver;

import java.util.List;

public record ResolvedMessageDefinition(
        String name, int id, List<ResolvedFieldDefinition> fields) {}
