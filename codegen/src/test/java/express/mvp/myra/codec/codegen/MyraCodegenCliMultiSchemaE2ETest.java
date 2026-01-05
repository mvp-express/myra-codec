package express.mvp.myra.codec.codegen;

import static org.junit.jupiter.api.Assertions.*;

import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.myra.codec.schema.LockFileManager;
import express.mvp.myra.codec.schema.resolver.LockFile;
import express.mvp.roray.ffm.utils.memory.MemorySegmentPool;
import express.mvp.roray.ffm.utils.memory.PooledSegment;
import express.mvp.roray.ffm.utils.memory.Utf8View;
import java.io.File;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
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

/** End-to-end codegen tests for additional schema shapes. */
class MyraCodegenCliMultiSchemaE2ETest {

    @Test
    void telemetrySchema_RoundTripsOptionalAndRepeatedFields(@TempDir Path tempDir)
            throws Exception {
        GeneratedArtifacts artifacts =
                generateArtifacts(tempDir, "telemetry.myra.yml", "Telemetry");

        MemorySegmentPool pool = new MemorySegmentPool(4096, 1, 8);
        MessageEncoder encoder = new MessageEncoder(pool);
        MemorySegment scratch = MemorySegment.ofArray(new byte[256]);
        byte[] payloadBytes = new byte[] {1, 2, 3, 4};

        try (URLClassLoader loader =
                new URLClassLoader(
                        new URL[] {artifacts.classesDir.toUri().toURL()},
                        getClass().getClassLoader())) {
            Class<?> builderClass =
                    Class.forName("com.example.telemetry.TelemetryBuilder", true, loader);
            Object builder =
                    builderClass
                            .getMethod("allocate", MessageEncoder.class, int.class)
                            .invoke(null, encoder, 2048);

            builderClass
                    .getMethod("setDeviceId", String.class, MemorySegment.class)
                    .invoke(builder, "device-01", scratch);
            builderClass.getMethod("setSequence", int.class).invoke(builder, 101);

            Class<?> enumClass = Class.forName("com.example.telemetry.Health", true, loader);
            Object warn = Enum.valueOf(enumClass.asSubclass(Enum.class), "WARN");
            builderClass.getMethod("setHealth", enumClass).invoke(builder, warn);

            builderClass
                    .getMethod("setLatencies", long[].class)
                    .invoke(builder, new Object[] {new long[] {10L, 20L}});
            builderClass
                    .getMethod("setNote", String.class, MemorySegment.class)
                    .invoke(builder, "all-good", scratch);
            builderClass
                    .getMethod("setPayload", MemorySegment.class)
                    .invoke(builder, MemorySegment.ofArray(payloadBytes));

            PooledSegment encoded =
                    (PooledSegment)
                            builderClass
                                    .getMethod("build", short.class, short.class)
                                    .invoke(builder, artifacts.templateId, artifacts.schemaVersion);

            try (PooledSegment pooled = encoded) {
                MemorySegment segment = pooled.segment();
                MessageHeader header = new MessageHeader();
                header.wrap(segment, 0);
                assertEquals(artifacts.templateId, header.getTemplateId());

                Class<?> flyweightClass =
                        Class.forName("com.example.telemetry.TelemetryFlyweight", true, loader);
                Object flyweight = flyweightClass.getConstructor().newInstance();
                flyweightClass
                        .getMethod("wrap", MemorySegment.class, long.class)
                        .invoke(flyweight, segment, (long) MessageHeader.HEADER_SIZE);

                Utf8View deviceView =
                        (Utf8View) flyweightClass.getMethod("getDeviceId").invoke(flyweight);
                assertEquals("device-01", deviceView.toString());
                int sequence = (int) flyweightClass.getMethod("getSequence").invoke(flyweight);
                assertEquals(101, sequence);

                byte healthId = (byte) flyweightClass.getMethod("getHealth").invoke(flyweight);
                Object healthEnum =
                        enumClass
                                .getMethod("fromId", int.class)
                                .invoke(null, Byte.toUnsignedInt(healthId));
                assertEquals("WARN", healthEnum.toString());

                assertEquals(2, flyweightClass.getMethod("getLatenciesCount").invoke(flyweight));
                assertEquals(
                        10L,
                        flyweightClass.getMethod("getLatenciesAt", int.class).invoke(flyweight, 0));
                assertEquals(
                        20L,
                        flyweightClass.getMethod("getLatenciesAt", int.class).invoke(flyweight, 1));

                assertTrue((boolean) flyweightClass.getMethod("hasNote").invoke(flyweight));
                Utf8View noteView =
                        (Utf8View) flyweightClass.getMethod("getNote").invoke(flyweight);
                assertEquals("all-good", noteView.toString());

                assertTrue((boolean) flyweightClass.getMethod("hasPayload").invoke(flyweight));
                Utf8View payloadView =
                        (Utf8View) flyweightClass.getMethod("getPayload").invoke(flyweight);
                byte[] decoded = new byte[(int) payloadView.byteSize()];
                MemorySegment.copy(
                        payloadView.segment(),
                        payloadView.offset(),
                        MemorySegment.ofArray(decoded),
                        0,
                        payloadView.byteSize());
                assertArrayEquals(payloadBytes, decoded);
            }
        }
    }

