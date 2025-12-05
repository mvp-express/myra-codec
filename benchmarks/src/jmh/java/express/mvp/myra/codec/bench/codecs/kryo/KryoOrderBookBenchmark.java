package express.mvp.myra.codec.bench.codecs.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import express.mvp.myra.codec.bench.shared.OrderBookFixtures;
import express.mvp.myra.codec.bench.shared.model.Level;
import express.mvp.myra.codec.bench.shared.model.MetadataEntry;
import express.mvp.myra.codec.bench.shared.model.OrderBookSnapshot;
import express.mvp.myra.codec.bench.shared.model.Trade;
import java.util.ArrayList;
import java.util.List;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Baseline benchmark that exercises Kryo encode/decode pipelines with the shared order-book fixtures.
 */
@State(Scope.Benchmark)
public class KryoOrderBookBenchmark {

    private static final int INITIAL_BUFFER_BYTES = 8 * 1024;

    @Param({
            "benchmarks/data/order_book_snapshots_sample.json",
            "benchmarks/data/order_book_snapshot.json"
    })
    public String dataset;

    private final Output encodeBuffer = new Output(INITIAL_BUFFER_BYTES, -1);
    private final Input decodeBuffer = new Input();

    private Kryo kryo;
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
        encodeCursor = 0;
        decodeCursor = 0;
        kryo = configureKryo();
        encodedPayloads = preEncode(fixtures);
    }

    @Benchmark
    public void encodeKryo(Blackhole blackhole) {
        OrderBookSnapshot snapshot = nextSnapshot();
        encodeBuffer.reset();
        kryo.writeObject(encodeBuffer, snapshot);
        blackhole.consume(encodeBuffer.position());
    }

    @Benchmark
    public void decodeKryo(Blackhole blackhole) {
        byte[] payload = nextPayload();
        decodeBuffer.setBuffer(payload, 0, payload.length);
        OrderBookSnapshot snapshot = kryo.readObject(decodeBuffer, OrderBookSnapshot.class);
        blackhole.consume(snapshot.sequence());
    }

    private Kryo configureKryo() {
        Kryo instance = new Kryo();
        instance.setRegistrationRequired(false);
        instance.setReferences(false);
        instance.register(OrderBookSnapshot.class);
        instance.register(Trade.class);
        instance.register(Level.class);
        instance.register(MetadataEntry.class);
        instance.register(ArrayList.class);
        return instance;
    }

    private List<byte[]> preEncode(List<OrderBookSnapshot> snapshots) {
        List<byte[]> payloads = new ArrayList<>(snapshots.size());
        for (OrderBookSnapshot snapshot : snapshots) {
            encodeBuffer.reset();
            kryo.writeObject(encodeBuffer, snapshot);
            payloads.add(encodeBuffer.toBytes());
        }
        encodeBuffer.reset();
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
}
