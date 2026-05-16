# Quality Harness

Required checks:

```bash
./tools/harness/check.sh
./gradlew check
```

Current harness coverage:
- required docs exist
- `AGENTS.md` points to `docs/INDEX.md` and `docs/WORKFLOW.md`
- expected Gradle modules are present
- expected source package roots are present
- `bin/` artifacts are flagged as generated/build output, not source truth

Future checks:
- generated source freshness
- schema lock compatibility checks
- docs link validation against actual source modules
