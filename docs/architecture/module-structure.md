# Module Architecture

> Current architecture of the myra-codec module system (December 2025)

## Overview

MyraCodec is organized into distinct modules with clear separation of concerns:

```text
myra-codec/
├── schema-core/     # Schema parsing, resolution, lock file management
├── runtime/         # Lightweight runtime for generated code
├── codegen/         # CLI tool + code generation
└── benchmarks/      # JMH performance benchmarks
```

## Module Responsibilities

### schema-core

**Purpose**: Shared library for schema data structures, parsing, and resolution logic.

| Component | Responsibility |
|-----------|----------------|
| `SchemaParser` | Parse `.myra.yml` files into `SchemaDefinition` |
| `SchemaResolver` | Assign stable IDs, handle schema evolution |
| `LockFileManager` | Read/write `.myra.lock` files |
| `SchemaDefinition` | Data model for messages, fields, enums |

**Dependencies**: `jackson-dataformat-yaml`, `jackson-databind`

**Consumers**:

- `codegen` module (this repo)
- `rpc-framework` (external, for `.mvpe.yml` parsing)

### runtime

**Purpose**: Minimal runtime library that generated flyweights depend on.

| Component | Responsibility |
|-----------|----------------|
| `MessageEncoder` | Single-pass encoding with header finalization |
| `MessageHeader` | 16-byte header flyweight (frameLength, templateId, etc.) |
| `PooledSegment` | AutoCloseable wrapper for pooled MemorySegments |

**Dependencies**: `roray-ffm-utils` only

**Why separate?** Users who just need to use generated flyweights should not pull in CLI dependencies (picocli, javapoet, jackson).

> **Note**: myra-codec is **transport-agnostic**. No transport interfaces (e.g., `RpcTransport`) belong here. All transport and RPC abstractions live in `rpc-framework/transport`.

### codegen

**Purpose**: CLI tool and code generator for producing Java flyweights from schemas.

| Component | Responsibility |
|-----------|----------------|
| `MyraCodegenCli` | Command-line interface (picocli) |
| `StubGenerator` | JavaPoet-based flyweight/builder generation |

**Dependencies**: `schema-core`, `runtime`, `picocli`, `javapoet`, `roray-ffm-utils`

### benchmarks

**Purpose**: JMH benchmarks comparing MyraCodec against other serialization frameworks.

**Dependencies**: `codegen`, `runtime`, `kryo`, `avro`, `sbe`, `flatbuffers`

## Dependency Graph

```text
                    ┌──────────────┐
                    │ schema-core  │
                    │   (parsing   │
                    │  & resolve)  │
                    └──────┬───────┘
                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         │
      ┌──────────────┐                  │
      │   codegen    │                  │
      │  (CLI tool)  │                  │
      └──────┬───────┘                  │
              │                         │
              │    ┌────────────────────┘
              │    │
              ▼    ▼
      ┌──────────────┐          ┌──────────────┐
      │  benchmarks  │          │   runtime    │ ← End users depend here
      │   (JMH)      │─────────▶│ (flyweight   │
      └──────────────┘          │   helpers)   │
                                └──────┬───────┘
                                       │
                                       ▼
                                ┌──────────────┐
                                │ roray-ffm-   │
                                │    utils     │
                                └──────────────┘
```

## For End Users

When using generated flyweights in your application:

```kotlin
// build.gradle.kts
dependencies {
    // Only need the lightweight runtime
    implementation("express.mvp.myra.codec:runtime:0.1.0")
}
```

For code generation (typically a build-time dependency):

```kotlin
// build.gradle.kts
dependencies {
    // CLI for generating flyweights from schemas
    implementation("express.mvp.myra.codec:codegen:0.1.0")
}
```

## Design Rationale

### Why separate runtime from codegen?

1. **Minimal footprint**: Applications using generated code shouldn't need Jackson, picocli, or JavaPoet at runtime
2. **Faster startup**: Fewer classes to load
3. **Reduced dependency conflicts**: No transitive deps from build tools

### Why schema-core is shared?

1. **Single source of truth**: Same schema model for both data codecs and RPC services
2. **Consistent ID assignment**: Lock file format is identical across consumers
3. **Schema evolution**: Same compatibility rules everywhere

## Future: RPC Integration

The `rpc-framework` repository will add:

```text
rpc-framework/
├── rpc-schema/      # Depends on myra-codec:schema-core
│                    # Parses .mvpe.yml, generates service stubs
└── rpc-runtime/     # Depends on myra-codec:runtime
                     # Transport implementations, tracing, deadlines
```

This allows RPC services to reference data messages defined in `.myra.yml` while maintaining their own service-specific metadata in `.mvpe.yml`.
