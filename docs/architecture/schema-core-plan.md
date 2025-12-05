# Schema Core Sharing Plan (Implemented — December 2025)

> **Status**: ✅ Implemented. See [module-structure.md](module-structure.md) for current architecture.

## Goals

- Allow `.myra.yml` (data schema) parsing/resolution to live inside Myra Codec without any RPC/service coupling.
- Enable `.mvpe.yml` (service schema) to import/include `.myra.yml` definitions and generate `.mvpe.lock` while reusing the same AST/resolution logic.
- Provide a single source of truth for schema evolution (hashes, reserved IDs, compatibility checks).

## Proposed Modules

1. **schema-core** (new, in `myra-codec/lib`)
   - Data structures: Namespace, Message, Field, Enum, ScalarType, Options, Constraints.
   - Services: SchemaParser, LockFileModel, LayoutResolver.
   - Outputs `.myra.lock` + runtime metadata consumed by codegen + transports.
2. **codec-gen** (existing)
   - Depends on schema-core.
   - Emits flyweights, builders, adapters, runtime helpers.
3. **rpc-schema** (future, in rpc-framework repo)
   - Depends on schema-core artifact (published from codec repo).
   - Adds Service, RpcMethod, StreamingMode, QoS, RetryPolicy, Visibility, ACL metadata.
   - Produces `.mvpe.lock`, referencing imported `.myra.lock` hashes.

## Runtime Split

- **Move generic helpers to roray-ffm-utils**: `PooledSegment` and similar wrappers now live beside `MemorySegmentPool`, so generated code can depend solely on the roray package for pooling/segment helpers.
- **Optional myra-codec-runtime artifact**: keep only codec-specific flyweight helpers that cannot reasonably live in roray; this artifact must depend exclusively on `roray-ffm-utils`.
- **RPC-specific runtime**: interfaces such as `RpcTransport`, message envelopes carrying tracing/deadline metadata, and service/client stubs migrate to the RPC framework (future `rpc-runtime` module) where they can depend on transport implementations.

## File Relationships

```text
.schemas/
  data/
    order_book.myra.yml   # owned by codec tooling
  rpc/
    order_service.mvpe.yml  # owned by rpc tooling, imports data schema
```

- The RPC parser first loads `.mvpe.yml`.
- For every `imports` entry it resolves the corresponding `.myra.lock` (preferred) or `.myra.yml`.
- Lock hashes are embedded into `.mvpe.lock` so CI can detect mismatches.

## CLI Updates

- `myra-codec` CLI gains `--emit-schema-model <file>` (JSON/YAML) for downstream consumers/tests.
- RPC CLI adds `--schema-path` to locate `.myra.yml` roots and `--lock-cache` for speedy rebuilds.

## Next Steps

1. Extract current `express.mvp.myra.codec.schema` model into its own Gradle subproject (`lib/schema-core`).
2. Publish schema-core as an internal artifact so rpc-framework can depend on it.
3. Prototype `.mvpe.yml` parser that loads `.myra.lock` and validates imports.
4. Document the inclusion semantics + sample folder layout in repo docs.
