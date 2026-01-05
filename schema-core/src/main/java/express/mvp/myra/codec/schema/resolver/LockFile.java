package express.mvp.myra.codec.schema.resolver;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashMap;
import java.util.Map;

/** Lock file model for persisting stable ids across schema evolution. */
@SuppressFBWarnings(
        value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
        justification = "Jackson serialization accesses fields reflectively.")
public final class LockFile {

    /** Schema metadata such as namespace, version, and source file. */
    public Map<String, Object> schemaInfo = new HashMap<>();

    /** Stable message ids by message name. */
    public Map<String, MessageLock> messages = new HashMap<>();

    /** Stable enum value ids by enum name. */
    public Map<String, EnumLock> enums = new HashMap<>();

    /** Reserved ids for removed fields. */
    public Map<String, Object> reservedIds = new HashMap<>();

    /** Lock entry for a message and its fields. */
    public static class MessageLock {
        public int id;
        public Map<String, Integer> fields = new HashMap<>();
    }

    @SuppressFBWarnings(
            value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
            justification = "Jackson serialization accesses fields reflectively.")
    /** Lock entry for enum values. */
    public static class EnumLock {
        public Map<String, Integer> values = new HashMap<>();
    }

    /**
     * Creates an empty lock file.
     *
     * @return a new empty lock file instance
     */
    public static LockFile empty() {
        return new LockFile();
    }
}
