package express.mvp.myra.codec.bench.codecs.avro;

import express.mvp.myra.codec.bench.shared.OrderBookFixtures;
import express.mvp.myra.codec.bench.shared.model.Level;
import express.mvp.myra.codec.bench.shared.model.MetadataEntry;
import express.mvp.myra.codec.bench.shared.model.OrderBookSnapshot;
import express.mvp.myra.codec.bench.shared.model.Trade;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Encode/decode benchmarks for Avro using the shared order-book fixtures.
 */
@State(Scope.Benchmark)
public class AvroOrderBookBenchmark {

    private static final String NAMESPACE = "express.mvp.myra.codec.bench.avro";

    private static final Schema LEVEL_SCHEMA = SchemaBuilder.record("Level")
            .namespace(NAMESPACE)
            .fields()
            .requiredLong("priceNanos")
            .requiredInt("size")
            .requiredInt("orderCount")
            .name("maker").type().unionOf().nullType().and().booleanType().endUnion().nullDefault()
            .endRecord();

    private static final Schema TRADE_SCHEMA = SchemaBuilder.record("Trade")
            .namespace(NAMESPACE)
            .fields()
            .requiredLong("priceNanos")
            .requiredInt("size")
            .name("aggressor").type().unionOf().nullType().and().stringType().endUnion().nullDefault()
            .endRecord();

    private static final Schema METADATA_SCHEMA = SchemaBuilder.record("MetadataEntry")
            .namespace(NAMESPACE)
            .fields()
            .requiredString("key")
            .requiredString("value")
            .endRecord();

    private static final Schema SNAPSHOT_SCHEMA = SchemaBuilder.record("OrderBookSnapshot")
            .namespace(NAMESPACE)
            .fields()
            .requiredString("timestamp")
            .requiredString("venue")
            .requiredString("symbol")
            .requiredInt("instrumentId")
            .requiredLong("sequence")
            .requiredBoolean("isTrading")
            .name("tradingStatus").type().unionOf().nullType().and().stringType().endUnion().nullDefault()
            .name("lastTrade").type().unionOf().nullType().and().type(TRADE_SCHEMA).endUnion().nullDefault()
            .name("bids").type().array().items(LEVEL_SCHEMA).noDefault()
            .name("asks").type().array().items(LEVEL_SCHEMA).noDefault()
            .name("metadata").type().array().items(METADATA_SCHEMA).noDefault()
            .endRecord();

    private static final Schema BIDS_ARRAY_SCHEMA = SNAPSHOT_SCHEMA.getField("bids").schema();
    private static final Schema ASKS_ARRAY_SCHEMA = SNAPSHOT_SCHEMA.getField("asks").schema();
    private static final Schema METADATA_ARRAY_SCHEMA = SNAPSHOT_SCHEMA.getField("metadata").schema();

    @Param({
            "benchmarks/data/order_book_snapshots_sample.json",
            "benchmarks/data/order_book_snapshot.json"
    })
    public String dataset;

    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final ByteArrayOutputStream encodeBuffer = new ByteArrayOutputStream(32 * 1024);

