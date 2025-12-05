package express.mvp.myra.codec.codegen.resolver;

import java.util.HashMap;
import java.util.Map;

public final class LockFile {

    public Map<String, Object> schemaInfo = new HashMap<>();
    public Map<String, MessageLock> messages = new HashMap<>();
    public Map<String, EnumLock> enums = new HashMap<>();
    public Map<String, Object> reservedIds = new HashMap<>();

    public static class MessageLock {
        public int id;
        public Map<String, Integer> fields = new HashMap<>();
    }

    public static class EnumLock {
        public Map<String, Integer> values = new HashMap<>();
    }

    public static LockFile empty() {
        return new LockFile();
    }
}
