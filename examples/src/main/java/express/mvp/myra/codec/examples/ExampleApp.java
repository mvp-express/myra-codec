package express.mvp.myra.codec.examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import express.mvp.myra.codec.examples.generated.orderbook.LevelArrayWriter;
import express.mvp.myra.codec.examples.generated.orderbook.LevelFlyweight;
import express.mvp.myra.codec.examples.generated.orderbook.MetadataEntryArrayWriter;
import express.mvp.myra.codec.examples.generated.orderbook.MetadataEntryFlyweight;
import express.mvp.myra.codec.examples.generated.orderbook.OrderBookSnapshotBuilder;
import express.mvp.myra.codec.examples.generated.orderbook.OrderBookSnapshotFlyweight;
import express.mvp.myra.codec.examples.generated.orderbook.TradeFlyweight;
import express.mvp.myra.codec.examples.generated.orderbook.TradeWriterImpl;
import express.mvp.myra.codec.examples.generated.orderbook.TradingStatus;
import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.roray.ffm.utils.memory.MemorySegmentPool;
import express.mvp.roray.ffm.utils.memory.PooledSegment;
import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Demonstrates encoding and decoding generated order book messages. */
public class ExampleApp {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SNAPSHOT_RESOURCE = "data/order_book_snapshot.json";

    /** Runs a small end-to-end encode/decode demo. */
    public static void main(String[] args) {
        MemorySegmentPool pool = new MemorySegmentPool(4096, 1, 16);
        MessageEncoder encoder = new MessageEncoder(pool);

        MemorySegment scratch = MemorySegment.ofArray(new byte[256]);

        // Reusable writers (no lambdas, no per-call allocation)
        TradeWriterImpl tradeWriter = new TradeWriterImpl();
        LevelArrayWriter bidsWriter = new LevelArrayWriter();
        LevelArrayWriter asksWriter = new LevelArrayWriter();
        MetadataEntryArrayWriter metaWriter = new MetadataEntryArrayWriter();

        // Example 1: full snapshot (all optionals present, 2 bids, 2 asks, 1 metadata)
        tradeWriter.priceNanos = 123_450_000L;
        tradeWriter.size = 100;
        tradeWriter.aggressor = "BUY";
        tradeWriter.aggressorScratch = scratch;

        bidsWriter.count = 2;
        bidsWriter.priceNanos = new long[] {123_440_000L, 123_430_000L};
        bidsWriter.size = new int[] {10, 5};
        bidsWriter.orderCount = new int[] {2, 1};
        bidsWriter.hasMaker = new boolean[] {true, true};
        bidsWriter.maker = new boolean[] {true, false};

        asksWriter.count = 2;
        asksWriter.priceNanos = new long[] {123_460_000L, 123_470_000L};
        asksWriter.size = new int[] {12, 6};
        asksWriter.orderCount = new int[] {1, 1};
        asksWriter.hasMaker = new boolean[] {true, true};
        asksWriter.maker = new boolean[] {false, false};

        metaWriter.count = 1;
        metaWriter.key = new String[] {"source"};
        metaWriter.value = new String[] {"demo"};
        metaWriter.keyScratch = scratch;
        metaWriter.valueScratch = scratch;

        try (PooledSegment pooled =
                OrderBookSnapshotBuilder.allocate(encoder, 2048)
                        .setTimestamp("2024-01-01T12:00:00Z", scratch)
                        .setVenue("XNAS", scratch)
                        .setSymbol("AAPL", scratch)
                        .setInstrumentId(42)
                        .setSequence(1001L)
                        .setIsTrading(true)
                        .setTradingStatus(TradingStatus.OPEN) // enum overload
                        .setLastTrade(tradeWriter) // writer overload
                        .setBids(bidsWriter.count, bidsWriter)
                        .setAsks(asksWriter.count, asksWriter)
                        .setMetadata(metaWriter.count, metaWriter)
                        .build(
                                (short) OrderBookSnapshotFlyweight.TEMPLATE_ID,
                                OrderBookSnapshotFlyweight.SCHEMA_VERSION)) {

            decodeAndPrint("FULL", pooled.segment());
        }

        // Example 2: minimal snapshot (optionals absent, empty repeats)
        try (PooledSegment pooled =
                OrderBookSnapshotBuilder.allocate(encoder, 2048)
                        .setTimestamp("2024-01-01T12:00:01Z", scratch)
                        .setVenue("XNAS", scratch)
                        .setSymbol("AAPL", scratch)
                        .setInstrumentId(42)
                        .setSequence(1002L)
                        .setIsTrading(false)
                        .setBids(0, bidsWriter) // zero count
                        .setAsks(0, asksWriter) // zero count
                        .setMetadata(0, metaWriter) // zero count
                        .build(
                                (short) OrderBookSnapshotFlyweight.TEMPLATE_ID,
                                OrderBookSnapshotFlyweight.SCHEMA_VERSION)) {

            decodeAndPrint("MIN", pooled.segment());
        }

        encodeDecodeFromJsonResource(encoder, scratch, SNAPSHOT_RESOURCE, 3);
    }