    private GenericDatumWriter<GenericRecord> datumWriter;
    private GenericDatumReader<GenericRecord> datumReader;
    private BinaryEncoder reuseEncoder;
    private BinaryDecoder reuseDecoder;

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
        datumWriter = new GenericDatumWriter<>(SNAPSHOT_SCHEMA);
        datumReader = new GenericDatumReader<>(SNAPSHOT_SCHEMA);
        encodedPayloads = preEncode(fixtures);
        encodeCursor = 0;
        decodeCursor = 0;
    }

    @Benchmark
    public void encodeAvro(Blackhole blackhole) throws IOException {
        OrderBookSnapshot snapshot = nextSnapshot();
        encodeBuffer.reset();
        reuseEncoder = encoderFactory.directBinaryEncoder(encodeBuffer, reuseEncoder);
        datumWriter.write(toRecord(snapshot), reuseEncoder);
        reuseEncoder.flush();
        blackhole.consume(encodeBuffer.size());
    }

    @Benchmark
    public void decodeAvro(Blackhole blackhole) throws IOException {
        byte[] payload = nextPayload();
        reuseDecoder = decoderFactory.binaryDecoder(payload, reuseDecoder);
        GenericRecord record = datumReader.read(null, reuseDecoder);
        blackhole.consume(recordChecksum(record));
    }

    private List<byte[]> preEncode(List<OrderBookSnapshot> snapshots) {
        List<byte[]> payloads = new ArrayList<>(snapshots.size());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(32 * 1024);
        BinaryEncoder encoder = null;
        for (OrderBookSnapshot snapshot : snapshots) {
            buffer.reset();
            encoder = encoderFactory.directBinaryEncoder(buffer, encoder);
            try {
                datumWriter.write(toRecord(snapshot), encoder);
                encoder.flush();
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to pre-encode snapshot", ex);
            }
            payloads.add(buffer.toByteArray());
        }
        return payloads;
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

    private GenericRecord toRecord(OrderBookSnapshot snapshot) {
        GenericData.Record record = new GenericData.Record(SNAPSHOT_SCHEMA);
        record.put("timestamp", snapshot.timestamp());
        record.put("venue", snapshot.venue());
        record.put("symbol", snapshot.symbol());
        record.put("instrumentId", snapshot.instrumentId());
        record.put("sequence", snapshot.sequence());
        record.put("isTrading", snapshot.isTrading());
        record.put("tradingStatus", snapshot.tradingStatus());
        record.put("lastTrade", toTradeRecord(snapshot.lastTrade()));
        record.put("bids", toLevelArray(snapshot.bids(), BIDS_ARRAY_SCHEMA));
        record.put("asks", toLevelArray(snapshot.asks(), ASKS_ARRAY_SCHEMA));
        record.put("metadata", toMetadataArray(snapshot.metadata()));
        return record;
    }

    private GenericData.Record toTradeRecord(Trade trade) {
        if (trade == null) {
            return null;
        }
        GenericData.Record record = new GenericData.Record(TRADE_SCHEMA);
        record.put("priceNanos", trade.priceNanos());
        record.put("size", trade.size());
        record.put("aggressor", trade.aggressor());
        return record;
    }

    private GenericData.Array<GenericRecord> toLevelArray(List<Level> levels, Schema arraySchema) {
        List<Level> safeLevels = levels == null ? List.of() : levels;
        GenericData.Array<GenericRecord> array = new GenericData.Array<>(safeLevels.size(), arraySchema);
        for (Level level : safeLevels) {
            GenericData.Record record = new GenericData.Record(LEVEL_SCHEMA);
            record.put("priceNanos", level.priceNanos());
            record.put("size", level.size());
            record.put("orderCount", level.orderCount());
            record.put("maker", level.maker());
            array.add(record);
        }
        return array;
    }

    private GenericData.Array<GenericRecord> toMetadataArray(List<MetadataEntry> metadataEntries) {
        List<MetadataEntry> safeEntries = metadataEntries == null ? List.of() : metadataEntries;
        GenericData.Array<GenericRecord> array = new GenericData.Array<>(safeEntries.size(), METADATA_ARRAY_SCHEMA);
        for (MetadataEntry entry : safeEntries) {
            GenericData.Record record = new GenericData.Record(METADATA_SCHEMA);
            record.put("key", entry.key());
            record.put("value", entry.value());
            array.add(record);
        }
        return array;
    }

    private long recordChecksum(GenericRecord record) {
        long checksum = 0L;
        checksum += charLength(record.get("timestamp"));
        checksum += charLength(record.get("venue"));
        checksum += charLength(record.get("symbol"));
        checksum += (int) record.get("instrumentId");
        checksum += (long) record.get("sequence");
        checksum += Boolean.TRUE.equals(record.get("isTrading")) ? 1 : 0;
        checksum += charLength(record.get("tradingStatus"));
        checksum += tradeChecksum((GenericRecord) record.get("lastTrade"));
        checksum += levelsChecksum(record.get("bids"));
        checksum += levelsChecksum(record.get("asks"));
        checksum += metadataChecksum(record.get("metadata"));
        return checksum;
    }

    private long tradeChecksum(GenericRecord tradeRecord) {
        if (tradeRecord == null) {
            return 1L;
        }
        long checksum = (long) tradeRecord.get("priceNanos");
        checksum += (int) tradeRecord.get("size");
        checksum += charLength(tradeRecord.get("aggressor"));
        return checksum;
    }

    @SuppressWarnings("unchecked")
    private long levelsChecksum(Object levelsObject) {
        if (levelsObject == null) {
            return 0L;
        }
        long checksum = 0L;
        for (GenericRecord level : (Iterable<GenericRecord>) levelsObject) {
            checksum += (long) level.get("priceNanos");
            checksum += (int) level.get("size");
            checksum += (int) level.get("orderCount");
            Object maker = level.get("maker");
            if (maker == null) {
                checksum += 1;
            } else {
                checksum += Boolean.TRUE.equals(maker) ? 2 : 3;
            }
        }
        return checksum;
    }

    @SuppressWarnings("unchecked")
    private long metadataChecksum(Object metadataObject) {
        if (metadataObject == null) {
            return 0L;
        }
        long checksum = 0L;
        for (GenericRecord entry : (Iterable<GenericRecord>) metadataObject) {
            checksum += charLength(entry.get("key"));
            checksum += charLength(entry.get("value"));
        }
        return checksum;
    }

    private long charLength(Object value) {
        if (value == null) {
            return 0L;
        }
        return ((CharSequence) value).length();
    }
}
