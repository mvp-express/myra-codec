package express.mvp.myra.codec.examples.generated.orderbook;

import java.lang.Override;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Auto-generated writer implementation for MetadataEntry.
 *
 * Fill fields directly, then pass this instance to a builder.
 */
public final class MetadataEntryWriterImpl implements MetadataEntryWriter {
    public String key;

    public MemorySegment keyScratch;

    public String value;

    public MemorySegment valueScratch;

    @Override
    public void writeTo(MetadataEntryBuilder builder, int index) {
        Objects.requireNonNull(builder, "builder");
        builder.setKey(this.key, this.keyScratch);
        builder.setValue(this.value, this.valueScratch);
    }
}
