package express.mvp.myra.codec.codegen;

import static org.junit.jupiter.api.Assertions.*;

import express.mvp.myra.codec.codegen.resolver.LockFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LockFileManagerTest {

    @TempDir Path tempDir;

    @Test
    void load_WithNonExistentFile_ShouldReturnNull() throws Exception {
        Path lockPath = tempDir.resolve("nonexistent.myra.lock");
        LockFile result = LockFileManager.load(lockPath);
        assertNull(result);
    }

    @Test
    void save_AndLoad_ShouldRoundTrip() throws Exception {
        Path lockPath = tempDir.resolve("test.myra.lock");

        LockFile original = LockFile.empty();
        original.schemaInfo = Map.of("namespace", "com.test", "version", "1.0.0", "sourceFile", "test.myra.yml");

        LockFile.MessageLock msgLock = new LockFile.MessageLock();
        msgLock.id = 1;
        msgLock.fields = Map.of("field1", 1, "field2", 2);
        original.messages = Map.of("TestMessage", msgLock);

        LockFile.EnumLock enumLock = new LockFile.EnumLock();
        enumLock.values = Map.of("VALUE1", 0, "VALUE2", 1);
        original.enums = Map.of("TestEnum", enumLock);

        LockFileManager.save(original, lockPath);
        assertTrue(Files.exists(lockPath));

        LockFile loaded = LockFileManager.load(lockPath);
        assertNotNull(loaded);
        assertEquals("com.test", loaded.schemaInfo.get("namespace"));
        assertEquals("1.0.0", loaded.schemaInfo.get("version"));
        assertEquals("test.myra.yml", loaded.schemaInfo.get("sourceFile"));
        assertEquals(1, loaded.messages.get("TestMessage").id);
        assertEquals(2, loaded.messages.get("TestMessage").fields.size());
        assertEquals(2, loaded.enums.get("TestEnum").values.size());
    }

    @Test
    void save_WithEmptyLockFile_ShouldCreateValidFile() throws Exception {
        Path lockPath = tempDir.resolve("empty.myra.lock");
        LockFile empty = LockFile.empty();

        LockFileManager.save(empty, lockPath);
        assertTrue(Files.exists(lockPath));

        LockFile loaded = LockFileManager.load(lockPath);
        assertNotNull(loaded);
        assertNotNull(loaded.schemaInfo);
        assertNotNull(loaded.messages);
        assertNotNull(loaded.enums);
    }
}
