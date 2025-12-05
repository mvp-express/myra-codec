package express.mvp.myra.codec.bench.codecs.sbe;

import express.mvp.myra.codec.bench.codecs.sbe.generated.BooleanType;
import express.mvp.myra.codec.bench.codecs.sbe.generated.MessageHeaderDecoder;
import express.mvp.myra.codec.bench.codecs.sbe.generated.MessageHeaderEncoder;
import express.mvp.myra.codec.bench.codecs.sbe.generated.NullableBooleanType;
import express.mvp.myra.codec.bench.codecs.sbe.generated.OrderBookSnapshotDecoder;
import express.mvp.myra.codec.bench.codecs.sbe.generated.OrderBookSnapshotEncoder;
import express.mvp.myra.codec.bench.shared.OrderBookFixtures;
import express.mvp.myra.codec.bench.shared.model.Level;
import express.mvp.myra.codec.bench.shared.model.MetadataEntry;
import express.mvp.myra.codec.bench.shared.model.OrderBookSnapshot;
import express.mvp.myra.codec.bench.shared.model.Trade;
import java.util.ArrayList;
import java.util.List;
import org.agrona.concurrent.UnsafeBuffer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Encode/decode benchmarks for SBE using the shared order-book fixtures.
 */
@State(Scope.Benchmark)
public class SbeOrderBookBenchmark {

    private static final int MAX_MESSAGE_BYTES = 128 * 1024;

    @Param({
            "benchmarks/data/order_book_snapshots_sample.json",
            "benchmarks/data/order_book_snapshot.json"
    })
    public String dataset;

    private final UnsafeBuffer encodeBuffer = new UnsafeBuffer(new byte[MAX_MESSAGE_BYTES]);
    private final UnsafeBuffer decodeBuffer = new UnsafeBuffer(new byte[0]);
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final OrderBookSnapshotEncoder snapshotEncoder = new OrderBookSnapshotEncoder();
    private final OrderBookSnapshotDecoder snapshotDecoder = new OrderBookSnapshotDecoder();
    private final StringBuilder scratch = new StringBuilder(128);

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
    public void encodeSbe(Blackhole blackhole) {
        OrderBookSnapshot snapshot = nextSnapshot();
        int length = encodeIntoBuffer(snapshot);
        blackhole.consume(length);
    }

    @Benchmark
    public void decodeSbe(Blackhole blackhole) {
        byte[] payload = nextPayload();
        decodeBuffer.wrap(payload);
        long checksum = decodeChecksum(decodeBuffer);
        blackhole.consume(checksum);
    }

    private List<byte[]> preEncode(List<OrderBookSnapshot> snapshots) {
        List<byte[]> payloads = new ArrayList<>(snapshots.size());
        for (OrderBookSnapshot snapshot : snapshots) {
            int length = encodeIntoBuffer(snapshot);
            byte[] copy = new byte[length];
            encodeBuffer.getBytes(0, copy, 0, length);
            payloads.add(copy);
        }
        return payloads;
    }

    private int encodeIntoBuffer(OrderBookSnapshot snapshot) {
        snapshotEncoder.wrapAndApplyHeader(encodeBuffer, 0, headerEncoder);
        snapshotEncoder.instrumentId(snapshot.instrumentId());
        snapshotEncoder.sequence(snapshot.sequence());
        snapshotEncoder.isTrading(snapshot.isTrading() ? BooleanType.TRUE : BooleanType.FALSE);

        String tradingStatus = snapshot.tradingStatus();
        boolean hasStatus = tradingStatus != null && !tradingStatus.isEmpty();
        snapshotEncoder.hasTradingStatus(hasStatus ? BooleanType.TRUE : BooleanType.FALSE);

        Trade lastTrade = snapshot.lastTrade();
        boolean hasTrade = lastTrade != null;
        snapshotEncoder.hasLastTrade(hasTrade ? BooleanType.TRUE : BooleanType.FALSE);
        if (hasTrade) {
            snapshotEncoder.lastTradePrice(lastTrade.priceNanos());
            snapshotEncoder.lastTradeSize(lastTrade.size());
        } else {
            snapshotEncoder.lastTradePrice(OrderBookSnapshotEncoder.lastTradePriceNullValue());
            snapshotEncoder.lastTradeSize(OrderBookSnapshotEncoder.lastTradeSizeNullValue());
        }

        String aggressor = hasTrade ? lastTrade.aggressor() : null;
        boolean hasAggressor = aggressor != null && !aggressor.isEmpty();
        snapshotEncoder.hasLastTradeAggressor(hasAggressor ? BooleanType.TRUE : BooleanType.FALSE);

        List<Level> bids = safeList(snapshot.bids());
        List<Level> asks = safeList(snapshot.asks());
        List<MetadataEntry> metadata = safeList(snapshot.metadata());

        encodeLevels(snapshotEncoder.bidsCount(bids.size()), bids);
        encodeLevels(snapshotEncoder.asksCount(asks.size()), asks);
        encodeMetadata(snapshotEncoder.metadataCount(metadata.size()), metadata);

        snapshotEncoder.timestamp(snapshot.timestamp());
        snapshotEncoder.venue(snapshot.venue());
        snapshotEncoder.symbol(snapshot.symbol());
        snapshotEncoder.tradingStatus(hasStatus ? tradingStatus : "");
        snapshotEncoder.lastTradeAggressor(hasAggressor ? aggressor : "");

        return MessageHeaderEncoder.ENCODED_LENGTH + snapshotEncoder.encodedLength();
    }

