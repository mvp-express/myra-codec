package express.mvp.myra.codec.bench.codecs.flatbuffers;

import com.google.flatbuffers.FlatBufferBuilder;
import express.mvp.myra.codec.bench.shared.OrderBookFixtures;
import express.mvp.myra.codec.bench.shared.model.Level;
import express.mvp.myra.codec.bench.shared.model.MetadataEntry;
import express.mvp.myra.codec.bench.shared.model.OrderBookSnapshot;
import express.mvp.myra.codec.bench.shared.model.Trade;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Encode/decode benchmarks for FlatBuffers using the shared order-book fixtures.
 */
@State(Scope.Benchmark)
public class FlatBuffersOrderBookBenchmark {

    private static final int DEFAULT_BUILDER_CAPACITY = 64 * 1024;

    @Param({
            "benchmarks/data/order_book_snapshots_sample.json",
            "benchmarks/data/order_book_snapshot.json"
    })
    public String dataset;

    private final FlatBufferBuilder builder = new FlatBufferBuilder(DEFAULT_BUILDER_CAPACITY);
    private final express.mvp.myra.codec.bench.codecs.flatbuffers.OrderBookSnapshot fbSnapshot =
            new express.mvp.myra.codec.bench.codecs.flatbuffers.OrderBookSnapshot();
    private final express.mvp.myra.codec.bench.codecs.flatbuffers.Level fbLevel =
            new express.mvp.myra.codec.bench.codecs.flatbuffers.Level();
    private final express.mvp.myra.codec.bench.codecs.flatbuffers.MetadataEntry fbMetadata =
            new express.mvp.myra.codec.bench.codecs.flatbuffers.MetadataEntry();
    private final express.mvp.myra.codec.bench.codecs.flatbuffers.Trade fbTrade =
            new express.mvp.myra.codec.bench.codecs.flatbuffers.Trade();

    private List<OrderBookSnapshot> fixtures;
    private List<byte[]> encodedPayloads;
    private int encodeCursor;
    private int decodeCursor;

    @Setup(org.openjdk.jmh.annotations.Level.Trial)
    public void setup() {
        fixtures = new ArrayList<>(OrderBookFixtures.load(dataset));
        if (fixtures.isEmpty()) {
            throw new IllegalStateException("Fixture dataset is empty: " + dataset);
        }
        encodedPayloads = preEncode(fixtures);
        encodeCursor = 0;
        decodeCursor = 0;
    }

    @Benchmark
    public void encodeFlatBuffers(Blackhole blackhole) {
        OrderBookSnapshot snapshot = nextSnapshot();
        int length = buildSnapshot(snapshot);
        blackhole.consume(length);
    }

    @Benchmark
    public void decodeFlatBuffers(Blackhole blackhole) {
        byte[] payload = nextPayload();
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        express.mvp.myra.codec.bench.codecs.flatbuffers.OrderBookSnapshot decoded =
                express.mvp.myra.codec.bench.codecs.flatbuffers.OrderBookSnapshot.getRootAsOrderBookSnapshot(buffer, fbSnapshot);
        long checksum = checksum(decoded);
        blackhole.consume(checksum);
    }

    private List<byte[]> preEncode(List<OrderBookSnapshot> snapshots) {
        List<byte[]> payloads = new ArrayList<>(snapshots.size());
        for (OrderBookSnapshot snapshot : snapshots) {
            payloads.add(encodeToArray(snapshot));
        }
        return payloads;
    }

    private int buildSnapshot(OrderBookSnapshot snapshot) {
        builder.clear();
        int timestampOffset = builder.createString(snapshot.timestamp());
        int venueOffset = builder.createString(snapshot.venue());
        int symbolOffset = builder.createString(snapshot.symbol());
        int tradingStatusOffset = snapshot.tradingStatus() == null ? 0 : builder.createString(snapshot.tradingStatus());

        Trade lastTrade = snapshot.lastTrade();
        int aggressorOffset = (lastTrade != null && lastTrade.aggressor() != null)
                ? builder.createString(lastTrade.aggressor())
                : 0;
        int lastTradeOffset = lastTrade == null
                ? 0
                : express.mvp.myra.codec.bench.codecs.flatbuffers.Trade.createTrade(
                        builder, lastTrade.priceNanos(), lastTrade.size(), aggressorOffset);

        int bidsOffset = buildLevelVector(safeLevels(snapshot.bids()), true);
        int asksOffset = buildLevelVector(safeLevels(snapshot.asks()), false);
        int metadataOffset = buildMetadataVector(safeMetadata(snapshot.metadata()));

        int snapshotOffset = express.mvp.myra.codec.bench.codecs.flatbuffers.OrderBookSnapshot.createOrderBookSnapshot(
                builder,
                timestampOffset,
                venueOffset,
                symbolOffset,
                snapshot.instrumentId(),
                snapshot.sequence(),
                snapshot.isTrading(),
                tradingStatusOffset,
                lastTradeOffset,
                bidsOffset,
                asksOffset,
                metadataOffset);
        express.mvp.myra.codec.bench.codecs.flatbuffers.OrderBookSnapshot.finishOrderBookSnapshotBuffer(builder, snapshotOffset);
        return builder.dataBuffer().remaining();
    }

