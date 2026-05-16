# Workflow Guidance

## Start

1. Read `AGENTS.md`.
2. Use `docs/INDEX.md` to find the relevant module.
3. Check `git status --short`.
4. Inspect source/build files before trusting docs.

## Implementation

- Add or update one public-boundary test first.
- For schema changes, update parser/resolver/codegen tests together.
- For runtime changes, preserve borrowed-buffer and flyweight lifetime semantics.
- For codegen changes, verify generated Java compiles and round-trips.
- Keep examples aligned when public schema or generated API shape changes.

## Validation

```bash
./tools/harness/check.sh
./gradlew check
```