    private static void decodeAndPrint(String label, MemorySegment segment) {
        OrderBookSnapshotFlyweight fw = new OrderBookSnapshotFlyweight();
        fw.wrap(segment, MessageHeader.HEADER_SIZE);
        fw.validate();

        System.out.println("[" + label + "]");
        System.out.println("symbol=" + fw.getSymbol().toString());
        System.out.println("seq=" + fw.getSequence());
        System.out.println("isTrading=" + fw.getIsTrading());

        if (fw.hasTradingStatus()) {
            TradingStatus status = TradingStatus.fromId(Byte.toUnsignedInt(fw.getTradingStatus()));
            System.out.println("tradingStatus=" + status);
        } else {
            System.out.println("tradingStatus=ABSENT");
        }

        if (fw.hasLastTrade()) {
            TradeFlyweight trade = fw.getLastTrade();
            System.out.println("lastTradePrice=" + trade.getPriceNanos());
        } else {
            System.out.println("lastTrade=ABSENT");
        }

        System.out.println("bidsCount=" + fw.getBidsCount());
        for (int i = 0; i < fw.getBidsCount(); i++) {
            LevelFlyweight lvl = fw.getBidsAt(i);
            System.out.println("bid " + i + " " + lvl.getPriceNanos() + " x " + lvl.getSize());
        }

        System.out.println("asksCount=" + fw.getAsksCount());
        for (int i = 0; i < fw.getAsksCount(); i++) {
            LevelFlyweight lvl = fw.getAsksAt(i);
            System.out.println("ask " + i + " " + lvl.getPriceNanos() + " x " + lvl.getSize());
        }

        System.out.println("metaCount=" + fw.getMetadataCount());
        for (int i = 0; i < fw.getMetadataCount(); i++) {
            MetadataEntryFlyweight entry = fw.getMetadataAt(i);
            System.out.println(entry.getKey().toString() + "=" + entry.getValue().toString());
        }
    }

    private static void encodeDecodeFromJsonResource(
            MessageEncoder encoder, MemorySegment scratch, String resourcePath, int maxSnapshots) {
        try (InputStream stream =
                ExampleApp.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing resource: " + resourcePath);
            }
            JsonNode root = MAPPER.readTree(stream);
            if (!root.isArray()) {
                throw new IllegalArgumentException("Expected JSON array in " + resourcePath);
            }
            int limit = Math.min(maxSnapshots, root.size());
            System.out.println("Loading " + limit + " snapshots from " + resourcePath);
            int index = 0;
            for (JsonNode node : root) {
                if (index >= limit) {
                    break;
                }
                encodeDecodeSnapshot(node, encoder, scratch, index);
                index++;
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load " + resourcePath, ex);
        }
    }

