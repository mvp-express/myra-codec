package express.mvp.myra.codec.codegen;

import static org.junit.jupiter.api.Assertions.*;

import express.mvp.myra.codec.schema.MessageDefinition;
import express.mvp.myra.codec.schema.SchemaDefinition;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SchemaParserTest {

    @Test
    void parse_WithValidSchemaFile_ShouldReturnCorrectSchemaDefinition() throws Exception {
        URL resource = getClass().getClassLoader().getResource("kvstore.myra.yml");
        assertNotNull(resource, "Test schema file 'kvstore.myra.yml' not found in resources.");
        Path schemaPath = Paths.get(resource.toURI());

        SchemaParser parser = new SchemaParser();
        SchemaDefinition schema = parser.parse(schemaPath);

        assertNotNull(schema);
        assertEquals("com.example.kvstore.codec", schema.namespace());
        assertEquals("1.0.0", schema.version());

        assertEquals(6, schema.messages().size());
        Optional<MessageDefinition> putRequestOpt =
                schema.messages().stream().filter(m -> m.name().equals("PutRequest")).findFirst();
        assertTrue(putRequestOpt.isPresent());
        assertEquals(2, putRequestOpt.get().fields().size());
        assertEquals("key", putRequestOpt.get().fields().getFirst().name());
        assertEquals("string", putRequestOpt.get().fields().getFirst().type());

        assertEquals(1, schema.enums().size());
        assertEquals("StatusCode", schema.enums().getFirst().name());
        assertEquals(2, schema.enums().getFirst().values().size());
        assertEquals("OK", schema.enums().getFirst().values().getFirst().name());
        assertEquals(0, schema.enums().getFirst().values().getFirst().id());
    }

    @Test
    void parse_WithMissingEnums_ShouldReturnEmptyEnumList() throws Exception {
        URL resource = getClass().getClassLoader().getResource("no_enums.myra.yml");
        assertNotNull(resource, "Test schema file 'no_enums.myra.yml' not found in resources.");
        Path schemaPath = Paths.get(resource.toURI());

        SchemaParser parser = new SchemaParser();
        SchemaDefinition schema = parser.parse(schemaPath);

        assertNotNull(schema);
        assertEquals("express.mvp.examples.echo", schema.namespace());
        assertEquals("1.0.0", schema.version());
        assertEquals(1, schema.messages().size());
        assertNotNull(schema.enums());
        assertEquals(0, schema.enums().size());
    }
}
