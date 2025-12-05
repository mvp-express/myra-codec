package express.mvp.myra.codec.codegen;

import static org.junit.jupiter.api.Assertions.*;

import express.mvp.myra.codec.codegen.resolver.LockFile;
import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.myra.codec.runtime.PooledSegment;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.roray.utils.memory.MemorySegmentPool;
import express.mvp.roray.utils.memory.Utf8View;
import java.io.File;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class MyraCodegenCliRoundTripTest {

    @Test
    void cliGeneratesBuildersThatRoundTripPayloads(@TempDir Path tempDir) throws Exception {
        Path schemaPath = Path.of("src", "test", "resources", "kvstore.myra.yml").toAbsolutePath();
        Path generatedSources = Files.createDirectories(tempDir.resolve("generated-src"));
        Path lockFilePath = tempDir.resolve("kvstore.generated.lock");

        int exitCode =
                new CommandLine(new MyraCodegenCli())
                        .execute(
                                "-s", schemaPath.toString(),
                                "-o", generatedSources.toString(),
                                "-l", lockFilePath.toString());
        assertEquals(0, exitCode, "Myra codegen CLI must succeed");

        LockFile lockFile = LockFileManager.load(lockFilePath);
        assertNotNull(lockFile, "CLI run should emit a lock file");

        Path compiledOutput = compileGeneratedSources(generatedSources);
        short templateId = templateId(lockFile, "PutRequest");
        short schemaVersion = schemaVersion(lockFile);

        MemorySegmentPool pool = new MemorySegmentPool(4096, 1, 8);
        MessageEncoder encoder = new MessageEncoder(pool);

        byte[] valueBytes = "payload-binary".getBytes(StandardCharsets.UTF_8);
        MemorySegment scratchBuffer = MemorySegment.ofArray(new byte[256]);

        try (URLClassLoader loader =
                new URLClassLoader(
                        new URL[] {compiledOutput.toUri().toURL()}, getClass().getClassLoader())) {
            Class<?> builderClass =
                    Class.forName("com.example.kvstore.codec.PutRequestBuilder", true, loader);
            Object builder =
                    builderClass
                            .getMethod("allocate", MessageEncoder.class, int.class)
                            .invoke(null, encoder, 2048);

            builderClass
                    .getMethod("setKey", String.class, MemorySegment.class)
                    .invoke(builder, "orders#1", scratchBuffer);
            builderClass
                    .getMethod("setValue", MemorySegment.class)
                    .invoke(builder, MemorySegment.ofArray(valueBytes));

            PooledSegment encoded =
                    (PooledSegment)
                            builderClass
                                    .getMethod("build", short.class, short.class)
                                    .invoke(builder, templateId, schemaVersion);

            try (PooledSegment pooled = encoded) {
                MemorySegment encodedSegment = pooled.segment();
                MessageHeader header = new MessageHeader();
                header.wrap(encodedSegment, 0);
                assertEquals(
                        templateId,
                        header.getTemplateId(),
                        "Template id should match lock file assignment");
                assertEquals(
                        schemaVersion,
                        header.getSchemaVersion(),
                        "Schema version should be preserved");
                assertTrue(
                        header.getFrameLength() > MessageHeader.HEADER_SIZE,
                        "Frame length should include payload");
                assertNotEquals(0, header.getChecksum(), "Checksum must be populated");

                Class<?> flyweightClass =
                        Class.forName(
                                "com.example.kvstore.codec.PutRequestFlyweight", true, loader);
                Object flyweight = flyweightClass.getConstructor().newInstance();
                flyweightClass
                        .getMethod("wrap", MemorySegment.class, long.class)
                        .invoke(flyweight, encodedSegment, (long) MessageHeader.HEADER_SIZE);

                Utf8View keyView = (Utf8View) flyweightClass.getMethod("getKey").invoke(flyweight);
                Utf8View valueView =
                        (Utf8View) flyweightClass.getMethod("getValue").invoke(flyweight);

                assertEquals(
                        "orders#1",
                        keyView.toString(),
                        "Key field should round-trip via flyweight view");
                String expectedValue = new String(valueBytes, StandardCharsets.UTF_8);
                assertEquals(
                        expectedValue,
                        valueView.toString(),
                        "Value bytes should match original payload");
            }
        }
    }

    private static Path compileGeneratedSources(Path outputDir) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Tests must run on a JDK to recompile generated sources");

        List<File> sources;
        try (Stream<Path> paths = Files.walk(outputDir)) {
            sources = paths.filter(p -> p.toString().endsWith(".java")).map(Path::toFile).toList();
        }
        assertFalse(sources.isEmpty(), "CLI should emit Java sources to compile");

        Path classesDir = Files.createDirectories(outputDir.resolveSibling("generated-classes"));

        try (StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(null, null, null)) {
            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromFiles(sources);
            String classpath = System.getProperty("java.class.path");
            assertNotNull(classpath, "Test JVM must expose a classpath");

            List<String> options = List.of("-classpath", classpath, "-d", classesDir.toString());
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            JavaCompiler.CompilationTask task =
                    compiler.getTask(
                            null, fileManager, diagnostics, options, null, compilationUnits);
            boolean success = task.call();
            if (!success) {
                String diagnosticText =
                        diagnostics.getDiagnostics().stream()
                                .map(d -> formatDiagnostic(d))
                                .collect(Collectors.joining(System.lineSeparator()));
                fail(
                        "Generated sources should compile cleanly but failed with:\n"
                                + diagnosticText);
            }
        }

        return classesDir;
    }

    private static String formatDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic) {
        return diagnostic.getKind()
                + " at "
                + diagnostic.getSource()
                + ":"
                + diagnostic.getLineNumber()
                + " - "
                + diagnostic.getMessage(Locale.getDefault());
    }

    private static short templateId(LockFile lockFile, String messageName) {
        LockFile.MessageLock messageLock = lockFile.messages.get(messageName);
        assertNotNull(messageLock, "Lock file missing template for " + messageName);
        return (short) messageLock.id;
    }

    private static short schemaVersion(LockFile lockFile) {
        Object version = lockFile.schemaInfo.get("version");
        if (version instanceof Number) {
            return ((Number) version).shortValue();
        }
        return 1;
    }
}
