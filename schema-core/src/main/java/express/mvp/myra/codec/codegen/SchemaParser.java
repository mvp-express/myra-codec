package express.mvp.myra.codec.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import express.mvp.myra.codec.schema.SchemaDefinition;
import java.io.IOException;
import java.nio.file.Path;

public class SchemaParser {

    public SchemaDefinition parse(Path schemaPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        SchemaDefinition parsed = mapper.readValue(schemaPath.toFile(), SchemaDefinition.class);

        // Defensive normalization: ensure lists are non-null so downstream code doesn't
        // need to defensively check for null. Convert missing collections to empty lists.
        java.util.List<express.mvp.myra.codec.schema.MessageDefinition> parsedMessages =
            parsed.messages();
        java.util.List<express.mvp.myra.codec.schema.EnumDefinition> parsedEnums = parsed.enums();

        java.util.List<express.mvp.myra.codec.schema.MessageDefinition> messages =
            (parsedMessages == null) ? java.util.List.of() : parsedMessages;
        java.util.List<express.mvp.myra.codec.schema.EnumDefinition> enums =
            (parsedEnums == null) ? java.util.List.of() : parsedEnums;

        return new SchemaDefinition(parsed.namespace(), parsed.version(), messages, enums);
    }
}