    private byte[] encodeToArray(OrderBookSnapshot snapshot) {
        int length = buildSnapshot(snapshot);
        byte[] payload = new byte[length];
        builder.dataBuffer().get(payload);
        return payload;
    }

    private int buildLevelVector(List<Level> levels, boolean bids) {
        if (levels.isEmpty()) {
            return 0;
        }
        int[] offsets = new int[levels.size()];
        for (int i = levels.size() - 1; i >= 0; i--) {
            Level level = levels.get(i);
            offsets[i] = express.mvp.myra.codec.bench.codecs.flatbuffers.Level.createLevel(
                    builder,
                    level.priceNanos(),
                    level.size(),
                    level.orderCount(),
                    toFlatMaker(level.maker()));
        }
        return bids
                ? express.mvp.myra.codec.bench.codecs.flatbuffers.OrderBookSnapshot.createBidsVector(builder, offsets)
                : express.mvp.myra.codec.bench.codecs.flatbuffers.OrderBookSnapshot.createAsksVector(builder, offsets);
    }

    private int buildMetadataVector(List<MetadataEntry> entries) {
        if (entries.isEmpty()) {
            return 0;
        }
        int[] offsets = new int[entries.size()];
        for (int i = entries.size() - 1; i >= 0; i--) {
            MetadataEntry entry = entries.get(i);
            int keyOffset = builder.createString(entry.key() == null ? "" : entry.key());
            int valueOffset = builder.createString(entry.value() == null ? "" : entry.value());
            offsets[i] = express.mvp.myra.codec.bench.codecs.flatbuffers.MetadataEntry.createMetadataEntry(
                    builder, keyOffset, valueOffset);
        }
        return express.mvp.myra.codec.bench.codecs.flatbuffers.OrderBookSnapshot.createMetadataVector(builder, offsets);
    }

    private OrderBookSnapshot nextSnapshot() {
        OrderBookSnapshot snapshot = fixtures.get(encodeCursor);
        encodeCursor = (encodeCursor + 1) % fixtures.size();
        return snapshot;
    }

    private byte[] nextPayload() {
        byte[] payload = encodedPayloads.get(decodeCursor);
        decodeCursor = (decodeCursor + 1) % encodedPayloads.size();
        return payload;
    }

    private long checksum(express.mvp.myra.codec.bench.codecs.flatbuffers.OrderBookSnapshot snapshot) {
        long checksum = 0L;
        checksum += snapshot.instrumentId();
        checksum += snapshot.sequence();
        checksum += snapshot.isTrading() ? 1 : 0;
        checksum += length(snapshot.timestamp());
        checksum += length(snapshot.venue());
        checksum += length(snapshot.symbol());
        checksum += length(snapshot.tradingStatus());

        express.mvp.myra.codec.bench.codecs.flatbuffers.Trade trade = snapshot.lastTrade(fbTrade);
        if (trade != null) {
            checksum += trade.priceNanos();
            checksum += trade.size();
            checksum += length(trade.aggressor());
        }

        for (int i = 0; i < snapshot.bidsLength(); i++) {
            snapshot.bids(fbLevel, i);
            checksum += fbLevel.priceNanos();
            checksum += fbLevel.size();
            checksum += fbLevel.orderCount();
            checksum += makerChecksum(fbLevel.maker());
        }

        for (int i = 0; i < snapshot.asksLength(); i++) {
            snapshot.asks(fbLevel, i);
            checksum += fbLevel.priceNanos();
            checksum += fbLevel.size();
            checksum += fbLevel.orderCount();
            checksum += makerChecksum(fbLevel.maker());
        }

        for (int i = 0; i < snapshot.metadataLength(); i++) {
            snapshot.metadata(fbMetadata, i);
            checksum += length(fbMetadata.key());
            checksum += length(fbMetadata.value());
        }

        return checksum;
    }

    private int length(String value) {
        return value == null ? 0 : value.length();
    }

    private byte toFlatMaker(Boolean maker) {
        if (maker == null) {
            return (byte) -1;
        }
        return maker ? (byte) 1 : (byte) 0;
    }

    private int makerChecksum(byte value) {
        if (value == 1) {
            return 2;
        }
        if (value == 0) {
            return 3;
        }
        return 1;
    }

    private List<Level> safeLevels(List<Level> levels) {
        return levels == null ? List.of() : levels;
    }

    private List<MetadataEntry> safeMetadata(List<MetadataEntry> entries) {
        return entries == null ? List.of() : entries;
    }
}
