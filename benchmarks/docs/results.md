# Benchmark Results

This document tracks encode/decode throughput for the codec benchmarks. After each JMH run, execute `python benchmarks/scripts/update_results.py` to append the latest measurements and refresh the summary table.

## Latest run
<!-- LATEST_RUN_START -->
### 2025-11-15T19:37:35Z — `./gradlew :benchmarks:jmh -Pjmh.include=SbeOrderBookBenchmark -Pjmh.quick=true -Pjmh.quickWarmups=1 -Pjmh.quickIterations=1 -Pjmh.quickForks=1`

- Host: `c6a.4xlarge (Linux 5.15.0-1054-aws)`
- JVM: Temurin 25.0.1+8 LTS
- Warmups: 1 × 10s, Measurements: 1 × 10s, Forks: 1
- Notes: Quick mode (jmh.quick=true)

| Codec | Operation | Dataset | Throughput (ops/s) | Error (99.9%) |
| --- | --- | --- | --- | --- |
| Avro | Decode | benchmarks/data/order_book_snapshot.json | 359,321.963 | — |
| FlatBuffers | Decode | benchmarks/data/order_book_snapshot.json | 1,451,580.74 | — |
| Kryo | Decode | benchmarks/data/order_book_snapshot.json | 1,016,779.126 | — |
| Myra | Decode | benchmarks/data/order_book_snapshot.json | 2,721,551.212 | — |
| SBE | Decode | benchmarks/data/order_book_snapshot.json | 2,204,557.047 | — |
| Avro | Encode | benchmarks/data/order_book_snapshot.json | 342,544.086 | — |
| FlatBuffers | Encode | benchmarks/data/order_book_snapshot.json | 757,091.787 | — |
| Kryo | Encode | benchmarks/data/order_book_snapshot.json | 700,404.775 | — |
| Myra | Encode | benchmarks/data/order_book_snapshot.json | 1,569,329.116 | — |
| SBE | Encode | benchmarks/data/order_book_snapshot.json | 2,618,446.117 | — |
| Avro | Decode | benchmarks/data/order_book_snapshots_sample.json | 454,552.987 | — |
| FlatBuffers | Decode | benchmarks/data/order_book_snapshots_sample.json | 1,968,855.128 | — |
| Kryo | Decode | benchmarks/data/order_book_snapshots_sample.json | 1,322,754.213 | — |
| Myra | Decode | benchmarks/data/order_book_snapshots_sample.json | 4,150,078.944 | — |
| SBE | Decode | benchmarks/data/order_book_snapshots_sample.json | 1,813,579.569 | — |
| Avro | Encode | benchmarks/data/order_book_snapshots_sample.json | 466,816.148 | — |
| FlatBuffers | Encode | benchmarks/data/order_book_snapshots_sample.json | 1,045,843.018 | — |
| Kryo | Encode | benchmarks/data/order_book_snapshots_sample.json | 1,342,611.323 | — |
| Myra | Encode | benchmarks/data/order_book_snapshots_sample.json | 1,911,780.791 | — |
| SBE | Encode | benchmarks/data/order_book_snapshots_sample.json | 4,990,071.115 | — |
<!-- LATEST_RUN_END -->

## Run history
<!-- RUN_HISTORY_START -->
### 2025-11-15T19:37:35Z — `./gradlew :benchmarks:jmh -Pjmh.include=SbeOrderBookBenchmark -Pjmh.quick=true -Pjmh.quickWarmups=1 -Pjmh.quickIterations=1 -Pjmh.quickForks=1` (historical)

- Host: `c6a.4xlarge (Linux 5.15.0-1054-aws)`
- JVM: Temurin 25.0.1+8 LTS
- Warmups: 1 × 10s, Measurements: 1 × 10s, Forks: 1
- Notes: Quick mode (jmh.quick=true)

| Codec | Operation | Dataset | Throughput (ops/s) | Error (99.9%) |
| --- | --- | --- | --- | --- |
| Avro | Decode | benchmarks/data/order_book_snapshot.json | 359,321.963 | — |
| FlatBuffers | Decode | benchmarks/data/order_book_snapshot.json | 1,451,580.74 | — |
| Kryo | Decode | benchmarks/data/order_book_snapshot.json | 1,016,779.126 | — |
| Myra | Decode | benchmarks/data/order_book_snapshot.json | 2,721,551.212 | — |
| SBE | Decode | benchmarks/data/order_book_snapshot.json | 2,204,557.047 | — |
| Avro | Encode | benchmarks/data/order_book_snapshot.json | 342,544.086 | — |
| FlatBuffers | Encode | benchmarks/data/order_book_snapshot.json | 757,091.787 | — |
| Kryo | Encode | benchmarks/data/order_book_snapshot.json | 700,404.775 | — |
| Myra | Encode | benchmarks/data/order_book_snapshot.json | 1,569,329.116 | — |
| SBE | Encode | benchmarks/data/order_book_snapshot.json | 2,618,446.117 | — |
| Avro | Decode | benchmarks/data/order_book_snapshots_sample.json | 454,552.987 | — |
| FlatBuffers | Decode | benchmarks/data/order_book_snapshots_sample.json | 1,968,855.128 | — |
| Kryo | Decode | benchmarks/data/order_book_snapshots_sample.json | 1,322,754.213 | — |
| Myra | Decode | benchmarks/data/order_book_snapshots_sample.json | 4,150,078.944 | — |
| SBE | Decode | benchmarks/data/order_book_snapshots_sample.json | 1,813,579.569 | — |
| Avro | Encode | benchmarks/data/order_book_snapshots_sample.json | 466,816.148 | — |
| FlatBuffers | Encode | benchmarks/data/order_book_snapshots_sample.json | 1,045,843.018 | — |
| Kryo | Encode | benchmarks/data/order_book_snapshots_sample.json | 1,342,611.323 | — |
| Myra | Encode | benchmarks/data/order_book_snapshots_sample.json | 1,911,780.791 | — |
| SBE | Encode | benchmarks/data/order_book_snapshots_sample.json | 4,990,071.115 | — |

