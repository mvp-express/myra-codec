package express.mvp.myra.codec.schema;

import com.fasterxml.jackson.annotation.JsonAlias;

public record FieldDefinition(
        int tag,
        String name,
        String type,
        boolean optional,
        boolean repeated,
        boolean deprecated,
        String deprecationNote,
        @JsonAlias("fixed_capacity") Integer fixedCapacity) {
    public FieldDefinition {
        if (tag < 1) {
            throw new IllegalArgumentException("tag must be >= 1");
        }
        // Provide defaults for missing values
        if (deprecationNote == null) {
            deprecationNote = "";
        }
        if (fixedCapacity != null && fixedCapacity < 0) {
            throw new IllegalArgumentException("fixedCapacity must be positive when provided");
        }
    }
}
