# Repository Knowledge Map

`myra-codec` is the schema-driven codec and flyweight code generation library.

Source-verified modules:
- `schema-core` - `.myra.yml` schema model, parser, resolver, and lock files
- `runtime` - runtime encoding and struct helpers
- `codegen` - Java code generator and CLI
- `examples` - generated codec usage examples
- `benchmarks` - performance benchmarks

Start here:
- `README.md` - public overview
- `docs/specs/myra-schema-specification.md` - schema format
- `docs/architecture/module-structure.md` - module architecture
- `docs/guide.md` - usage guide
- `docs/quality/README.md` - validation harness

Important package roots:
- `express.mvp.myra.codec.schema`
- `express.mvp.myra.codec.schema.resolver`
- `express.mvp.myra.codec.runtime`
- `express.mvp.myra.codec.codegen`

Agent rule:
- verify README or guide claims against `settings.gradle.kts`, `build.gradle.kts`, and `src/main/java` before relying on them
