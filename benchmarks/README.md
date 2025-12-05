# Myra Codec Benchmarks

This module hosts comparative JMH benchmarks for MyraCodec versus other binary codecs.

## Running

```bash
./gradlew :benchmarks:jmh
```

### Fast iteration mode

Set `-Pjmh.quick=true` to shrink the default 5×10s warmups/measurements down to a single warmup, measurement, and fork. This keeps runs under a minute when you only need directional numbers.

```bash
./gradlew :benchmarks:jmh -Pjmh.include=OrderBookBenchmark -Pjmh.quick=true
```

You can further override the quick counts with `-Pjmh.quickWarmups`, `-Pjmh.quickIterations`, and `-Pjmh.quickForks` if you need a slightly longer sanity check without jumping back to the 1‑hour production configuration.

### Regenerating generated codecs

FlatBuffers and SBE bindings live under `benchmarks/src/generated/java` and must stay in sync with the schemas in `benchmarks/schema/**`. After editing a schema, rerun the Dockerized generators (Docker + network access required):

```bash
./benchmarks/scripts/generate_sbe.sh
./benchmarks/scripts/generate_flatbuffers.sh
```

The scripts stash tool artifacts under `.codegen-cache`, reuse the host UID/GID to prevent root-owned files, and overwrite the checked-in sources in place.

## Next Steps


Notes:

- Myra bench codegen will produce `Builder` types for single-pass encoding. Use
  `MessageEncoder` to acquire a segment and finalize header. Builders are
  write-once: the runtime enforces a no-mutation design.
- We will also experiment with SBE-style `fixed_capacity` hints for hot-path
  messages, which may reduce pointer chasing at the cost of memory.

## Codec Layout Plan

- `src/jmh/java/express/mvp/myra/codec/bench/myra` — wraps generated flyweights, pooling, and encode/decode helpers reused by the Myra JMH methods.
- `.../bench/avro|protobuf|kryo|thrift` — one package per codec containing schema bindings/builders plus adapters that translate the shared `bench.model` POJOs into each wire format.
- `.../bench/io` — fixture loaders, schema registries, and any cross-codec validation logic (all benchmarks pull datasets from this layer).
- `.../bench/codec` — future `CodecHarness` interfaces so each implementation plugs into the same encode/decode loops (keeps the `@Benchmark` classes tiny).
- `benchmarks/build/generated` — generated assets (Myra flyweights, Avro specific classes, etc.) wired into the packages above via Gradle sourceSets.
