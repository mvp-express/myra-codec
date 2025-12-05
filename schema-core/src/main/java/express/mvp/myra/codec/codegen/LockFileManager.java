package express.mvp.myra.codec.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import express.mvp.myra.codec.codegen.resolver.LockFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LockFileManager {

    private LockFileManager() {}

    public static LockFile load(Path lockFilePath) throws IOException {
        if (!Files.exists(lockFilePath)) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(lockFilePath.toFile(), LockFile.class);
    }

    public static void save(LockFile lockFile, Path lockFilePath) throws IOException {
        YAMLFactory yamlFactory =
                YAMLFactory.builder().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).build();

        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.writeValue(lockFilePath.toFile(), lockFile);
    }
}
