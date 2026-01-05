package express.mvp.myra.codec.examples;

import express.mvp.myra.codec.examples.generated.telemetry.Health;
import express.mvp.myra.codec.examples.generated.telemetry.TelemetryBuilder;
import express.mvp.myra.codec.examples.generated.telemetry.TelemetryFlyweight;
import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.roray.ffm.utils.memory.MemorySegmentPool;
import express.mvp.roray.ffm.utils.memory.PooledSegment;
import express.mvp.roray.ffm.utils.memory.Utf8View;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;

/** Demonstrates encoding and decoding a simple telemetry schema. */
public final class TelemetryExampleApp {

    private TelemetryExampleApp() {}

    /** Runs a compact telemetry encode/decode demo. */
    public static void main(String[] args) {
        MemorySegmentPool pool = new MemorySegmentPool(2048, 1, 8);
        MessageEncoder encoder = new MessageEncoder(pool);
        MemorySegment scratch = MemorySegment.ofArray(new byte[256]);

        byte[] payloadBytes = new byte[] {1, 2, 3, 4};

        try (PooledSegment pooled =
                TelemetryBuilder.allocate(encoder, 1024)
                        .setDeviceId("sensor-01", scratch)
                        .setSequence(42)
                        .setHealth(Health.WARN)
                        .setLatencies(new long[] {10L, 15L, 12L})
                        .setNote("baseline", scratch)
                        .setPayload(MemorySegment.ofArray(payloadBytes))
                        .build(
                                (short) TelemetryFlyweight.TEMPLATE_ID,
                                TelemetryFlyweight.SCHEMA_VERSION)) {
            decodeAndPrint(pooled.segment());
        }
    }

    private static void decodeAndPrint(MemorySegment segment) {
        TelemetryFlyweight fw = new TelemetryFlyweight();
        fw.wrap(segment, MessageHeader.HEADER_SIZE);
        fw.validate();

        System.out.println("deviceId=" + fw.getDeviceId().toString());
        System.out.println("sequence=" + fw.getSequence());
        Health health = Health.fromId(Byte.toUnsignedInt(fw.getHealth()));
        System.out.println("health=" + health);

        int count = fw.getLatenciesCount();
        long[] latencies = new long[count];
        for (int i = 0; i < count; i++) {
            latencies[i] = fw.getLatenciesAt(i);
        }
        System.out.println("latencies=" + Arrays.toString(latencies));

        if (fw.hasNote()) {
            System.out.println("note=" + fw.getNote().toString());
        } else {
            System.out.println("note=ABSENT");
        }

        if (fw.hasPayload()) {
            Utf8View payloadView = fw.getPayload();
            byte[] payload = new byte[(int) payloadView.byteSize()];
            MemorySegment.copy(
                    payloadView.segment(),
                    payloadView.offset(),
                    MemorySegment.ofArray(payload),
                    0,
                    payloadView.byteSize());
            System.out.println("payloadBytes=" + Arrays.toString(payload));
        } else {
            System.out.println("payloadBytes=ABSENT");
        }
    }
}