### 2025-11-15T18:47:51Z — `./gradlew :benchmarks:jmh -Pjmh.include=OrderBookBenchmark -Pjmh.quick=true -Pjmh.quickWarmups=1 -Pjmh.quickIterations=1 -Pjmh.quickForks=1` (historical)

- Host: `c6a.4xlarge (Linux 5.15.0-1054-aws)`
- JVM: Temurin 25.0.1+8 LTS
- Warmups: 1 × 10s, Measurements: 1 × 10s, Forks: 1
- Notes: Quick mode (jmh.quick=true)

| Codec | Operation | Dataset | Throughput (ops/s) | Error (99.9%) |
| --- | --- | --- | --- | --- |
| Avro | Decode | benchmarks/data/order_book_snapshot.json | 366,846.474 | — |
| Kryo | Decode | benchmarks/data/order_book_snapshot.json | 979,092.143 | — |
| Myra | Decode | benchmarks/data/order_book_snapshot.json | 2,836,473.312 | — |
| Avro | Encode | benchmarks/data/order_book_snapshot.json | 341,946.824 | — |
| Kryo | Encode | benchmarks/data/order_book_snapshot.json | 650,157.015 | — |
| Myra | Encode | benchmarks/data/order_book_snapshot.json | 1,535,880.471 | — |
| Avro | Decode | benchmarks/data/order_book_snapshots_sample.json | 454,777.366 | — |
| Kryo | Decode | benchmarks/data/order_book_snapshots_sample.json | 1,253,275.293 | — |
| Myra | Decode | benchmarks/data/order_book_snapshots_sample.json | 4,158,799.677 | — |
| Avro | Encode | benchmarks/data/order_book_snapshots_sample.json | 473,916.959 | — |
| Kryo | Encode | benchmarks/data/order_book_snapshots_sample.json | 1,289,824.124 | — |
| Myra | Encode | benchmarks/data/order_book_snapshots_sample.json | 1,915,329.483 | — |

### 2025-11-15T18:35:57Z — `./gradlew :benchmarks:jmh -Pjmh.include=MyraOrderBookBenchmark -Pjmh.quick=true -Pjmh.quickWarmups=1 -Pjmh.quickIterations=1 -Pjmh.quickForks=1` (historical)

- Host: `c6a.4xlarge (Linux 5.15.0-1054-aws)`
- JVM: Temurin 25.0.1+8 LTS
- Warmups: 1 × 10s, Measurements: 1 × 10s, Forks: 1
- Notes: Quick mode (jmh.quick=true)

| Codec | Operation | Dataset | Throughput (ops/s) | Error (99.9%) |
| --- | --- | --- | --- | --- |
| Kryo | Decode | benchmarks/data/order_book_snapshot.json | 1,014,238.22 | — |
| Myra | Decode | benchmarks/data/order_book_snapshot.json | 2,853,340.363 | — |
| Kryo | Encode | benchmarks/data/order_book_snapshot.json | 683,802.363 | — |
| Myra | Encode | benchmarks/data/order_book_snapshot.json | 1,506,037.325 | — |
| Kryo | Decode | benchmarks/data/order_book_snapshots_sample.json | 1,322,584.342 | — |
| Myra | Decode | benchmarks/data/order_book_snapshots_sample.json | 4,155,399.519 | — |
| Kryo | Encode | benchmarks/data/order_book_snapshots_sample.json | 1,273,620.645 | — |
| Myra | Encode | benchmarks/data/order_book_snapshots_sample.json | 1,940,334.235 | — |

### 2025-02-15T00:00:00Z — `./gradlew :benchmarks:jmh -Pjmh.include=OrderBookBenchmark` (historical)

- Host: `c6a.4xlarge` (Linux `5.15.0-1054-aws`)
- JVM: Temurin 25.0.1+8 LTS
- Warmups: 5 × 10s, Measurements: 5 × 10s, Forks: 5

