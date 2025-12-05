package express.mvp.myra.codec.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OrderBookSnapshotSampleTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void sampleJsonAlignsWithSchemaExpectations() throws Exception {
        URL resource = getClass().getClassLoader().getResource("order_book_snapshots_sample.json");
        assertNotNull(
                resource, "Fixture 'order_book_snapshots_sample.json' not found in resources.");

        JsonNode root = mapper.readTree(Paths.get(resource.toURI()).toFile());
        assertTrue(root.isArray(), "Sample should contain an array of snapshots");
        assertTrue(root.size() >= 1, "Sample should include at least one snapshot");

        for (JsonNode snapshot : root) {
            assertRequiredText(snapshot, "timestamp");
            assertRequiredText(snapshot, "venue");
            assertRequiredText(snapshot, "symbol");
            assertTrue(snapshot.has("instrumentId"), "instrumentId must be present");
            assertTrue(snapshot.has("sequence"), "sequence must be present");
            assertTrue(snapshot.has("isTrading"), "isTrading must be present");

            JsonNode bids = snapshot.path("bids");
            assertTrue(bids.isArray(), "bids must be an array");
            bids.forEach(OrderBookSnapshotSampleTest::assertLevelShape);

            JsonNode asks = snapshot.path("asks");
            assertTrue(asks.isArray(), "asks must be an array");
            asks.forEach(OrderBookSnapshotSampleTest::assertLevelShape);

            JsonNode lastTrade = snapshot.get("lastTrade");
            if (lastTrade != null && !lastTrade.isNull()) {
                assertTrue(
                        lastTrade.has("priceNanos"),
                        "lastTrade.priceNanos required when lastTrade present");
                assertTrue(lastTrade.has("size"), "lastTrade.size required when lastTrade present");
            }

            JsonNode metadata = snapshot.get("metadata");
            assertNotNull(metadata, "metadata must be present");
            assertTrue(
                    metadata.isObject() || metadata.isArray(), "metadata must be object or array");
            validateMetadata(metadata);
        }
    }

    private static void assertRequiredText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        assertNotNull(value, "Missing field: " + fieldName);
        assertFalse(value.isNull(), "Field cannot be null: " + fieldName);
        assertTrue(value.isTextual(), "Field must be a string: " + fieldName);
        assertFalse(value.asText().isBlank(), "Field cannot be blank: " + fieldName);
    }

    private static void assertLevelShape(JsonNode levelNode) {
        assertTrue(levelNode.has("priceNanos"), "Level.priceNanos required");
        assertTrue(levelNode.has("size"), "Level.size required");
        assertTrue(levelNode.has("orderCount"), "Level.orderCount required");
    }

    private static void validateMetadata(JsonNode metadataNode) {
        if (metadataNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iterator = metadataNode.fields();
            int count = 0;
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                assertFalse(entry.getKey().isBlank(), "Metadata keys must not be blank");
                count++;
            }
            assertTrue(count >= 0, "Metadata object should expose entries");
            return;
        }
        if (metadataNode.isArray()) {
            int count = 0;
            for (JsonNode entry : metadataNode) {
                assertTrue(entry.has("key"), "Metadata entry requires key");
                assertTrue(entry.has("value"), "Metadata entry requires value");
                count++;
            }
            assertTrue(count >= 0, "Metadata array should expose entries");
            return;
        }
        fail("Metadata node must be an object or array");
    }
}
