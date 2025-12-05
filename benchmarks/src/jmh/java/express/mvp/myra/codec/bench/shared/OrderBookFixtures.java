package express.mvp.myra.codec.bench.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import express.mvp.myra.codec.bench.shared.model.Level;
import express.mvp.myra.codec.bench.shared.model.MetadataEntry;
import express.mvp.myra.codec.bench.shared.model.OrderBookSnapshot;
import express.mvp.myra.codec.bench.shared.model.Trade;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared fixture loader that materializes order-book snapshots from JSON resources.
 */
public final class OrderBookFixtures {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ConcurrentHashMap<String, List<OrderBookSnapshot>> CACHE = new ConcurrentHashMap<>();

    private OrderBookFixtures() {
    }

    public static List<OrderBookSnapshot> load(String dataset) {
        return CACHE.computeIfAbsent(dataset, OrderBookFixtures::loadSnapshotsUnchecked);
    }

    private static List<OrderBookSnapshot> loadSnapshotsUnchecked(String dataset) {
        try (InputStream stream = openDataset(dataset)) {
            if (stream == null) {
                throw new IllegalArgumentException("Cannot locate dataset: " + dataset);
            }
            JsonNode root = MAPPER.readTree(stream);
            if (!root.isArray()) {
                throw new IllegalArgumentException("Expected JSON array in " + dataset);
            }
            List<OrderBookSnapshot> snapshots = new ArrayList<>(root.size());
            for (JsonNode node : root) {
                snapshots.add(toSnapshot(node));
            }
            return List.copyOf(snapshots);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load dataset " + dataset, ex);
        }
    }

    private static InputStream openDataset(String dataset) throws IOException {
        ClassLoader loader = OrderBookFixtures.class.getClassLoader();
        InputStream resourceStream = loader.getResourceAsStream(dataset);
        if (resourceStream != null) {
            return resourceStream;
        }
        URL resourceUrl = loader.getResource(dataset.startsWith("benchmarks/") ? dataset : "benchmarks/" + dataset);
        if (resourceUrl != null) {
            return resourceUrl.openStream();
        }
        Path absolute = Paths.get(dataset);
        if (Files.exists(absolute)) {
            return Files.newInputStream(absolute);
        }
        Path workspaceRelative = Paths.get(System.getProperty("user.dir", ".")).resolve(dataset).normalize();
        if (Files.exists(workspaceRelative)) {
            return Files.newInputStream(workspaceRelative);
        }
        return null;
    }

    private static OrderBookSnapshot toSnapshot(JsonNode node) {
        return new OrderBookSnapshot(
                requiredText(node, "timestamp"),
                requiredText(node, "venue"),
                requiredText(node, "symbol"),
                node.path("instrumentId").asInt(),
                node.path("sequence").asLong(),
                node.path("isTrading").asBoolean(),
                optionalText(node, "tradingStatus"),
                toTrade(node.path("lastTrade")),
                toLevels(node, "bids"),
                toLevels(node, "asks"),
                toMetadata(node.path("metadata"))
        );
    }

    private static List<Level> toLevels(JsonNode parent, String fieldName) {
        JsonNode levelsNode = parent.path(fieldName);
        if (!levelsNode.isArray() || levelsNode.size() == 0) {
            return List.of();
        }
        List<Level> levels = new ArrayList<>(levelsNode.size());
        for (JsonNode levelNode : levelsNode) {
            levels.add(new Level(
                    levelNode.path("priceNanos").asLong(),
                    levelNode.path("size").asInt(),
                    levelNode.path("orderCount").asInt(),
                    levelNode.hasNonNull("maker") ? levelNode.get("maker").asBoolean() : null
            ));
        }
        return levels;
    }

    private static Trade toTrade(JsonNode tradeNode) {
        if (tradeNode == null || tradeNode.isMissingNode() || tradeNode.isNull()) {
            return null;
        }
        return new Trade(
                tradeNode.path("priceNanos").asLong(),
                tradeNode.path("size").asInt(),
                optionalText(tradeNode, "aggressor")
        );
    }

    private static List<MetadataEntry> toMetadata(JsonNode metadataNode) {
        if (metadataNode == null || metadataNode.isMissingNode() || metadataNode.isNull()) {
            return List.of();
        }
        List<MetadataEntry> entries = new ArrayList<>();
        if (metadataNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = metadataNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                entries.add(new MetadataEntry(entry.getKey(), entry.getValue().asText("")));
            }
        } else if (metadataNode.isArray()) {
            for (JsonNode entryNode : metadataNode) {
                String key = entryNode.path("key").asText("");
                String value = entryNode.path("value").asText("");
                if (!key.isEmpty() || !value.isEmpty()) {
                    entries.add(new MetadataEntry(key, value));
                }
            }
        }
        return entries;
    }

    private static String requiredText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("Missing required field '" + fieldName + "'");
        }
        return value.asText();
    }

    private static String optionalText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }
}
