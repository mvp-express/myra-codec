package express.mvp.myra.codec.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import express.mvp.myra.codec.schema.SchemaDefinition;
import java.io.IOException;
import java.nio.file.Path;

public class SchemaParser {

    public SchemaDefinition parse(Path schemaPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(schemaPath.toFile(), SchemaDefinition.class);
    }
}
