#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

fail() {
  printf 'myra-codec harness check failed: %s\n' "$1" >&2
  exit 1
}

require_file() {
  [[ -f "$ROOT/$1" ]] || fail "missing $1"
}

require_dir() {
  [[ -d "$ROOT/$1" ]] || fail "missing $1"
}

require_text() {
  grep -Fq "$2" "$ROOT/$1" || fail "$1 must reference $2"
}

require_file "AGENTS.md"
require_file "docs/INDEX.md"
require_file "docs/WORKFLOW.md"
require_file "docs/quality/README.md"
require_file "settings.gradle.kts"
require_file "build.gradle.kts"

require_text "AGENTS.md" "docs/INDEX.md"
require_text "AGENTS.md" "docs/WORKFLOW.md"
require_text "settings.gradle.kts" "include(\"schema-core\")"
require_text "settings.gradle.kts" "include(\"runtime\")"
require_text "settings.gradle.kts" "include(\"codegen\")"
require_text "settings.gradle.kts" "include(\"examples\")"

require_dir "schema-core/src/main/java/express/mvp/myra/codec/schema"
require_dir "runtime/src/main/java/express/mvp/myra/codec/runtime"
require_dir "codegen/src/main/java/express/mvp/myra/codec/codegen"

printf 'myra-codec harness check passed\n'
