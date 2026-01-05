package express.mvp.myra.codec.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Parses .myra.yml schema files into {@link SchemaDefinition} instances. */
public class SchemaParser {

    /**
     * Parse the schema file at the given path.
     *
     * @param schemaPath the path to the .myra.yml file
     * @return the parsed schema definition
     * @throws IOException if the file cannot be read or parsed
     */
    public SchemaDefinition parse(Path schemaPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        SchemaDefinition parsed = mapper.readValue(schemaPath.toFile(), SchemaDefinition.class);

        List<MessageDefinition> parsedMessages = parsed.messages();
        List<EnumDefinition> parsedEnums = parsed.enums();
        List<MessageDefinition> messages = (parsedMessages == null) ? List.of() : parsedMessages;
        List<EnumDefinition> enums = (parsedEnums == null) ? List.of() : parsedEnums;

        return new SchemaDefinition(parsed.namespace(), parsed.version(), messages, enums);
    }
}
