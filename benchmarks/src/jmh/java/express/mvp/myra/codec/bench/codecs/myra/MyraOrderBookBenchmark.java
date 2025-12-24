package express.mvp.myra.codec.bench.codecs.myra;

import express.mvp.myra.codec.bench.shared.OrderBookFixtures;
import express.mvp.myra.codec.bench.shared.model.Level;
import express.mvp.myra.codec.bench.shared.model.MetadataEntry;
import express.mvp.myra.codec.bench.shared.model.OrderBookSnapshot;
import express.mvp.myra.codec.bench.shared.model.Trade;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.roray.ffm.utils.memory.MemorySegmentPool;
import express.mvp.roray.ffm.utils.memory.SegmentBinaryReader;
import express.mvp.roray.ffm.utils.memory.SegmentBinaryWriter;
import express.mvp.roray.ffm.utils.memory.SegmentUtils;
import express.mvp.roray.ffm.utils.memory.Utf8View;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Measures encode/decode throughput for the Myra codec runtime using the shared order-book fixtures.
 */
@State(Scope.Benchmark)
public class MyraOrderBookBenchmark {

    private static final short ORDER_BOOK_TEMPLATE_ID = 4001;
    private static final short ORDER_BOOK_SCHEMA_VERSION = 1;
    private static final int SEGMENT_SIZE_BYTES = 64 * 1024;
    private static final int MIN_SCRATCH_BYTES = 4 * 1024;

    @Param({
            "benchmarks/data/order_book_snapshots_sample.json",
            "benchmarks/data/order_book_snapshot.json"
    })
    public String dataset;

    /**
     * Controls whether CRC32 checksum is computed during encoding.
     * Set to "false" for maximum throughput when integrity is verified elsewhere.
     */
    @Param({"true", "false"})
    public boolean checksumEnabled;

    /**
     * Controls which string encoding method is used.
     * - "varint": Two-pass encoding with VarInt length prefix (compact wire format)
     * - "fixed": Single-pass encoding with fixed 4-byte length prefix (faster encoding)
     */
    @Param({"varint", "fixed"})
    public String stringEncoding;

    /**
     * Controls which VarInt encoding method is used.
     * - "standard": Byte-by-byte VarInt encoding (1-5 separate writes)
     * - "fast": Batch VarInt encoding (single 64-bit write)
     */
    @Param({"standard", "fast"})
    public String varintMethod;

    private final SegmentBinaryWriter writer = new SegmentBinaryWriter();
    private final SegmentBinaryReader reader = new SegmentBinaryReader();
    private final MessageHeader header = new MessageHeader();
    private final Utf8View utf8View = new Utf8View();

    private MemorySegmentPool segmentPool;
    private Arena scratchArena;
    private MemorySegment scratchBuffer;
    private List<OrderBookSnapshot> fixtures;
    private List<MemorySegment> encodedFrames;
    private int encodeCursor;
    private int decodeCursor;

    @Setup(org.openjdk.jmh.annotations.Level.Trial)
    public void setup() throws IOException {
        fixtures = new ArrayList<>(OrderBookFixtures.load(dataset));
        if (fixtures.isEmpty()) {
            throw new IllegalStateException("Fixture dataset is empty: " + dataset);
        }
        encodeCursor = 0;
        decodeCursor = 0;
        segmentPool = new MemorySegmentPool(SEGMENT_SIZE_BYTES, 8, 128, false);
        scratchArena = Arena.ofShared();
        scratchBuffer = scratchArena.allocate(MIN_SCRATCH_BYTES);
        encodedFrames = preEncode(fixtures);
    }

    @TearDown(org.openjdk.jmh.annotations.Level.Trial)
    public void tearDown() {
        if (scratchArena != null) {
            scratchArena.close();
        }
    }

    @Benchmark
    public void encodeMyra(Blackhole blackhole) {
        OrderBookSnapshot snapshot = nextSnapshot();
        MemorySegment target = segmentPool.acquire();
        try {
            long frameLength = encodeSnapshot(snapshot, target);
            blackhole.consume(frameLength);
            blackhole.consume(target);
        } finally {
            segmentPool.release(target);
        }
    }

    @Benchmark
    public void decodeMyra(Blackhole blackhole) {
        MemorySegment frame = nextEncodedFrame();
        long checksum = decodeSnapshot(frame);
        blackhole.consume(checksum);
    }

    private List<MemorySegment> preEncode(List<OrderBookSnapshot> snapshots) {
        List<MemorySegment> frames = new ArrayList<>(snapshots.size());
        for (OrderBookSnapshot snapshot : snapshots) {
            MemorySegment segment = segmentPool.acquire();
            long frameLength = encodeSnapshot(snapshot, segment);
            byte[] copy = new byte[(int) frameLength];
            MemorySegment.copy(segment, 0, MemorySegment.ofArray(copy), 0, frameLength);
            frames.add(MemorySegment.ofArray(copy));
            segmentPool.release(segment);
        }
        return frames;
    }

    private OrderBookSnapshot nextSnapshot() {
        OrderBookSnapshot snapshot = fixtures.get(encodeCursor);
        encodeCursor = (encodeCursor + 1) % fixtures.size();
        return snapshot;
    }

    private MemorySegment nextEncodedFrame() {
        MemorySegment frame = encodedFrames.get(decodeCursor);
        decodeCursor = (decodeCursor + 1) % encodedFrames.size();
        return frame;
    }

