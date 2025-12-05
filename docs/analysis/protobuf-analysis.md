# Protocol Buffers Support for MyraCodec - Feasibility Analysis

**Date:** November 2, 2025  
**Author:** Technical Analysis  
**Status:** Design Proposal

---

## Executive Summary

**Feasibility:** ✅ **YES** - Adding Protocol Buffers (protobuf) support to MyraCodec is feasible and can be implemented with moderate complexity.

**Estimated Complexity:** **Medium** (2-3 weeks of development)

**Recommended Approach:** Implement a `ProtobufToYamlConverter` that translates `.proto` files into `.myra.yml` format first, then use the existing YAML parser. This approach preserves lock file semantics, enables better debugging, and maintains clean separation of concerns.

---

## Table of Contents

1. [Current Architecture Analysis](#current-architecture-analysis)
2. [Implementation Strategy](#implementation-strategy)
3. [Complexity Assessment](#complexity-assessment)
4. [Technical Challenges](#technical-challenges)
5. [Feature Mapping: Protobuf ↔ Myra](#feature-mapping-protobuf--myra)
6. [Limitations Comparison](#limitations-comparison)
7. [Implementation Roadmap](#implementation-roadmap)
8. [Recommendations](#recommendations)

---

## Current Architecture Analysis

### Schema Processing Pipeline

MyraCodec's current architecture is well-suited for adding alternative schema formats:

```
┌─────────────────┐
│  .myra.yml      │
│  Schema File    │
└────────┬────────┘
         │
         ▼
┌─────────────────────┐
│  SchemaParser       │  ◄── Currently YAML-specific
│  (SnakeYAML)        │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  SchemaDefinition   │  ◄── Format-agnostic AST
│  (Internal AST)     │      (namespace, messages, enums, service)
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  SchemaResolver     │  ◄── ID assignment & evolution logic
│  (Lock file mgmt)   │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  StubGenerator      │  ◄── Code generation
│  (JavaPoet)         │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  Generated Java     │
│  Source Files       │
└─────────────────────┘
```

### Proposed Pipeline with Protobuf Support

```
┌─────────────────┐
│  .proto file    │  ◄── NEW: Protobuf input
└────────┬────────┘
         │
         ▼
┌──────────────────────┐
│ ProtobufToYaml       │  ◄── NEW: Converter
│ Converter            │
└────────┬─────────────┘
         │
         ▼
┌─────────────────┐
│  .myra.yml      │  ◄── Intermediate artifact (persistent or temp)
│  Schema File    │
└────────┬────────┘
         │
         ▼
┌─────────────────────┐
│  SchemaParser       │  ◄── Existing code (UNCHANGED)
│  (SnakeYAML)        │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  SchemaDefinition   │  ◄── Existing code (UNCHANGED)
│  (Internal AST)     │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  SchemaResolver     │  ◄── Existing code (UNCHANGED)
│  (Lock file mgmt)   │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  StubGenerator      │  ◄── Existing code (UNCHANGED)
│  (JavaPoet)         │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  Generated Java     │
│  Source Files       │
└─────────────────────┘
```

### Key Insight

Converting protobuf to `.myra.yml` **first**, then using the existing pipeline, provides:
- ✅ **Lock file compatibility**: Lock file references YAML as designed
- ✅ **Zero risk to existing code**: YAML parser and resolver unchanged
- ✅ **Better debugging**: Can inspect intermediate YAML
- ✅ **Clean architecture**: Separation of concerns
- ✅ **Version control friendly**: Generated YAML can be committed

### Current Schema Parser (lib/src/main/java/.../codegen/SchemaParser.java)

```java
public class SchemaParser {
    public SchemaDefinition parse(Path schemaPath) throws Exception {
        // Currently hardcoded to YAML
        LoaderOptions loaderOptions = new LoaderOptions();
        Constructor constructor = new Constructor(SchemaDefinition.class, loaderOptions);
        Yaml yaml = new Yaml(constructor);
        
        try (InputStream in = Files.newInputStream(schemaPath)) {
            return yaml.load(in);
        }
    }
}
```

---

## Implementation Strategy

### Recommended Approach: Protobuf → YAML Conversion

Instead of parsing protobuf directly to `SchemaDefinition`, convert `.proto` files to `.myra.yml` format first, then use the existing pipeline.

**Architecture:**

```java
// NEW: Protobuf to YAML converter
public class ProtobufToYamlConverter {
    
    /**
     * Converts a .proto file to .myra.yml format.
     * 
     * @param protoFile Path to the .proto file
     * @param outputYamlFile Path for the generated .myra.yml file
     * @return Path to the generated YAML file
     */
    public Path convert(Path protoFile, Path outputYamlFile) throws Exception {
        // 1. Compile proto to descriptor using protoc
        Path descriptorFile = compileProtoFile(protoFile);
        
        // 2. Parse the descriptor
        FileDescriptorSet descriptorSet = FileDescriptorSet.parseFrom(
            Files.readAllBytes(descriptorFile)
        );
        
        // 3. Convert to YAML structure (Map/List)
        Map<String, Object> yamlSchema = convertToYamlStructure(
            descriptorSet.getFile(0)
        );
        
        // 4. Write YAML file using SnakeYAML
        writeYaml(yamlSchema, outputYamlFile);
        
        return outputYamlFile;
    }
    
    private Map<String, Object> convertToYamlStructure(FileDescriptorProto proto) {
        // Convert proto descriptor to nested Map/List structure
        // that matches .myra.yml format
    }
}
```

### Modified CLI (MyraCodegenCli.java)

```java
@Override
public Integer call() {
    Path actualSchemaFile = schemaFile.toPath();
    
    // Auto-detect and convert .proto files
    if (schemaFile.getName().endsWith(".proto")) {
        System.out.println("Detected protobuf schema, converting to Myra YAML...");
        
        ProtobufToYamlConverter converter = new ProtobufToYamlConverter();
        
        // Generate YAML alongside the proto file
        Path yamlFile = actualSchemaFile.resolveSibling(
            schemaFile.getName().replace(".proto", ".myra.yml")
        );
        
        actualSchemaFile = converter.convert(actualSchemaFile, yamlFile);
        System.out.println("  Generated: " + yamlFile);
    }
    
    // Rest of pipeline unchanged
    LockFile existingLockFile = LockFileManager.load(lockFile.toPath());
    SchemaParser parser = new SchemaParser(); // Existing YAML parser
    SchemaDefinition rawSchema = parser.parse(actualSchemaFile);
    
    // ... rest remains the same
}
```

### Advantages of This Approach

1. **🎯 Lock File Semantics Preserved**
   - Lock file always references a `.myra.yml` file
   - No special cases needed in `SchemaResolver`
   - Schema evolution works as designed

2. **✅ Zero Risk to Existing Code**
   - YAML parser: unchanged
   - Schema resolver: unchanged
   - Code generator: unchanged
   - Only CLI gets minimal modification

3. **✅ Better Debugging**
   - Can inspect the generated `.myra.yml`
   - Easy to verify conversion correctness
   - Can manually tweak YAML if needed

4. **✅ Clean Architecture**
   - Converter is completely independent module
   - Could be separate CLI tool: `myra-proto2yaml`
   - Single Responsibility Principle

5. **✅ Version Control Friendly**
   - Generated `.myra.yml` can be committed
   - Team members without `protoc` can still build
   - Clear audit trail of conversions

6. **✅ Gradual Migration Path**
   - Convert `.proto` → `.myra.yml` once
   - Treat as normal Myra schema going forward
   - Eventually migrate away from protobuf entirely

---

## Complexity Assessment

### Low Complexity Components ✅

1. **Interface Extraction** - Simple refactoring
2. **Factory Pattern** - Straightforward implementation
3. **CLI Integration** - One-line change

### Medium Complexity Components ⚠️

1. **Protobuf Parsing** - Need to use protobuf compiler API
2. **Type Mapping** - Map protobuf types to MyraCodec types
3. **Service Translation** - Convert protobuf services to MyraCodec RPCs

### High Complexity Components ❌

1. **Advanced Protobuf Features** - Some features won't map cleanly
2. **Import Resolution** - Handling protobuf imports across files
3. **Custom Options** - Protobuf custom options need special handling

### Overall Complexity: **MEDIUM** (Reduced from original estimate)

**Estimated Implementation Time:** 2-3 weeks (reduced due to code reuse)

**Lines of Code:** ~600-800 lines (mostly ProtobufToYamlConverter)

**Complexity Reduction Factors:**
- No need to modify existing parser
- No need to change resolver logic
- Simpler converter vs. full parser
- Reuses existing YAML infrastructure

---

## Technical Challenges

### Challenge 1: Protobuf Compiler Integration

**Problem:** Need to parse `.proto` files programmatically.

**Solution Options:**

**Option A: Use protoc programmatically**
```java
// Use protoc to generate FileDescriptorSet
ProcessBuilder pb = new ProcessBuilder(
    "protoc",
    "--descriptor_set_out=" + descriptorFile,
    "--include_imports",
    protoFile.toString()
);
pb.start().waitFor();

// Parse the descriptor set
FileDescriptorSet fds = FileDescriptorSet.parseFrom(
    Files.newInputStream(descriptorFile)
);
```

**Option B: Use protobuf-java library with reflection**
```java
// Requires protobuf-java dependency
import com.google.protobuf.DescriptorProtos.*;
import com.google.protobuf.Descriptors.*;

// Parse using Protobuf Java API
FileDescriptor descriptor = FileDescriptor.buildFrom(
    FileDescriptorProto.parseFrom(protoBytes),
    dependencies
);
```

**Recommendation:** **Option A** - More reliable, no version conflicts

**Dependency Required:**
```kotlin
dependencies {
    implementation("com.google.protobuf:protobuf-java:3.25.1")
}
```

### Challenge 2: Type System Mapping

Protobuf and MyraCodec have different type systems that need translation:

| Protobuf Type | MyraCodec Equivalent | Notes |
|---------------|---------------------|-------|
| `bool` | `bool` | ✅ Direct mapping |
| `int32` | `int32` | ✅ Direct mapping |
| `int64` | `int64` | ✅ Direct mapping |
| `uint32` | ❌ No equivalent | **Challenge** |
| `uint64` | ❌ No equivalent | **Challenge** |
| `sint32` | `int32` | ⚠️ Different encoding |
| `sint64` | `int64` | ⚠️ Different encoding |
| `fixed32` | `int32` | ⚠️ Different encoding |
| `fixed64` | `int64` | ⚠️ Different encoding |
| `sfixed32` | `int32` | ✅ Compatible |
| `sfixed64` | `int64` | ✅ Compatible |
| `float` | `float32` | ✅ Direct mapping |
| `double` | `float64` | ✅ Direct mapping |
| `string` | `string` | ✅ Direct mapping |
| `bytes` | `bytes` | ✅ Direct mapping |
| `enum` | `enum` | ✅ Direct mapping |
| `message` | `message` | ✅ Direct mapping |
| `repeated` | `repeated: true` | ✅ Direct mapping |
| `map<K,V>` | ❌ Not yet supported | **Challenge** |
| `oneof` | ❌ Not yet supported | **Challenge** |
| `Any` | ❌ No equivalent | **Limitation** |
| `Timestamp` | ❌ No equivalent | Can map to `int64` |
| `Duration` | ❌ No equivalent | Can map to `int64` |

**Implementation Example:**

```java
private String mapProtobufType(FieldDescriptor field) {
    return switch (field.getType()) {
        case BOOL -> "bool";
        case INT32, SINT32, SFIXED32 -> "int32";
        case INT64, SINT64, SFIXED64 -> "int64";
        case FLOAT -> "float32";
        case DOUBLE -> "float64";
        case STRING -> "string";
        case BYTES -> "bytes";
        case ENUM -> field.getEnumType().getName();
        case MESSAGE -> field.getMessageType().getName();
        case UINT32, FIXED32 -> {
            System.err.println("WARNING: Unsigned int32 not supported, using int32");
            yield "int32";
        }
        case UINT64, FIXED64 -> {
            System.err.println("WARNING: Unsigned int64 not supported, using int64");
            yield "int64";
        }
        default -> throw new UnsupportedOperationException(
            "Type not supported: " + field.getType()
        );
    };
}
```

### Challenge 3: Service Definition Translation

Protobuf services map naturally to MyraCodec services:

**Protobuf:**
```protobuf
service KeyValueStore {
  rpc Put(PutRequest) returns (PutResponse);
  rpc Get(GetRequest) returns (GetResponse);
}
```

**MyraCodec Equivalent:**
```yaml
service:
  name: "KeyValueStore"
  rpcs:
    - name: "put"
      request: "PutRequest"
      response: "PutResponse"
    - name: "get"
      request: "GetRequest"
      response: "GetResponse"
```

**Translation Logic:**
```java
private ServiceDefinition convertService(ServiceDescriptor protoService) {
    List<RpcDefinition> rpcs = protoService.getMethods().stream()
        .map(this::convertMethod)
        .collect(Collectors.toList());
    
    ServiceDefinition service = new ServiceDefinition();
    service.setName(protoService.getName());
    service.setDescription(extractComments(protoService));
    service.setRpcs(rpcs);
    return service;
}

private RpcDefinition convertMethod(MethodDescriptor method) {
    RpcDefinition rpc = new RpcDefinition();
    rpc.setName(toCamelCase(method.getName()));
    rpc.setRequest(method.getInputType().getName());
    rpc.setResponse(method.getOutputType().getName());
    rpc.setDescription(extractComments(method));
    return rpc;
}
```

### Challenge 4: Field Number Handling

Protobuf uses explicit field numbers; MyraCodec uses sequential IDs via lock file.

**Protobuf:**
```protobuf
message User {
  string name = 1;
  int32 age = 2;
  string email = 5;  // Non-sequential OK
}
```

**Solution with YAML Conversion:**

When converting to YAML, we simply preserve the field order:

```yaml
messages:
  - name: "User"
    fields:
      - name: "name"
        type: "string"
      - name: "age"
        type: "int32"
      - name: "email"
        type: "string"
```

The existing lock file mechanism handles ID assignment:
- First run: assigns IDs 1, 2, 3 to fields in order
- Lock file stores these IDs
- Subsequent runs: IDs remain stable

**Note:** Protobuf field numbers are irrelevant after conversion. The YAML field order determines MyraCodec IDs. This is acceptable because:
- We're using MyraCodec's wire format, not protobuf's
- Lock file ensures stability
- If protobuf compatibility is needed, use protobuf codegen for other languages

### Challenge 5: Package/Namespace Translation

**Protobuf:**
```protobuf
package com.example.myapp;
option java_package = "com.example.myapp.generated";
```

**Translation:**
```java
private String extractNamespace(FileDescriptor file) {
    // Prefer java_package option if set
    if (file.getOptions().hasJavaPackage()) {
        return file.getOptions().getJavaPackage();
    }
    // Fall back to package declaration
    return file.getPackage();
}
```

### Challenge 6: Import Handling

Protobuf allows importing other `.proto` files:

```protobuf
import "common.proto";
import "google/protobuf/timestamp.proto";
```

**Challenge:** Need to resolve and parse dependencies.

**Solution:** Use protoc with `--include_imports` flag:

```java
ProcessBuilder pb = new ProcessBuilder(
    "protoc",
    "--descriptor_set_out=" + descriptorFile,
    "--include_imports",  // Include all dependencies
    "--proto_path=" + protoPath,
    protoFile.toString()
);
```

---

## Feature Mapping: Protobuf ↔ Myra

### ✅ Fully Compatible Features

| Feature | Protobuf | Myra | Notes |
|---------|----------|------|-------|
| Basic types | `int32`, `int64`, `bool`, `string` | `int32`, `int64`, `bool`, `string` | Perfect match |
| Floating point | `float`, `double` | `float32`, `float64` | Compatible |
| Bytes | `bytes` | `bytes` | Compatible |
| Enums | `enum Status { OK = 0; }` | `enums: - name: Status` | Compatible |
| Messages | `message User { }` | `messages: - name: User` | Compatible |
| Nested messages | `message Outer { message Inner {} }` | Flatten to `OuterInner` | Manageable |
| Repeated fields | `repeated string tags = 1;` | `repeated: true` | Compatible |
| Optional fields | `optional string name = 1;` | `optional: true` | Compatible (proto3) |
| Services | `service MyService` | `service: name: MyService` | Compatible |
| RPC methods | `rpc MyMethod(Req) returns (Res)` | `rpcs: - name: myMethod` | Compatible |
| Comments | `// Comment` | Preserve in description | Can extract |

### ⚠️ Partially Compatible Features

| Feature | Protobuf | Myra | Solution |
|---------|----------|------|----------|
| Unsigned ints | `uint32`, `uint64` | Not supported | Map to `int32`/`int64` with warning |
| Fixed-size ints | `fixed32`, `fixed64` | Not supported | Map to `int32`/`int64` |
| Signed variants | `sint32`, `sint64` | `int32`, `int64` | Encoding differs (VarInt vs ZigZag) |
| Well-known types | `google.protobuf.Timestamp` | Not supported | Map to `int64` (nanos since epoch) |
| Reserved fields | `reserved 2, 15, 9 to 11;` | Lock file reserves | Can preserve in lock file |
| Deprecated | `string old = 1 [deprecated=true];` | `deprecated: true` | Compatible |

### ❌ Incompatible Features (Not Yet Supported in Myra)

| Feature | Protobuf | Myra Status | Workaround |
|---------|----------|-------------|------------|
| `oneof` | `oneof payment { ... }` | Mentioned in whitepaper, not implemented | **Cannot translate** - Skip with warning |
| `map` | `map<string, int32> data = 1;` | Mentioned in whitepaper, not implemented | **Cannot translate** - Skip with warning |
| Extensions | `extend Foo { ... }` | Not supported | **Cannot translate** |
| `Any` type | `google.protobuf.Any` | Not supported | **Cannot translate** |
| Streaming RPCs | `stream Request` | Not supported | Only unary RPC supported |
| Custom options | `option (my_option) = ...` | Not supported | Ignore with warning |
| Groups | `group Result = 1 { ... }` | Not supported (deprecated) | Error |

### 📋 Missing Myra Features Not in Protobuf

| Feature | Myra | Protobuf | Notes |
|---------|------|----------|-------|
| `int8`, `int16` | Explicit small int types | Uses varint encoding | Myra is more explicit |
| Repeating groups | SBE-style groups | Not applicable | Myra-specific optimization |
| Rich types | `uuid`, `timestamp_nanos` | Separate well-known types | Different approach |

---

## Limitations Comparison

### Myra Schema (.myra.yml) Advantages

1. **✅ Explicit Type Sizes**
   - Myra: `int8`, `int16`, `int32`, `int64` - exact wire format
   - Protobuf: Only `int32`, `int64` with variable encoding
   - **Winner:** Myra (more predictable performance)

2. **✅ Simpler Syntax**
   - Myra: Clean YAML, human-readable
   - Protobuf: More verbose, requires learning proto syntax
   - **Winner:** Myra (easier for beginners)

3. **✅ Modern Design**
   - Myra: Built for Java 24+, FFM API, no legacy baggage
   - Protobuf: Cross-language, carries compatibility overhead
   - **Winner:** Myra (optimized for modern Java)

4. **✅ Transparent Evolution**
   - Myra: `.myra.lock` file is human-readable YAML
   - Protobuf: Field numbers in source, less explicit tracking
   - **Winner:** Myra (better visibility)

5. **✅ Zero-GC Focus**
   - Myra: Flyweights, off-heap from ground up
   - Protobuf: Generated code creates objects
   - **Winner:** Myra (better for low-latency)

### Protobuf Advantages

1. **✅ Industry Standard**
   - Protobuf: Used by Google, gRPC, thousands of projects
   - Myra: New, niche
   - **Winner:** Protobuf (ecosystem, tooling, familiarity)

2. **✅ Multi-Language Support**
   - Protobuf: 10+ languages (Java, C++, Python, Go, etc.)
   - Myra: Java only
   - **Winner:** Protobuf (polyglot systems)

3. **✅ Mature Tooling**
   - Protobuf: IDE plugins, linters, validators, formatters
   - Myra: Limited tooling
   - **Winner:** Protobuf (developer experience)

4. **✅ Well-Known Types**
   - Protobuf: `Timestamp`, `Duration`, `Any`, `Empty`
   - Myra: Custom implementations needed
   - **Winner:** Protobuf (standardization)

5. **✅ Backward Compatibility Guarantees**
   - Protobuf: Field numbers + strict compatibility rules
   - Myra: Lock file (newer, less battle-tested)
   - **Winner:** Protobuf (proven in production)

6. **✅ Existing Schemas**
   - Protobuf: Can leverage existing `.proto` files
   - Myra: Must write new schemas
   - **Winner:** Protobuf (migration path)

### Feature Comparison Summary

| Feature | Myra | Protobuf | Winner |
|---------|------|----------|--------|
| **Wire Format** | Custom binary | Protobuf binary | Tie (different goals) |
| **Type System** | Explicit sizes (`int8`, `int16`) | VarInt encoding | **Myra** (predictable) |
| **Readability** | YAML (simple) | Proto syntax | **Myra** (easier) |
| **Multi-Language** | Java only | 10+ languages | **Protobuf** |
| **Tooling** | Limited | Extensive | **Protobuf** |
| **Zero-GC** | Core design | Not a focus | **Myra** |
| **Ecosystem** | New | Massive | **Protobuf** |
| **Evolution** | Lock file | Field numbers | **Tie** (different approaches) |
| **Learning Curve** | Lower | Higher | **Myra** |
| **Production Maturity** | New | Battle-tested | **Protobuf** |
| **gRPC Integration** | Not supported | Native | **Protobuf** |
| **Advanced Features** | `oneof`, `map` not yet impl | Full support | **Protobuf** |

### When to Use Myra

- ✅ Pure Java microservices
- ✅ Ultra-low latency requirements
- ✅ Zero-GC mandate
- ✅ Modern JDK features (FFM API)
- ✅ Prefer YAML over proto syntax
- ✅ Greenfield projects

### When to Use Protobuf (via Myra Parser)

- ✅ Existing protobuf schemas
- ✅ Need multi-language interop (generate for other langs separately)
- ✅ Large protobuf ecosystem integration
- ✅ Team familiar with protobuf
- ✅ Want standard tooling
- ✅ gRPC compatibility (for other services)

---

## Implementation Roadmap

### Phase 1: ProtobufToYamlConverter Core (Week 1)

**Goals:** Build the conversion engine

**Tasks:**
1. ✅ Add protobuf-java dependency to `build.gradle.kts`
2. ✅ Create `ProtobufToYamlConverter` class
3. ✅ Implement `protoc` integration for compiling `.proto` files
4. ✅ Parse `FileDescriptorSet` from protoc output
5. ✅ Convert basic message types to YAML structure
6. ✅ Convert basic field types (primitives, strings, bytes)
7. ✅ Convert enum definitions to YAML structure
8. ✅ Write YAML output using SnakeYAML
9. ✅ Add unit tests with sample `.proto` files

**Deliverable:** Can convert simple protobuf schemas to `.myra.yml`

### Phase 2: Advanced Features & Services (Week 2)

**Goals:** Support full protobuf feature set

**Tasks:**
1. ✅ Convert service definitions to YAML
2. ✅ Handle repeated fields
3. ✅ Handle optional fields
4. ✅ Handle deprecated fields
5. ✅ Handle nested messages (flatten to top-level)
6. ✅ Extract and preserve comments in YAML
7. ✅ Handle imports (with `--include_imports`)
8. ✅ Implement type mapping with warnings for unsupported types
9. ✅ Handle package/namespace extraction (`java_package` option)

**Deliverable:** Full protobuf to YAML conversion support

### Phase 3: CLI Integration & Polish (Week 3)

**Goals:** Integration and production readiness

**Tasks:**
1. ✅ Modify `MyraCodegenCli` to auto-detect `.proto` files
2. ✅ Add converter invocation logic
3. ✅ Decide on persistent vs. temporary YAML files (recommend persistent)
4. ✅ Handle unsupported features gracefully (oneof, map) with clear errors
5. ✅ Improve error messages and validation
6. ✅ Add comprehensive end-to-end test suite
7. ✅ Update `Usage.md` with protobuf workflow
8. ✅ Add example `.proto` files and conversion examples
9. ✅ Performance testing

**Deliverable:** Production-ready protobuf support

### Phase 5: Future Enhancements (Post-MVP)

**Nice-to-Have:**
1. Support for `oneof` (requires Myra runtime changes)
2. Support for `map<K,V>` (requires Myra runtime changes)
3. Direct gRPC service stub generation
4. Protobuf plugin for IDE
5. Bi-directional conversion (Myra → Protobuf)

---

## Recommendations

### ✅ Recommended: Implement Protobuf Support

**Reasons:**
1. **Strategic Value:** Opens MyraCodec to teams with existing protobuf schemas
2. **Moderate Complexity:** 3-4 week effort is reasonable
3. **Clean Architecture:** Current codebase is well-structured for this
4. **No Breaking Changes:** Purely additive feature
5. **Competitive Advantage:** Unique selling point (protobuf with zero-GC runtime)

### Implementation Priority

**High Priority:**
- Basic message/enum/service parsing
- Type mapping with warnings
- Lock file integration

**Medium Priority:**
- Import resolution
- Comment extraction
- Nested message handling

**Low Priority (Future):**
- Advanced features (oneof, map)
- Custom options support
- Bidirectional conversion

### Usage Pattern After Implementation

#### Option 1: Auto-Conversion (Recommended)

```powershell
# CLI auto-detects .proto and converts to .myra.yml automatically
.\gradlew run --args="-s service.proto -o src/main/java -l service.myra.lock"

# This internally:
# 1. Detects .proto extension
# 2. Converts to service.myra.yml (persistent file)
# 3. Proceeds with normal codegen using the YAML file
```

#### Option 2: Two-Step Process (Explicit)

```powershell
# Step 1: Convert proto to YAML (could be separate command in future)
# (Currently done automatically by codegen CLI)

# Step 2: Generate code from YAML (standard workflow)
.\gradlew run --args="-s service.myra.yml -o src/main/java -l service.myra.lock"
```

#### Files Generated

```
project/
├── service.proto           # Original protobuf (source)
├── service.myra.yml        # Generated YAML (intermediate, can commit)
├── service.myra.lock       # Lock file (references service.myra.yml)
└── src/main/java/
    └── com/example/
        ├── ServiceInterface.java
        ├── AbstractServiceServer.java
        ├── ServiceClient.java
        └── *Flyweight.java files
```

**Recommended Version Control:**
```bash
git add service.proto service.myra.yml service.myra.lock
git commit -m "Add service schema and generated YAML"
```

---

## Sample Implementation: ProtobufToYamlConverter

Here's a skeleton implementation to get started:

```java
package express.mvp.myra.codec.codegen;

import com.google.protobuf.DescriptorProtos.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Converts Protocol Buffer .proto files to MyraCodec .myra.yml format.
 * This produces a valid YAML schema that can be used with the standard
 * MyraCodec pipeline, including lock file management.
 */
public class ProtobufToYamlConverter {
    
    /**
     * Converts a .proto file to .myra.yml format.
     * 
     * @param protoFile Path to the .proto file
     * @param outputYamlFile Path for the generated .myra.yml file
     * @return Path to the generated YAML file
     */
    public Path convert(Path protoFile, Path outputYamlFile) throws Exception {
        // Step 1: Compile proto to descriptor using protoc
        Path descriptorFile = compileProtoFile(protoFile);
        
        // Step 2: Parse the descriptor
        FileDescriptorSet descriptorSet = FileDescriptorSet.parseFrom(
            Files.readAllBytes(descriptorFile)
        );
        
        // Step 3: Convert to YAML structure (Map/List)
        Map<String, Object> yamlSchema = convertToYamlStructure(
            descriptorSet.getFile(0)
        );
        
        // Step 4: Write YAML file
        writeYaml(yamlSchema, outputYamlFile);
        
        return outputYamlFile;
    }
    
    private Path compileProtoFile(Path protoFile) throws IOException, InterruptedException {
        Path descriptorFile = Files.createTempFile("proto", ".desc");
        
        ProcessBuilder pb = new ProcessBuilder(
            "protoc",
            "--descriptor_set_out=" + descriptorFile.toAbsolutePath(),
            "--include_imports",
            "--proto_path=" + protoFile.getParent().toAbsolutePath(),
            protoFile.toAbsolutePath().toString()
        );
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new IOException("protoc compilation failed with exit code: " + exitCode);
        }
        
        return descriptorFile;
    }
    
    private Map<String, Object> convertToYamlStructure(FileDescriptorProto protoFile) {
        Map<String, Object> schema = new LinkedHashMap<>();
        
        // Namespace
        schema.put("namespace", extractNamespace(protoFile));
        
        // Version
        schema.put("version", "1.0.0");
        
        // Service
        if (protoFile.getServiceCount() > 0) {
            schema.put("service", convertService(protoFile.getService(0)));
        }
        
        // Messages
        List<Map<String, Object>> messages = new ArrayList<>();
        for (DescriptorProto msg : protoFile.getMessageTypeList()) {
            messages.add(convertMessage(msg));
        }
        schema.put("messages", messages);
        
        // Enums
        List<Map<String, Object>> enums = new ArrayList<>();
        for (EnumDescriptorProto enumDef : protoFile.getEnumTypeList()) {
            enums.add(convertEnum(enumDef));
        }
        if (!enums.isEmpty()) {
            schema.put("enums", enums);
        }
        
        return schema;
    }
    
    private String extractNamespace(FileDescriptorProto file) {
        if (file.getOptions().hasJavaPackage()) {
            return file.getOptions().getJavaPackage();
        }
        return file.getPackage();
    }
    
    private Map<String, Object> convertMessage(DescriptorProto protoMsg) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("name", protoMsg.getName());
        
        List<Map<String, Object>> fields = new ArrayList<>();
        for (FieldDescriptorProto field : protoMsg.getFieldList()) {
            fields.add(convertField(field));
        }
        message.put("fields", fields);
        
        return message;
    }
    
    private Map<String, Object> convertField(FieldDescriptorProto protoField) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", protoField.getName());
        field.put("type", mapProtobufType(protoField));
        
        // Add optional flags only if true (cleaner YAML output)
        if (protoField.getLabel() == FieldDescriptorProto.Label.LABEL_REPEATED) {
            field.put("repeated", true);
        }
        if (protoField.getLabel() == FieldDescriptorProto.Label.LABEL_OPTIONAL) {
            field.put("optional", true);
        }
        if (protoField.getOptions().getDeprecated()) {
            field.put("deprecated", true);
            field.put("deprecation_note", "Deprecated in protobuf schema");
        }
        
        return field;
    }
    
    private String mapProtobufType(FieldDescriptorProto field) {
        return switch (field.getType()) {
            case TYPE_BOOL -> "bool";
            case TYPE_INT32, TYPE_SINT32, TYPE_SFIXED32 -> "int32";
            case TYPE_INT64, TYPE_SINT64, TYPE_SFIXED64 -> "int64";
            case TYPE_FLOAT -> "float32";
            case TYPE_DOUBLE -> "float64";
            case TYPE_STRING -> "string";
            case TYPE_BYTES -> "bytes";
            case TYPE_ENUM -> field.getTypeName().substring(field.getTypeName().lastIndexOf('.') + 1);
            case TYPE_MESSAGE -> field.getTypeName().substring(field.getTypeName().lastIndexOf('.') + 1);
            case TYPE_UINT32, TYPE_FIXED32 -> {
                System.err.println("WARNING: Unsigned int32 not supported for field '" 
                    + field.getName() + "', using int32");
                yield "int32";
            }
            case TYPE_UINT64, TYPE_FIXED64 -> {
                System.err.println("WARNING: Unsigned int64 not supported for field '" 
                    + field.getName() + "', using int64");
                yield "int64";
            }
            default -> throw new UnsupportedOperationException(
                "Unsupported protobuf type: " + field.getType()
            );
        };
    }
    
    private Map<String, Object> convertEnum(EnumDescriptorProto protoEnum) {
        Map<String, Object> enumDef = new LinkedHashMap<>();
        enumDef.put("name", protoEnum.getName());
        enumDef.put("type", "int8"); // Default enum storage type
        
        List<Map<String, Object>> values = new ArrayList<>();
        for (EnumValueDescriptorProto value : protoEnum.getValueList()) {
            Map<String, Object> enumValue = new LinkedHashMap<>();
            enumValue.put("name", value.getName());
            enumValue.put("id", value.getNumber());
            values.add(enumValue);
        }
        enumDef.put("values", values);
        
        return enumDef;
    }
    
    private Map<String, Object> convertService(ServiceDescriptorProto protoService) {
        Map<String, Object> service = new LinkedHashMap<>();
        service.put("name", protoService.getName());
        
        // Extract description from comments if available
        // (requires parsing source_code_info from descriptor)
        
        List<Map<String, Object>> rpcs = new ArrayList<>();
        for (MethodDescriptorProto method : protoService.getMethodList()) {
            rpcs.add(convertMethod(method));
        }
        service.put("rpcs", rpcs);
        
        return service;
    }
    
    private Map<String, Object> convertMethod(MethodDescriptorProto method) {
        Map<String, Object> rpc = new LinkedHashMap<>();
        rpc.put("name", toCamelCase(method.getName()));
        rpc.put("request", extractTypeName(method.getInputType()));
        rpc.put("response", extractTypeName(method.getOutputType()));
        
        return rpc;
    }
    
    private void writeYaml(Map<String, Object> data, Path outputFile) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        
        Yaml yaml = new Yaml(options);
        try (Writer writer = Files.newBufferedWriter(outputFile)) {
            yaml.dump(data, writer);
        }
    }
    
    private String extractTypeName(String fullName) {
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }
    
    private String toCamelCase(String name) {
        if (name.isEmpty()) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
```

---

## Conclusion

**YES**, adding Protocol Buffers support to MyraCodec is **feasible and recommended**. 

### Key Takeaways

1. **Architecture is Ready:** Converting to YAML first preserves existing architecture
2. **Lower Effort:** 2-3 weeks (reduced from original 3-4 week estimate)
3. **Strategic Value:** Opens MyraCodec to existing protobuf users
4. **No Breaking Changes:** Zero impact on existing code
5. **Clear Implementation Path:** Protobuf → YAML converter + CLI integration
6. **Lock File Semantics Preserved:** Lock file references YAML as designed

### Advantages of YAML Conversion Approach

| Aspect | Value |
|--------|-------|
| **Existing Code Impact** | Zero - all existing code unchanged |
| **Lock File Compatibility** | Perfect - lock file references YAML as designed |
| **Debugging** | Easy - can inspect intermediate YAML |
| **Architecture** | Clean - proper separation of concerns |
| **Testing** | Simpler - test converter independently |
| **Future Extensions** | Easy - can add JSON, TOML, etc. converters |

### Next Steps

1. Get stakeholder approval for 2-3 week effort
2. Add `protobuf-java:3.25.1` dependency to `build.gradle.kts`
3. Implement Phase 1: `ProtobufToYamlConverter` core (Week 1)
4. Implement Phase 2: Advanced features (Week 2)
5. Implement Phase 3: CLI integration & polish (Week 3)
6. Update `Usage.md` with protobuf workflow examples

### Recommended Workflow for Users

```bash
# 1. Write your schema in protobuf (or use existing .proto files)
vim service.proto

# 2. Run codegen - auto-converts to YAML and generates code
./gradlew run --args="-s service.proto -o src/main/java -l service.myra.lock"

# Result: Creates service.myra.yml + generates Java code

# 3. Commit all schema files
git add service.proto service.myra.yml service.myra.lock

# 4. Future runs use the YAML (or proto, both work)
./gradlew run --args="-s service.myra.yml -o src/main/java -l service.myra.lock"
```

### Final Recommendation

**Implement protobuf support via YAML conversion as an MVP feature.** This approach:
- ✅ Reduces implementation complexity
- ✅ Eliminates risk to existing code
- ✅ Preserves lock file semantics
- ✅ Provides better debugging experience
- ✅ Opens migration path for protobuf users

The best of both worlds: **protobuf schemas with MyraCodec's zero-GC runtime** 🚀

### Migration Path

For teams with existing protobuf:
1. **Phase 1:** Use `.proto` files, auto-generate `.myra.yml`
2. **Phase 2:** Gradually customize `.myra.yml` to use Myra-specific features
3. **Phase 3:** Eventually deprecate `.proto` files, use `.myra.yml` as source

This provides a smooth, risk-free transition to MyraCodec while maintaining protobuf compatibility during migration.
