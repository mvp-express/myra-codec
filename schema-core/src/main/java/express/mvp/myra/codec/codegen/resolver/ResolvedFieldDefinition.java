package express.mvp.myra.codec.codegen.resolver;

public record ResolvedFieldDefinition(
        String name,
        int id,
        int tag,
        String type,
        boolean optional,
        boolean repeated,
        boolean deprecated,
        String deprecationNote,
        Integer fixedCapacity) {}
