package express.mvp.myra.codec.codegen;

import express.mvp.myra.codec.codegen.resolver.LockFile;
import express.mvp.myra.codec.codegen.resolver.ResolutionResult;
import express.mvp.myra.codec.codegen.resolver.SchemaResolver;
import express.mvp.myra.codec.schema.SchemaDefinition;
import java.io.File;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "myra-codec-codegen",
        mixinStandardHelpOptions = true,
        version = "MyraCodec Code Gen 1.0",
        description = "Generates Java source files from a .myra.yml schema.")
public class MyraCodegenCli implements Callable<Integer> {

    @Option(
            names = {"-s", "--schema"},
            required = true,
            description = "The path to the .myra.yml schema file.")
    private File schemaFile;

    @Option(
            names = {"-o", "--output"},
            required = true,
            description = "The output directory for the generated Java source files.")
    private File outputDir;

    @Option(
            names = {"-l", "--lockfile"},
            required = true,
            description = "The path to the .myra.lock file.")
    private File lockFile;

    @Override
    public Integer call() {
        System.out.println("Starting MYRA Codec code generation...");
        System.out.println("  Schema: " + schemaFile.getAbsolutePath());
        System.out.println("  Output Dir: " + outputDir.getAbsolutePath());
        System.out.println("  Lock File: " + lockFile.getAbsolutePath());

        try {
            // 1. Load the existing .myra.lock file (if it exists).
            System.out.println("Step 1: Loading lock file...");
            LockFile existingLockFile = LockFileManager.load(lockFile.toPath());

            // 2. Parse the .myra.yml schema file.
            System.out.println("Step 2: Parsing schema file...");
            SchemaParser parser = new SchemaParser();
            SchemaDefinition rawSchema = parser.parse(schemaFile.toPath());

                // 3. Resolve the schema, assigning stable IDs.
            System.out.println("Step 3: Resolving schema and assigning IDs...");
                // If we loaded a lockfile, fail fast with a friendly error if the namespace doesn't match
                if (existingLockFile != null && existingLockFile.schemaInfo != null) {
                Object ns = existingLockFile.schemaInfo.get("namespace");
                if (ns instanceof String && !ns.equals(rawSchema.namespace())) {
                    System.err.println(
                            "ERROR: lockfile namespace '"
                                    + ns
                                    + "' does not match schema namespace '"
                                    + rawSchema.namespace()
                                    + "' — aborting to avoid applying the lock to a different schema.\n");
                    System.err.println("Delete or regenerate the lockfile if you intentionally changed schema namespace.");
                    return 2;
                }
                }

                ResolutionResult result =
                    SchemaResolver.resolve(rawSchema, existingLockFile, schemaFile.toPath());

            // 4. Generate the Java source files.
            System.out.println("Step 4: Generating Java stubs and flyweights...");
            StubGenerator generator = new StubGenerator(result.resolvedSchema());
            generator.writeFiles(outputDir.toPath());

            // 5. Write the updated .myra.lock file.
            System.out.println("Step 5: Writing updated lock file...");
            LockFileManager.save(result.updatedLockFile(), lockFile.toPath());

        } catch (Exception e) {
            // Provide a detailed error message on failure.
            System.err.println("\nERROR: Code generation failed.");
            e.printStackTrace(System.err);
            return 1; // Failure exit code
        }

        System.out.println("\nSUCCESS: Code generation completed.");
        return 0; // Success exit code
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MyraCodegenCli()).execute(args);
        System.exit(exitCode);
    }
}
