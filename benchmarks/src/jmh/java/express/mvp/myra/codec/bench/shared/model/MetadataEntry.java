package express.mvp.myra.codec.bench.shared.model;

import java.util.Objects;

public final class MetadataEntry {
    private String key;
    private String value;

    public MetadataEntry() {
    }

    public MetadataEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return "MetadataEntry{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MetadataEntry other)) {
            return false;
        }
        return Objects.equals(key, other.key) && Objects.equals(value, other.value);
    }
}