    @Test
    void alertsSchema_RoundTripsRepeatedEnumsAndPrimitives(@TempDir Path tempDir) throws Exception {
        GeneratedArtifacts artifacts = generateArtifacts(tempDir, "alerts.myra.yml", "AlertBatch");

        MemorySegmentPool pool = new MemorySegmentPool(2048, 1, 4);
        MessageEncoder encoder = new MessageEncoder(pool);
        MemorySegment scratch = MemorySegment.ofArray(new byte[128]);

        try (URLClassLoader loader =
                new URLClassLoader(
                        new URL[] {artifacts.classesDir.toUri().toURL()},
                        getClass().getClassLoader())) {
            Class<?> builderClass =
                    Class.forName("com.example.alerts.AlertBatchBuilder", true, loader);
            Object builder =
                    builderClass
                            .getMethod("allocate", MessageEncoder.class, int.class)
                            .invoke(null, encoder, 1024);

            builderClass
                    .getMethod("setSource", String.class, MemorySegment.class)
                    .invoke(builder, "monitoring", scratch);
            builderClass
                    .getMethod("setLevels", byte[].class)
                    .invoke(builder, new Object[] {new byte[] {0, 2}});
            builderClass
                    .getMethod("setCodes", int[].class)
                    .invoke(builder, new Object[] {new int[] {100, 200, 300}});

            PooledSegment encoded =
                    (PooledSegment)
                            builderClass
                                    .getMethod("build", short.class, short.class)
                                    .invoke(builder, artifacts.templateId, artifacts.schemaVersion);

            try (PooledSegment pooled = encoded) {
                MemorySegment segment = pooled.segment();

                Class<?> flyweightClass =
                        Class.forName("com.example.alerts.AlertBatchFlyweight", true, loader);
                Object flyweight = flyweightClass.getConstructor().newInstance();
                flyweightClass
                        .getMethod("wrap", MemorySegment.class, long.class)
                        .invoke(flyweight, segment, (long) MessageHeader.HEADER_SIZE);

                Utf8View sourceView =
                        (Utf8View) flyweightClass.getMethod("getSource").invoke(flyweight);
                assertEquals("monitoring", sourceView.toString());

                assertEquals(2, flyweightClass.getMethod("getLevelsCount").invoke(flyweight));
                byte level0 =
                        (byte)
                                flyweightClass
                                        .getMethod("getLevelsAt", int.class)
                                        .invoke(flyweight, 0);
                assertEquals(0, Byte.toUnsignedInt(level0));
                Class<?> levelEnum = Class.forName("com.example.alerts.Level", true, loader);
                Object level =
                        flyweightClass.getMethod("getLevelsEnumAt", int.class).invoke(flyweight, 1);
                assertEquals("CRITICAL", level.toString());
                Object fromId = levelEnum.getMethod("fromId", int.class).invoke(null, 2);
                assertEquals(fromId.toString(), level.toString());

                assertEquals(3, flyweightClass.getMethod("getCodesCount").invoke(flyweight));
                assertEquals(
                        200,
                        flyweightClass.getMethod("getCodesAt", int.class).invoke(flyweight, 1));
            }
        }
    }

