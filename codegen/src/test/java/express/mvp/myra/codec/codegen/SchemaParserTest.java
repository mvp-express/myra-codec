package express.mvp.myra.codec.codegen;

import static org.junit.jupiter.api.Assertions.*;

import express.mvp.myra.codec.schema.FieldDefinition;
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
        // 1. Arrange: Get the path to our test YAML file
        URL resource = getClass().getClassLoader().getResource("kvstore.myra.yml");
        assertNotNull(resource, "Test schema file 'kvstore.myra.yml' not found in resources.");
        Path schemaPath = Paths.get(resource.toURI());

        SchemaParser parser = new SchemaParser();

        // 2. Act: Parse the file
        SchemaDefinition schema = parser.parse(schemaPath);

        // 3. Assert: Verify the contents of the parsed object
        assertNotNull(schema);
        assertEquals("com.example.kvstore.codec", schema.namespace());
        assertEquals("1.0.0", schema.version());

        // Check a specific message
        assertEquals(6, schema.messages().size());
        Optional<MessageDefinition> putRequestOpt =
                schema.messages().stream().filter(m -> m.name().equals("PutRequest")).findFirst();
        assertTrue(putRequestOpt.isPresent());
        assertEquals(2, putRequestOpt.get().fields().size());
        assertEquals("key", putRequestOpt.get().fields().getFirst().name());
        assertEquals("string", putRequestOpt.get().fields().getFirst().type());

        // Check enum definition
        assertEquals(1, schema.enums().size());
        assertEquals("StatusCode", schema.enums().getFirst().name());
        assertEquals(2, schema.enums().getFirst().values().size());
        assertEquals("OK", schema.enums().getFirst().values().getFirst().name());
        assertEquals(0, schema.enums().getFirst().values().getFirst().id());
    }

    @Test
    void parse_OrderBookSchema_ShouldHandleRepeatedCollections() throws Exception {
        URL resource = getClass().getClassLoader().getResource("order_book.myra.yml");
        assertNotNull(resource, "Test schema file 'order_book.myra.yml' not found in resources.");
        Path schemaPath = Paths.get(resource.toURI());

        SchemaParser parser = new SchemaParser();
        SchemaDefinition schema = parser.parse(schemaPath);

        assertEquals("express.mvp.myra.bench", schema.namespace());
        assertEquals("1.0.0", schema.version());
        assertEquals(
                4,
                schema.messages().size(),
                "Schema should expose Trade, Level, MetadataEntry, OrderBookSnapshot");

        MessageDefinition snapshot =
                schema.messages().stream()
                        .filter(m -> m.name().equals("OrderBookSnapshot"))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("OrderBookSnapshot message missing"));
        assertEquals(11, snapshot.fields().size());

        FieldDefinition bids = field(snapshot, "bids");
        assertTrue(bids.repeated(), "Bids should be a repeated field");
        assertEquals("Level", bids.type());

        FieldDefinition asks = field(snapshot, "asks");
        assertTrue(asks.repeated(), "Asks should be a repeated field");
        assertEquals("Level", asks.type());

        FieldDefinition metadata = field(snapshot, "metadata");
        assertTrue(metadata.repeated(), "Metadata entries should repeat");
        assertEquals("MetadataEntry", metadata.type());
    }

    private static FieldDefinition field(MessageDefinition message, String fieldName) {
        return message.fields().stream()
                .filter(f -> f.name().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Field missing: " + fieldName));
    }
}
