# Contributing to Myra Codec

Thank you for your interest in contributing to Myra Codec! This document covers development setup, build commands, and contribution guidelines.

## Project Structure

```text
myra-codec/
├── schema-core/     # Schema parsing, resolution, lock file management
├── runtime/         # Lightweight runtime for generated flyweights
├── codegen/         # CLI tool + JavaPoet code generation
└── benchmarks/      # JMH performance benchmarks
```

See [docs/architecture/module-structure.md](docs/architecture/module-structure.md) for detailed module documentation.

## Development Setup

### Prerequisites

- **JDK 25+** (the project uses Java 25 toolchain)
- **Gradle 8.5+** (wrapper included)
- **Linux** recommended for full test coverage

### IDE Configuration

For IntelliJ IDEA:

1. Import as Gradle project
2. Set Project SDK to JDK 25
3. Enable annotation processing
4. Add VM options to run configurations:

```text
--enable-preview --enable-native-access=ALL-UNNAMED
```

For VS Code:

1. Install "Extension Pack for Java"
2. Configure `java.configuration.runtimes` to include JDK 25
3. Add launch configuration with VM args for preview features

---

## Gradle Commands

### Building

```bash
# Full build (compile + test + check)
./gradlew build

# Compile only (no tests)
./gradlew assemble

# Clean build artifacts
./gradlew clean

# Clean and rebuild
./gradlew clean build

# Build specific subproject
./gradlew :codegen:build
./gradlew :runtime:build
./gradlew :schema-core:build
```

### Testing

```bash
# Run all tests
./gradlew test

# Run tests with verbose output
./gradlew test --info

# Run a specific test class
./gradlew test --tests "express.mvp.myra.codec.EncoderTest"

# Run a specific test method
./gradlew test --tests "express.mvp.myra.codec.EncoderTest.testVarIntEncoding"

# Run tests matching a pattern
./gradlew test --tests "*Schema*"

# Run tests in a specific subproject
./gradlew :codegen:test
./gradlew :runtime:test
./gradlew :schema-core:test

# Run tests and show standard output
./gradlew test --info --console=plain

# Continue running tests after failures
./gradlew test --continue

# Re-run tests (ignore up-to-date checks)
./gradlew test --rerun-tasks
```

### Code Generation

```bash
# Generate codec classes from schema (if schema-core is configured)
./gradlew :schema-core:generateCodec

# Clean and regenerate
./gradlew :schema-core:clean :schema-core:generateCodec
```

### Code Quality

```bash
# Run Checkstyle
./gradlew checkstyleMain checkstyleTest

# Run SpotBugs (if configured)
./gradlew spotbugsMain

# Run all checks
./gradlew check
```

### Documentation

```bash
# Generate Javadoc
./gradlew javadoc

# Generate aggregated Javadoc (all subprojects)
./gradlew aggregateJavadoc

# Output location: build/docs/javadoc/
```

### Benchmarks

```bash
# Run JMH benchmarks
./gradlew :benchmarks:jmh

# Run specific benchmark
./gradlew :benchmarks:jmh -Pjmh.includes="EncodeBenchmark"

# Run with specific iterations
./gradlew :benchmarks:jmh -Pjmh.warmupIterations=3 -Pjmh.iterations=5

# Run with GC profiler
./gradlew :benchmarks:jmh -Pjmh.profilers="gc"

# Run with async profiler (flamegraph)
./gradlew :benchmarks:jmh -Pjmh.profilers="async:output=flamegraph"

# Compare against other codecs
./gradlew :benchmarks:jmh -Pjmh.includes="CodecComparison"

# Output location: benchmarks/build/results/jmh/
```

### Dependencies

```bash
# Show dependency tree
./gradlew dependencies

# Show dependencies for a specific configuration
./gradlew dependencies --configuration runtimeClasspath

# Check for dependency updates
./gradlew dependencyUpdates
```

### Publishing (Maintainers)

```bash
# Publish to local Maven repository
./gradlew publishToMavenLocal

# Publish to remote repository (requires credentials)
./gradlew publish
```

### Useful Combinations

```bash
# Quick validation before committing
./gradlew clean check

# Full CI build
./gradlew clean build javadoc

# Fast iteration during development
./gradlew assemble test --tests "*YourTest*"

# Regenerate and test schema changes
./gradlew :schema-core:generateCodec :schema-core:test
```

---

## Common Issues

### FFM Access Errors

If you see `java.lang.IllegalCallerException`:

```text
Add to your JVM args: --enable-native-access=ALL-UNNAMED
```

### Preview Feature Errors

If you see `preview features are not enabled`:

```text
Add to your JVM args: --enable-preview
```

### Schema Generation Failures

If code generation fails:

```bash
# Check schema syntax
./gradlew :schema-core:validateSchema

# View detailed errors
./gradlew :schema-core:generateCodec --info
```

---

## Pull Request Process

1. **Fork** the repository and create a feature branch
2. **Write tests** for new functionality
3. **Run** `./gradlew clean check` before submitting
4. **Sign** the [CLA](CLA.md) if you haven't already
5. **Submit** PR with clear description of changes

### Commit Message Format

```text
component: Short summary (50 chars or less)

Longer description if needed. Wrap at 72 characters.
Explain what and why, not how.

Fixes #123
```

### Code Style

- Follow existing code conventions
- Use meaningful variable names
- Add Javadoc for public APIs
- Keep methods focused and small

---

## Questions?

Open a [GitHub Discussion](https://github.com/mvp-express/myra-codec/discussions) for questions or ideas.
