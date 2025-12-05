package express.mvp.myra.codec.schema;

import java.util.List;

public record EnumDefinition(String name, String type, List<EnumValueDefinition> values) {}
