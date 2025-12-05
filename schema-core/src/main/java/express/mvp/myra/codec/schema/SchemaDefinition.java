package express.mvp.myra.codec.schema;

import java.util.List;

public record SchemaDefinition(
        String namespace,
        String version,
        List<MessageDefinition> messages,
        List<EnumDefinition> enums) {}
