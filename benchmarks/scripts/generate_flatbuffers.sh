#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BENCHMARKS_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPO_ROOT="$(cd "${BENCHMARKS_DIR}/.." && pwd)"
SCHEMA_FILE="${BENCHMARKS_DIR}/schema/flatbuffers/order_book.fbs"
OUTPUT_DIR="${BENCHMARKS_DIR}/src/generated/java"
HOST_UID="$(id -u)"
HOST_GID="$(id -g)"

mkdir -p "${OUTPUT_DIR}"

docker run --rm \
  -v "${REPO_ROOT}:/workspace" \
  -e DEBIAN_FRONTEND=noninteractive \
  -e HOST_UID="${HOST_UID}" \
  -e HOST_GID="${HOST_GID}" \
  debian:bookworm-slim \
  bash -c "set -euo pipefail && \
    apt-get update >/dev/null && \
    apt-get install -y flatbuffers-compiler >/dev/null && \
    flatc --java -o /workspace/benchmarks/src/generated/java /workspace/benchmarks/schema/flatbuffers/order_book.fbs && \
    chown -R \"${HOST_UID}\":\"${HOST_GID}\" /workspace/benchmarks/src/generated/java"

echo "FlatBuffers sources regenerated under ${OUTPUT_DIR}."