    private void encodeLevels(OrderBookSnapshotEncoder.BidsEncoder encoder, List<Level> levels) {
        for (Level level : levels) {
            encoder.next()
                    .priceNanos(level.priceNanos())
                    .size(level.size())
                    .orderCount(level.orderCount())
                    .maker(toNullable(level.maker()));
        }
    }

    private void encodeLevels(OrderBookSnapshotEncoder.AsksEncoder encoder, List<Level> levels) {
        for (Level level : levels) {
            encoder.next()
                    .priceNanos(level.priceNanos())
                    .size(level.size())
                    .orderCount(level.orderCount())
                    .maker(toNullable(level.maker()));
        }
    }

    private void encodeMetadata(OrderBookSnapshotEncoder.MetadataEncoder encoder, List<MetadataEntry> entries) {
        for (MetadataEntry entry : entries) {
            encoder.next()
                    .key(entry.key() == null ? "" : entry.key())
                    .value(entry.value() == null ? "" : entry.value());
        }
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

    private long decodeChecksum(UnsafeBuffer buffer) {
        headerDecoder.wrap(buffer, 0);
        snapshotDecoder.wrapAndApplyHeader(buffer, 0, headerDecoder);

        BooleanType tradingStatusPresent = snapshotDecoder.hasTradingStatus();
        BooleanType lastTradePresent = snapshotDecoder.hasLastTrade();
        BooleanType aggressorPresent = snapshotDecoder.hasLastTradeAggressor();

        long checksum = 0L;
        checksum += snapshotDecoder.instrumentId();
        checksum += snapshotDecoder.sequence();
        checksum += snapshotDecoder.isTrading() == BooleanType.TRUE ? 1 : 0;

        if (lastTradePresent == BooleanType.TRUE) {
            checksum += snapshotDecoder.lastTradePrice();
            checksum += snapshotDecoder.lastTradeSize();
        }

        OrderBookSnapshotDecoder.BidsDecoder bids = snapshotDecoder.bids();
        for (OrderBookSnapshotDecoder.BidsDecoder bid : bids) {
            checksum += bid.priceNanos();
            checksum += bid.size();
            checksum += bid.orderCount();
            checksum += makerChecksum(bid.maker());
        }

        OrderBookSnapshotDecoder.AsksDecoder asks = snapshotDecoder.asks();
        for (OrderBookSnapshotDecoder.AsksDecoder ask : asks) {
            checksum += ask.priceNanos();
            checksum += ask.size();
            checksum += ask.orderCount();
            checksum += makerChecksum(ask.maker());
        }

        OrderBookSnapshotDecoder.MetadataDecoder metadata = snapshotDecoder.metadata();
        for (OrderBookSnapshotDecoder.MetadataDecoder entry : metadata) {
            checksum += readAscii(entry::getKey);
            checksum += readAscii(entry::getValue);
        }

        checksum += readAscii(snapshotDecoder::getTimestamp);
        checksum += readAscii(snapshotDecoder::getVenue);
        checksum += readAscii(snapshotDecoder::getSymbol);

        if (tradingStatusPresent == BooleanType.TRUE) {
            checksum += readAscii(snapshotDecoder::getTradingStatus);
        } else {
            snapshotDecoder.skipTradingStatus();
            checksum += 1;
        }

        if (aggressorPresent == BooleanType.TRUE) {
            checksum += readAscii(snapshotDecoder::getLastTradeAggressor);
        } else {
            snapshotDecoder.skipLastTradeAggressor();
            checksum += 1;
        }

        return checksum;
    }

    private int readAscii(AppendableReader reader) {
        scratch.setLength(0);
        return reader.read(scratch);
    }

    private int makerChecksum(NullableBooleanType value) {
        if (value == NullableBooleanType.TRUE) {
            return 2;
        }
        if (value == NullableBooleanType.FALSE) {
            return 3;
        }
        return 1;
    }

    private NullableBooleanType toNullable(Boolean value) {
        if (value == null) {
            return NullableBooleanType.NULL_VALUE;
        }
        return value ? NullableBooleanType.TRUE : NullableBooleanType.FALSE;
    }

    private <T> List<T> safeList(List<T> value) {
        return value == null ? List.of() : value;
    }

    @FunctionalInterface
    private interface AppendableReader {
        int read(Appendable target);
    }
}
