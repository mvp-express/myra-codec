package express.mvp.myra.codec.examples.generated.orderbook;

import java.lang.Override;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Auto-generated array-backed writer for MetadataEntry.
 *
 * Populate arrays and reuse this instance for repeated fields.
 */
public final class MetadataEntryArrayWriter implements MetadataEntryWriter {
    public int count = 0;

    public String[] key;

    public MemorySegment keyScratch;

    public String[] value;

    public MemorySegment valueScratch;

    public int count() {
        return this.count;
    }

    @Override
    public void writeTo(MetadataEntryBuilder builder, int index) {
        Objects.requireNonNull(builder, "builder");
        if (this.key != null) {
            builder.setKey(this.key[index], this.keyScratch);
        }
        if (this.value != null) {
            builder.setValue(this.value[index], this.valueScratch);
        }
    }
}
