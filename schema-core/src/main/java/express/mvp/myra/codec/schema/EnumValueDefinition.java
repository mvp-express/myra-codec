package express.mvp.myra.codec.schema;

/**
 * Describes a single enum value in a schema.
 *
 * @param name the enum constant name
 * @param id the numeric id encoded on the wire
 */
public record EnumValueDefinition(String name, int id) {}
