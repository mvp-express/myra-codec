package express.mvp.myra.codec.schema;

import java.util.List;

public record MessageDefinition(String name, List<FieldDefinition> fields) {}
