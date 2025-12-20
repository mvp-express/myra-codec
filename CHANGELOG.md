# Unreleased

- Fix: Prevent NullPointerException during code generation when schema files omit `enums`.
  - Defensive normalization in `SchemaParser` converts missing lists to empty lists so downstream
    code no longer needs null checks.
  - Added parser tests and resources for schemas with no enums in both `schema-core` and `codegen` modules.
  - Reverted temporary null-check in `SchemaResolver` — parser normalization is the single source of truth.
