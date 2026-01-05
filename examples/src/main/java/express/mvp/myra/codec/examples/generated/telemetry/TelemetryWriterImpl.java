package express.mvp.myra.codec.examples.generated.telemetry;

import java.lang.Override;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Auto-generated writer implementation for Telemetry.
 *
 * Fill fields directly, then pass this instance to a builder.
 */
public final class TelemetryWriterImpl implements TelemetryWriter {
    public String deviceId;

    public MemorySegment deviceIdScratch;

    public int sequence;

    public Health health;

    public long[] latenciesValues;

    public String note;

    public MemorySegment noteScratch;

    public MemorySegment payload;

    @Override
    public void writeTo(TelemetryBuilder builder, int index) {
        Objects.requireNonNull(builder, "builder");
        builder.setDeviceId(this.deviceId, this.deviceIdScratch);
        builder.setSequence(this.sequence);
        builder.setHealth(this.health);
        if (this.latenciesValues != null) {
            builder.setLatencies(this.latenciesValues);
        }
        if (this.note != null) {
            builder.setNote(this.note, this.noteScratch);
        }
        if (this.payload != null) {
            builder.setPayload(this.payload);
        }
    }
}
