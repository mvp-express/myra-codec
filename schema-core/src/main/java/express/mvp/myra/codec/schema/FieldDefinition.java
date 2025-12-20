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
            throw new IllegalArgumentException("Tag must be greater than 0");
        }
        // Provide defaults for missing values
        if (deprecationNote == null) {
            deprecationNote = "Deprecated field";
        }
        if (fixedCapacity != null && fixedCapacity < 0) {
            throw new IllegalArgumentException("Fixed capacity must be non-negative");
        }
    }
}
