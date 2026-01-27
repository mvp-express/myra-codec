# MyraCodec

> Zero-copy serialization with schema-driven code generation for Java's FFM API.

[![Build](https://img.shields.io/github/actions/workflow/status/mvp-express/myra-codec/build.yml?branch=main)](https://github.com/mvp-express/myra-codec/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## Installation

For applications using generated flyweights:

```kotlin
dependencies {
    implementation("express.mvp.myra.codec:runtime:0.2.1")
}
```

For code generation tooling:

```kotlin
dependencies {
    implementation("express.mvp.myra.codec:codegen:0.2.1")
}
```

**Requires Java 25+** with FFM enabled:

```bash
java --enable-native-access=ALL-UNNAMED -jar your-app.jar
```

## Quick Example

Define a schema in `.myra.yml`:

```yaml
namespace: "com.example"
messages:
  - name: "Order"
    fields:
      - { tag: 1, name: "id", type: "int64" }
      - { tag: 2, name: "symbol", type: "string" }
```

Generate and use:

```bash
./gradlew :codegen:run --args "-s order.myra.yml -o src/generated -l order.myra.lock"
```

```java
var builder = new OrderBuilder();
builder.wrap(segment).id(123L).symbol("AAPL");

var flyweight = new OrderFlyweight();
flyweight.wrap(segment, 0);
long id = flyweight.id();  // Zero-copy read
```

## Documentation

📚 **[User Guide](https://mvp.express/docs/myra-codec/)** — Full documentation  
🚀 **[Getting Started](https://mvp.express/docs/quick-start-guide/)** — Ecosystem tutorial  
📖 **[API Reference](https://mvp.express/myra-codec/api/latest/)** — Javadoc  
📋 **[Schema Specification](docs/specs/myra-schema-specification.md)** — `.myra.yml` format

## For Contributors

See [CONTRIBUTING.md](CONTRIBUTING.md) for build instructions and PR process.

## License

Apache 2.0 — See [LICENSE](LICENSE)