    @Test
    void portfolioSchema_RoundTripsNestedAndVariableRepeats(@TempDir Path tempDir)
            throws Exception {
        GeneratedArtifacts artifacts =
                generateArtifacts(tempDir, "portfolio.myra.yml", "Portfolio");

        MemorySegmentPool pool = new MemorySegmentPool(4096, 1, 8);
        MessageEncoder encoder = new MessageEncoder(pool);
        MemorySegment scratch = MemorySegment.ofArray(new byte[256]);

        try (URLClassLoader loader =
                new URLClassLoader(
                        new URL[] {artifacts.classesDir.toUri().toURL()},
                        getClass().getClassLoader())) {
            Class<?> builderClass =
                    Class.forName("com.example.portfolio.PortfolioBuilder", true, loader);
            Object builder =
                    builderClass
                            .getMethod("allocate", MessageEncoder.class, int.class)
                            .invoke(null, encoder, 2048);

            builderClass
                    .getMethod("setAccountId", String.class, MemorySegment.class)
                    .invoke(builder, "acct-77", scratch);

            AtomicInteger index = new AtomicInteger();
            Class<?> sideEnum = Class.forName("com.example.portfolio.Side", true, loader);
            Consumer<Object> legWriter =
                    legBuilder -> {
                        int i = index.getAndIncrement();
                        String symbol = i == 0 ? "AAPL" : "MSFT";
                        Object side =
                                Enum.valueOf(
                                        sideEnum.asSubclass(Enum.class), i == 0 ? "BUY" : "SELL");
                        int qty = i == 0 ? 10 : 20;
                        try {
                            legBuilder
                                    .getClass()
                                    .getMethod("setSymbol", String.class, MemorySegment.class)
                                    .invoke(legBuilder, symbol, scratch);
                            legBuilder
                                    .getClass()
                                    .getMethod("setSide", sideEnum)
                                    .invoke(legBuilder, side);
                            legBuilder
                                    .getClass()
                                    .getMethod("setQuantity", int.class)
                                    .invoke(legBuilder, qty);
                        } catch (ReflectiveOperationException ex) {
                            throw new IllegalStateException(ex);
                        }
                    };

            builderClass
                    .getMethod("setLegs", int.class, Consumer.class)
                    .invoke(builder, 2, legWriter);

            builderClass
                    .getMethod("setTags", String[].class, MemorySegment.class)
                    .invoke(builder, new Object[] {new String[] {"core", "long-term"}, scratch});
            builderClass
                    .getMethod("setAttachments", byte[][].class)
                    .invoke(
                            builder,
                            new Object[] {new byte[][] {new byte[] {1, 2}, new byte[] {3, 4, 5}}});
            builderClass
                    .getMethod("setComment", String.class, MemorySegment.class)
                    .invoke(builder, "review", scratch);

            PooledSegment encoded =
                    (PooledSegment)
                            builderClass
                                    .getMethod("build", short.class, short.class)
                                    .invoke(builder, artifacts.templateId, artifacts.schemaVersion);

            try (PooledSegment pooled = encoded) {
                MemorySegment segment = pooled.segment();

                Class<?> flyweightClass =
                        Class.forName("com.example.portfolio.PortfolioFlyweight", true, loader);
                Object flyweight = flyweightClass.getConstructor().newInstance();
                flyweightClass
                        .getMethod("wrap", MemorySegment.class, long.class)
                        .invoke(flyweight, segment, (long) MessageHeader.HEADER_SIZE);

                Utf8View accountView =
                        (Utf8View) flyweightClass.getMethod("getAccountId").invoke(flyweight);
                assertEquals("acct-77", accountView.toString());

                assertEquals(2, flyweightClass.getMethod("getLegsCount").invoke(flyweight));
                Object leg0 = flyweightClass.getMethod("getLegsAt", int.class).invoke(flyweight, 0);
                Utf8View symbol0 = (Utf8View) leg0.getClass().getMethod("getSymbol").invoke(leg0);
                assertEquals("AAPL", symbol0.toString());

                byte side0 = (byte) leg0.getClass().getMethod("getSide").invoke(leg0);
                Object sideValue0 =
                        sideEnum.getMethod("fromId", int.class)
                                .invoke(null, Byte.toUnsignedInt(side0));
                assertEquals("BUY", sideValue0.toString());

                Object leg1 = flyweightClass.getMethod("getLegsAt", int.class).invoke(flyweight, 1);
                Utf8View symbol1 = (Utf8View) leg1.getClass().getMethod("getSymbol").invoke(leg1);
                assertEquals("MSFT", symbol1.toString());

                assertEquals(2, flyweightClass.getMethod("getTagsCount").invoke(flyweight));
                Utf8View tagView = new Utf8View();
                flyweightClass
                        .getMethod("getTagsAt", int.class, Utf8View.class)
                        .invoke(flyweight, 0, tagView);
                assertEquals("core", tagView.toString());
                flyweightClass
                        .getMethod("getTagsAt", int.class, Utf8View.class)
                        .invoke(flyweight, 1, tagView);
                assertEquals("long-term", tagView.toString());

                assertEquals(2, flyweightClass.getMethod("getAttachmentsCount").invoke(flyweight));
                MemorySegment attachment =
                        (MemorySegment)
                                flyweightClass
                                        .getMethod("getAttachmentsAt", int.class)
                                        .invoke(flyweight, 1);
                byte[] attachmentBytes = new byte[(int) attachment.byteSize()];
                MemorySegment.copy(
                        attachment,
                        0,
                        MemorySegment.ofArray(attachmentBytes),
                        0,
                        attachment.byteSize());
                assertArrayEquals(new byte[] {3, 4, 5}, attachmentBytes);

                assertTrue((boolean) flyweightClass.getMethod("hasComment").invoke(flyweight));
                Utf8View commentView =
                        (Utf8View) flyweightClass.getMethod("getComment").invoke(flyweight);
                assertEquals("review", commentView.toString());
            }
        }
    }