    private static void encodeDecodeSnapshot(
            JsonNode node, MessageEncoder encoder, MemorySegment scratch, int index) {
        String timestamp = requiredText(node, "timestamp");
        String venue = requiredText(node, "venue");
        String symbol = requiredText(node, "symbol");
        int instrumentId = node.path("instrumentId").asInt();
        long sequence = node.path("sequence").asLong();
        boolean isTrading = node.path("isTrading").asBoolean();

        TradingStatus tradingStatus = parseTradingStatus(optionalText(node, "tradingStatus"));
        TradeWriterImpl tradeWriter = parseTrade(node.path("lastTrade"), scratch);
        LevelArrayWriter bidsWriter = parseLevels(node.path("bids"));
        LevelArrayWriter asksWriter = parseLevels(node.path("asks"));
        MetadataEntryArrayWriter metaWriter = parseMetadata(node.path("metadata"), scratch);

        OrderBookSnapshotBuilder builder =
                OrderBookSnapshotBuilder.allocate(encoder, 2048)
                        .setTimestamp(timestamp, scratch)
                        .setVenue(venue, scratch)
                        .setSymbol(symbol, scratch)
                        .setInstrumentId(instrumentId)
                        .setSequence(sequence)
                        .setIsTrading(isTrading);
        if (tradingStatus != null) {
            builder.setTradingStatus(tradingStatus);
        }
        if (tradeWriter != null) {
            builder.setLastTrade(tradeWriter);
        }
        try (PooledSegment pooled =
                builder.setBids(bidsWriter.count, bidsWriter)
                        .setAsks(asksWriter.count, asksWriter)
                        .setMetadata(metaWriter.count, metaWriter)
                        .build(
                                (short) OrderBookSnapshotFlyweight.TEMPLATE_ID,
                                OrderBookSnapshotFlyweight.SCHEMA_VERSION)) {
            decodeAndPrint("JSON-" + index + "-" + symbol, pooled.segment());
        }
    }

    private static TradingStatus parseTradingStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return TradingStatus.valueOf(value.toUpperCase(Locale.ROOT));
    }

    private static TradeWriterImpl parseTrade(JsonNode tradeNode, MemorySegment scratch) {
        if (tradeNode == null || tradeNode.isMissingNode() || tradeNode.isNull()) {
            return null;
        }
        TradeWriterImpl writer = new TradeWriterImpl();
        writer.priceNanos = tradeNode.path("priceNanos").asLong();
        writer.size = tradeNode.path("size").asInt();
        writer.aggressor = optionalText(tradeNode, "aggressor");
        writer.aggressorScratch = scratch;
        return writer;
    }

    private static LevelArrayWriter parseLevels(JsonNode levelsNode) {
        LevelArrayWriter writer = new LevelArrayWriter();
        if (levelsNode == null || !levelsNode.isArray() || levelsNode.size() == 0) {
            return writer;
        }
        int count = levelsNode.size();
        writer.count = count;
        writer.priceNanos = new long[count];
        writer.size = new int[count];
        writer.orderCount = new int[count];
        writer.hasMaker = new boolean[count];
        writer.maker = new boolean[count];
        for (int i = 0; i < count; i++) {
            JsonNode levelNode = levelsNode.get(i);
            writer.priceNanos[i] = levelNode.path("priceNanos").asLong();
            writer.size[i] = levelNode.path("size").asInt();
            writer.orderCount[i] = levelNode.path("orderCount").asInt();
            if (levelNode.hasNonNull("maker")) {
                writer.hasMaker[i] = true;
                writer.maker[i] = levelNode.get("maker").asBoolean();
            }
        }
        return writer;
    }

    private static MetadataEntryArrayWriter parseMetadata(
            JsonNode metadataNode, MemorySegment scratch) {
        MetadataEntryArrayWriter writer = new MetadataEntryArrayWriter();
        if (metadataNode == null || metadataNode.isMissingNode() || metadataNode.isNull()) {
            return writer;
        }
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        if (metadataNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = metadataNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                keys.add(entry.getKey());
                values.add(entry.getValue().asText(""));
            }
        } else if (metadataNode.isArray()) {
            for (JsonNode entryNode : metadataNode) {
                String key = entryNode.path("key").asText("");
                String value = entryNode.path("value").asText("");
                if (!key.isEmpty() || !value.isEmpty()) {
                    keys.add(key);
                    values.add(value);
                }
            }
        }
        int count = keys.size();
        writer.count = count;
        if (count > 0) {
            writer.key = keys.toArray(new String[0]);
            writer.value = values.toArray(new String[0]);
            writer.keyScratch = scratch;
            writer.valueScratch = scratch;
        }
        return writer;
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
