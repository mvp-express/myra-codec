# AGENTS.md

## Purpose

Agent harness for `/home/ubuntu/mvp-express/myra-codec`.

Repository knowledge map: `docs/INDEX.md`.

Workflow guidance: `docs/WORKFLOW.md`.

## Rules

- Treat source and build files as authoritative; docs may be stale.
- Preserve zero-copy, low-allocation codec behavior.
- Keep schema, resolver, codegen, runtime, examples, and docs in sync.
- Prefer public-boundary tests before implementation changes.
- Do not treat generated or compiled `bin/` artifacts as source of truth.

## Verification

Run:

```bash
./tools/harness/check.sh
./gradlew check
```