    private long encodeSnapshot(OrderBookSnapshot snapshot, MemorySegment target) {
        writer.wrap(target);
        header.wrap(target, 0);
        header.setTemplateId(ORDER_BOOK_TEMPLATE_ID);
        header.setSchemaVersion(ORDER_BOOK_SCHEMA_VERSION);
        header.setFlags((byte) 0);

        writer.position(MessageHeader.HEADER_SIZE);
        writeString(snapshot.timestamp());
        writeString(snapshot.venue());
        writeString(snapshot.symbol());
        writer.writeIntBE(snapshot.instrumentId());
        writer.writeLongBE(snapshot.sequence());
        writer.writeBoolean(snapshot.isTrading());
        writeNullableString(snapshot.tradingStatus());
        writeTrade(snapshot.lastTrade());
        writeLevels(snapshot.bids());
        writeLevels(snapshot.asks());
        writeMetadata(snapshot.metadata());

        long frameLength = writer.position();
        header.setFrameLength((int) frameLength);

        // Only compute checksum if enabled (configurable via @Param)
        if (checksumEnabled) {
            MemorySegment payload =
                    target.asSlice(MessageHeader.HEADER_SIZE, frameLength - MessageHeader.HEADER_SIZE);
            header.setChecksum(SegmentUtils.calculateCrc32(payload));
        } else {
            header.setChecksum(0);
        }
        return frameLength;
    }

    private void writeTrade(Trade trade) {
        if (trade == null) {
            writer.writeBoolean(false);
            return;
        }
        writer.writeBoolean(true);
        writer.writeLongBE(trade.priceNanos());
        writer.writeIntBE(trade.size());
        writeNullableString(trade.aggressor());
    }

    private void writeLevels(List<Level> levels) {
        List<Level> safeLevels = levels == null ? List.of() : levels;
        writeVarInt(safeLevels.size());
        for (Level level : safeLevels) {
            writer.writeLongBE(level.priceNanos());
            writer.writeIntBE(level.size());
            writer.writeIntBE(level.orderCount());
            writer.writeNullableBoolean(level.maker());
        }
    }

    private void writeMetadata(List<MetadataEntry> metadata) {
        List<MetadataEntry> safeMetadata = metadata == null ? List.of() : metadata;
        writeVarInt(safeMetadata.size());
        for (MetadataEntry entry : safeMetadata) {
            writeString(entry.key());
            writeString(entry.value());
        }
    }

    private void writeVarInt(int value) {
        if ("fast".equals(varintMethod)) {
            writer.writeVarIntFast(value);
        } else {
            writer.writeVarInt(value);
        }
    }

    private void writeString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("String fields must not be null");
        }
        if ("fixed".equals(stringEncoding)) {
            // Single-pass encoding with fixed 4-byte length prefix
            writer.writeStringFixedLength(value);
        } else {
            // Two-pass encoding with VarInt length prefix (default)
            ensureScratchCapacity(value.length() * 4L);
            writer.writeString(value, scratchBuffer);
        }
    }

    private void writeNullableString(String value) {
        if (value == null) {
            writer.writeBoolean(false);
            return;
        }
        writer.writeBoolean(true);
        if ("fixed".equals(stringEncoding)) {
            writer.writeStringFixedLength(value);
        } else {
            ensureScratchCapacity(value.length() * 4L);
            writer.writeString(value, scratchBuffer);
        }
    }

    private void ensureScratchCapacity(long requiredBytes) {
        if (requiredBytes <= scratchBuffer.byteSize()) {
            return;
        }
        long newCapacity = scratchBuffer.byteSize();
        while (newCapacity < requiredBytes) {
            newCapacity *= 2;
        }
        scratchBuffer = scratchArena.allocate(newCapacity);
    }

    private long decodeSnapshot(MemorySegment encoded) {
        reader.wrap(encoded);
        header.wrap(encoded, 0);
        if (header.getTemplateId() != ORDER_BOOK_TEMPLATE_ID) {
            throw new IllegalStateException("Unexpected template id: " + header.getTemplateId());
        }
        reader.position(MessageHeader.HEADER_SIZE);

        long checksum = 0;
        checksum += readUtf8();
        checksum += readUtf8();
        checksum += readUtf8();
        checksum += reader.readIntBE();
        checksum += reader.readLongBE();
        checksum += reader.readBoolean() ? 1 : 0;
        checksum += readNullableUtf8();
        checksum += readTradeChecksum();
        checksum += readLevelsChecksum();
        checksum += readLevelsChecksum();
        checksum += readMetadataChecksum();
        return checksum;
    }

    private long readUtf8() {
        if ("fixed".equals(stringEncoding)) {
            reader.readStringFixedLength(utf8View);
        } else {
            reader.readString(utf8View);
        }
        return utf8View.byteSize();
    }

    private long readNullableUtf8() {
        if ("fixed".equals(stringEncoding)) {
            return reader.readNullableStringFixedLength(utf8View) ? utf8View.byteSize() : 0;
        } else {
            return reader.readNullableString(utf8View) ? utf8View.byteSize() : 0;
        }
    }

    private long readTradeChecksum() {
        if (!reader.readBoolean()) {
            return 1;
        }
        long checksum = reader.readLongBE();
        checksum += reader.readIntBE();
        checksum += readNullableUtf8();
        return checksum;
    }

    private long readLevelsChecksum() {
        int count = reader.readVarInt();
        long checksum = count;
        for (int i = 0; i < count; i++) {
            checksum += reader.readLongBE();
            checksum += reader.readIntBE();
            checksum += reader.readIntBE();
            checksum += readNullableBoolean();
        }
        return checksum;
    }

    private int readNullableBoolean() {
        if (!reader.readBoolean()) {
            return 1;
        }
        return reader.readBoolean() ? 2 : 3;
    }

    private long readMetadataChecksum() {
        int count = reader.readVarInt();
        long checksum = count;
        for (int i = 0; i < count; i++) {
            checksum += readUtf8();
            checksum += readUtf8();
        }
        return checksum;
    }
}
