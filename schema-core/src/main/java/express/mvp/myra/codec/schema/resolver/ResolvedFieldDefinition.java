package express.mvp.myra.codec.schema.resolver;

/**
 * Resolved field definition with a stable id.
 *
 * @param name the field name
 * @param id the stable field id
 * @param tag the 1-based field tag
 * @param type the field type
 * @param optional whether the field is optional
 * @param repeated whether the field is repeated
 * @param deprecated whether the field is deprecated
 * @param deprecationNote the deprecation guidance
 * @param fixedCapacity fixed inline capacity for strings/bytes, or null
 */
public record ResolvedFieldDefinition(
        String name,
        int id,
        int tag,
        String type,
        boolean optional,
        boolean repeated,
        boolean deprecated,
        String deprecationNote,
        Integer fixedCapacity) {}
