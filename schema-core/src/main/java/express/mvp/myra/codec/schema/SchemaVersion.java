package express.mvp.myra.codec.schema;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a semantic version for myra-codec schemas.
 *
 * <p>Schema versions use semantic versioning (MAJOR.MINOR.PATCH) with the following compatibility
 * rules:
 *
 * <ul>
 *   <li><b>MAJOR:</b> Breaking changes - new field layouts, removed fields, type changes
 *   <li><b>MINOR:</b> Backward-compatible additions - new optional fields, new message types
 *   <li><b>PATCH:</b> Backward-compatible fixes - documentation, cosmetic changes
 * </ul>
 *
 * <p>The wire format encodes version as a single {@code short} value using the formula: {@code
 * (major * 256) + minor} (patch is not encoded in wire format, only used for schema management).
 *
 * <p><b>Wire Format Compatibility:</b>
 *
 * <ul>
 *   <li>Messages from the same major version are compatible
 *   <li>Decoders can process messages with minor version ≤ their schema's minor version
 *   <li>Major version mismatches should be rejected
 * </ul>
 *
 * <p><b>Example:</b>
 *
 * <pre>{@code
 * SchemaVersion v = SchemaVersion.parse("2.3.1");
 * short wireVersion = v.toWireFormat();  // Returns (2 * 256) + 3 = 515
 *
 * // Compatibility check
 * SchemaVersion decoder = SchemaVersion.parse("2.5.0");
 * if (v.isCompatibleWith(decoder)) {
 *     // Safe to decode
 * }
 * }</pre>
 *
 * @see <a href="https://semver.org/">Semantic Versioning 2.0.0</a>
 */
public final class SchemaVersion implements Comparable<SchemaVersion> {

    /** Maximum major version that can be encoded in wire format (0-127, since we use 8 bits). */
    public static final int MAX_MAJOR = 127;

    /** Maximum minor version that can be encoded in wire format (0-255). */
    public static final int MAX_MINOR = 255;

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?$");

    private final int major;
    private final int minor;
    private final int patch;

    /**
     * Creates a new schema version.
     *
     * @param major the major version (0-127 for wire format compatibility)
     * @param minor the minor version (0-255 for wire format compatibility)
     * @param patch the patch version (not encoded in wire format)
     * @throws IllegalArgumentException if versions are negative or exceed limits
     */
    public SchemaVersion(int major, int minor, int patch) {
        if (major < 0 || major > MAX_MAJOR) {
            throw new IllegalArgumentException(
                    "Major version must be 0-" + MAX_MAJOR + ", got: " + major);
        }
        if (minor < 0 || minor > MAX_MINOR) {
            throw new IllegalArgumentException(
                    "Minor version must be 0-" + MAX_MINOR + ", got: " + minor);
        }
        if (patch < 0) {
            throw new IllegalArgumentException("Patch version must be non-negative, got: " + patch);
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Creates a version with major.minor only (patch defaults to 0).
     *
     * @param major the major version
     * @param minor the minor version
     */
    public SchemaVersion(int major, int minor) {
        this(major, minor, 0);
    }

    /**
     * Parses a semantic version string.
     *
     * @param versionString the version string (e.g., "1.0.0", "2.3", "1.0")
     * @return the parsed SchemaVersion
     * @throws IllegalArgumentException if the string is not a valid version
     */
    public static SchemaVersion parse(String versionString) {
        Objects.requireNonNull(versionString, "versionString must not be null");

        Matcher matcher = VERSION_PATTERN.matcher(versionString.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid version string: '"
                            + versionString
                            + "'. "
                            + "Expected format: MAJOR.MINOR or MAJOR.MINOR.PATCH");
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;

        return new SchemaVersion(major, minor, patch);
    }

    /**
     * Creates a SchemaVersion from its wire format representation.
     *
     * @param wireVersion the wire format value (major * 256 + minor)
     * @return the SchemaVersion
     */
    public static SchemaVersion fromWireFormat(short wireVersion) {
        int unsigned = Short.toUnsignedInt(wireVersion);
        int major = unsigned / 256;
        int minor = unsigned % 256;
        return new SchemaVersion(major, minor, 0);
    }

    /**
     * Returns the major version number.
     *
     * @return the major version (0-127)
     */
    public int major() {
        return major;
    }

    /**
     * Returns the minor version number.
     *
     * @return the minor version (0-255)
     */
    public int minor() {
        return minor;
    }

    /**
     * Returns the patch version number.
     *
     * @return the patch version
     */
    public int patch() {
        return patch;
    }

    /**
     * Converts this version to the wire format representation. The formula is: {@code (major * 256)
     * + minor}.
     *
     * @return the wire format value as a short
     */
    public short toWireFormat() {
        return (short) ((major * 256) + minor);
    }

    /**
     * Checks if this schema version is compatible with a decoder schema version.
     *
     * <p>Compatibility rules:
     *
     * <ul>
     *   <li>Major versions must match exactly
     *   <li>This version's minor must be ≤ decoder's minor (decoder can handle newer features)
     * </ul>
     *
     * @param decoderVersion the version of the decoder/consumer schema
     * @return true if this version can be decoded by the decoder
     */
    public boolean isCompatibleWith(SchemaVersion decoderVersion) {
        // Major versions must match
        if (this.major != decoderVersion.major) {
            return false;
        }
        // Decoder must have same or higher minor version
        return this.minor <= decoderVersion.minor;
    }

    /**
     * Checks if this version is a breaking change from another version.
     *
     * @param other the other version to compare
     * @return true if major versions differ (indicating breaking change)
     */
    public boolean isBreakingChangeFrom(SchemaVersion other) {
        return this.major != other.major;
    }

    @Override
    public int compareTo(SchemaVersion other) {
        int cmp = Integer.compare(this.major, other.major);
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(this.minor, other.minor);
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(this.patch, other.patch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SchemaVersion other)) {
            return false;
        }
        return major == other.major && minor == other.minor && patch == other.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    /**
     * Returns a version string without patch for display (e.g., "1.2").
     *
     * @return the major.minor string
     */
    public String toShortString() {
        return major + "." + minor;
    }
}