    private static GeneratedArtifacts generateArtifacts(
            Path tempDir, String schemaFile, String messageName) throws IOException {
        Path schemaPath = Path.of("src", "test", "resources", schemaFile).toAbsolutePath();
        Path generatedSources = Files.createDirectories(tempDir.resolve("generated-src"));
        Path lockFilePath = tempDir.resolve(schemaFile.replace(".myra.yml", ".lock"));

        int exitCode =
                new CommandLine(new MyraCodegenCli())
                        .execute(
                                "-s", schemaPath.toString(),
                                "-o", generatedSources.toString(),
                                "-l", lockFilePath.toString());
        assertEquals(0, exitCode, "Myra codegen CLI must succeed");

        LockFile lockFile = LockFileManager.load(lockFilePath);
        assertNotNull(lockFile, "CLI run should emit a lock file");

        Path classesDir = compileGeneratedSources(generatedSources);
        short templateId = templateId(lockFile, messageName);
        short schemaVersion = schemaVersion(lockFile);

        return new GeneratedArtifacts(classesDir, templateId, schemaVersion);
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
                                .map(MyraCodegenCliMultiSchemaE2ETest::formatDiagnostic)
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

    private static final class GeneratedArtifacts {
        private final Path classesDir;
        private final short templateId;
        private final short schemaVersion;

        private GeneratedArtifacts(Path classesDir, short templateId, short schemaVersion) {
            this.classesDir = classesDir;
            this.templateId = templateId;
            this.schemaVersion = schemaVersion;
        }
    }
}
