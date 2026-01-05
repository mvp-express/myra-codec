package express.mvp.myra.codec.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import express.mvp.myra.codec.schema.resolver.LockFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads and writes Myra schema lock files.
 *
 * <p>Lock files capture stable ids for messages and fields so schema evolution remains
 * deterministic across runs.
 */
public final class LockFileManager {

    private LockFileManager() {}

    /**
     * Loads a lock file from disk.
     *
     * @param lockFilePath the path to the .myra.lock file
     * @return the parsed lock file, or null if the file does not exist
     * @throws IOException if the file cannot be read or parsed
     */
    public static LockFile load(Path lockFilePath) throws IOException {
        if (!Files.exists(lockFilePath)) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(lockFilePath.toFile(), LockFile.class);
    }

    /**
     * Writes a lock file to disk.
     *
     * @param lockFile the lock file data to write
     * @param lockFilePath the destination path
     * @throws IOException if the file cannot be written
     */
    public static void save(LockFile lockFile, Path lockFilePath) throws IOException {
        YAMLFactory yamlFactory =
                YAMLFactory.builder().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).build();

        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.writeValue(lockFilePath.toFile(), lockFile);
    }
}
