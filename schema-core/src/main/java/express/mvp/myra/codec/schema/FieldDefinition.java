package express.mvp.myra.codec.schema;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Describes a single field in a message schema.
 *
 * @param tag the 1-based field tag used for ordering and compatibility
 * @param name the field name
 * @param type the field type name (primitive, enum, or message type)
 * @param optional whether the field is optional
 * @param repeated whether the field is a repeating group
 * @param deprecated whether the field is deprecated
 * @param deprecationNote human-friendly deprecation guidance
 * @param fixedCapacity fixed inline capacity for string/bytes fields, or null when variable
 */
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