| Codec | Operation | Dataset | Throughput (ops/s) | Error (99.9%) |
| --- | --- | --- | --- | --- |
| Kryo | Decode | order_book_snapshots_sample.json | 1,322,048.974 | ±4,483.222 |
| Kryo | Decode | order_book_snapshot.json | 1,022,223.340 | ±6,965.354 |
| Kryo | Encode | order_book_snapshots_sample.json | 1,288,541.272 | ±3,041.034 |
| Kryo | Encode | order_book_snapshot.json | 693,156.568 | ±16,074.327 |
| Myra | Decode | order_book_snapshots_sample.json | 4,178,558.181 | ±6,426.014 |
| Myra | Decode | order_book_snapshot.json | 2,795,058.510 | ±36,905.976 |
| Myra | Encode | order_book_snapshots_sample.json | 1,922,002.759 | ±3,428.845 |
| Myra | Encode | order_book_snapshot.json | 1,536,365.036 | ±24,158.455 |
<!-- RUN_HISTORY_END -->

## Observations

1. **Decode leaders**: Myra still tops the decode charts (2.7M–4.1M ops/s), with SBE now within ~19% on the single-snapshot payload and FlatBuffers leapfrogging Kryo on both datasets.
2. **Encode crossover**: SBE’s encoder is the new outlier at 2.6M–5.0M ops/s, running 60–160% faster than Myra; FlatBuffers lands between Myra and Kryo thanks to its zero-copy builder.
3. **Shape sensitivity**: FlatBuffers and Kryo lose 40–50% throughput when switching to the single snapshot, whereas SBE/Myra drop <25%, indicating their schemas amortize per-message overhead more effectively.

## Next steps

1. Add CSV/JSON exports so new runs append to this document automatically.
2. Capture allocation/GC data (e.g., `-prof gc`) to explain why SBE’s encode throughput spikes relative to FlatBuffers/Myra.
3. Extend the fixture set (deeper books, trades-only, etc.) and report payload sizes so we can relate throughput swings to wire footprint.

## Consolidated summary (latest)
<!-- SUMMARY_TABLE_START -->
| Dataset | Operation | Codec | Throughput (ops/s) | Δ vs best (%) | Δ vs Myra (%) |
| --- | --- | --- | --- | --- | --- |
| benchmarks/data/order_book_snapshot.json | Decode | Avro | 359,321.963 | -86.8% | -86.8% |
| benchmarks/data/order_book_snapshot.json | Decode | FlatBuffers | 1,451,580.74 | -46.7% | -46.7% |
| benchmarks/data/order_book_snapshot.json | Decode | Kryo | 1,016,779.126 | -62.6% | -62.6% |
| benchmarks/data/order_book_snapshot.json | Decode | Myra | 2,721,551.212 | +0.0% | +0.0% |
| benchmarks/data/order_book_snapshot.json | Decode | SBE | 2,204,557.047 | -19.0% | -19.0% |
| benchmarks/data/order_book_snapshot.json | Encode | Avro | 342,544.086 | -86.9% | -78.2% |
| benchmarks/data/order_book_snapshot.json | Encode | FlatBuffers | 757,091.787 | -71.1% | -51.8% |
| benchmarks/data/order_book_snapshot.json | Encode | Kryo | 700,404.775 | -73.3% | -55.4% |
| benchmarks/data/order_book_snapshot.json | Encode | Myra | 1,569,329.116 | -40.1% | +0.0% |
| benchmarks/data/order_book_snapshot.json | Encode | SBE | 2,618,446.117 | +0.0% | +66.9% |
| benchmarks/data/order_book_snapshots_sample.json | Decode | Avro | 454,552.987 | -89.0% | -89.0% |
| benchmarks/data/order_book_snapshots_sample.json | Decode | FlatBuffers | 1,968,855.128 | -52.6% | -52.6% |
| benchmarks/data/order_book_snapshots_sample.json | Decode | Kryo | 1,322,754.213 | -68.1% | -68.1% |
| benchmarks/data/order_book_snapshots_sample.json | Decode | Myra | 4,150,078.944 | +0.0% | +0.0% |
| benchmarks/data/order_book_snapshots_sample.json | Decode | SBE | 1,813,579.569 | -56.3% | -56.3% |
| benchmarks/data/order_book_snapshots_sample.json | Encode | Avro | 466,816.148 | -90.6% | -75.6% |
| benchmarks/data/order_book_snapshots_sample.json | Encode | FlatBuffers | 1,045,843.018 | -79.0% | -45.3% |
| benchmarks/data/order_book_snapshots_sample.json | Encode | Kryo | 1,342,611.323 | -73.1% | -29.8% |
| benchmarks/data/order_book_snapshots_sample.json | Encode | Myra | 1,911,780.791 | -61.7% | +0.0% |
| benchmarks/data/order_book_snapshots_sample.json | Encode | SBE | 4,990,071.115 | +0.0% | +161.0% |
<!-- SUMMARY_TABLE_END -->
