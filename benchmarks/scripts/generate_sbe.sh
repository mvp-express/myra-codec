#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BENCHMARKS_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPO_ROOT="$(cd "${BENCHMARKS_DIR}/.." && pwd)"
SCHEMA_FILE="${BENCHMARKS_DIR}/schema/sbe/order_book.xml"
OUTPUT_DIR="${BENCHMARKS_DIR}/src/generated/java"
CACHE_DIR="${BENCHMARKS_DIR}/.codegen-cache/sbe"
SBE_VERSION="${SBE_VERSION:-1.35.0}"
AGRONA_VERSION="${AGRONA_VERSION:-2.2.0}"
HOST_UID="$(id -u)"
HOST_GID="$(id -g)"

mkdir -p "${CACHE_DIR}" "${OUTPUT_DIR}"
JAR_PATH="${CACHE_DIR}/sbe-tool-${SBE_VERSION}.jar"
if [ ! -f "${JAR_PATH}" ]; then
  echo "Downloading sbe-tool ${SBE_VERSION}..."
  curl -sSL "https://repo1.maven.org/maven2/uk/co/real-logic/sbe-tool/${SBE_VERSION}/sbe-tool-${SBE_VERSION}.jar" -o "${JAR_PATH}"
fi
AGRONA_JAR="${CACHE_DIR}/agrona-${AGRONA_VERSION}.jar"
if [ ! -f "${AGRONA_JAR}" ]; then
  echo "Downloading agrona ${AGRONA_VERSION}..."
  curl -sSL "https://repo1.maven.org/maven2/org/agrona/agrona/${AGRONA_VERSION}/agrona-${AGRONA_VERSION}.jar" -o "${AGRONA_JAR}"
fi

docker run --rm \
  -v "${REPO_ROOT}:/workspace" \
  -v "${JAR_PATH}:/tmp/sbe-tool.jar:ro" \
  -v "${AGRONA_JAR}:/tmp/agrona.jar:ro" \
  -e HOST_UID="${HOST_UID}" \
  -e HOST_GID="${HOST_GID}" \
  eclipse-temurin:21-jdk \
  bash -c "set -euo pipefail && \
    java -Dsbe.target.language=java -Dsbe.output.dir=/workspace/benchmarks/src/generated/java \
         -cp /tmp/sbe-tool.jar:/tmp/agrona.jar uk.co.real_logic.sbe.SbeTool \
         /workspace/benchmarks/schema/sbe/order_book.xml && \
    chown -R \"${HOST_UID}\":\"${HOST_GID}\" /workspace/benchmarks/src/generated/java"

echo "SBE sources regenerated under ${OUTPUT_DIR}."
