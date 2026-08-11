#!/bin/bash
# Shared Maven wrapper selection for six-index shell scripts.
# Sources ./mvnw.cmd on Windows-style bash shells and ./mvnw elsewhere.

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

if [[ -z "${PROJECT_ROOT:-}" ]]; then
  PROJECT_ROOT="$(common::repo_root_from "${BASH_SOURCE[0]}")"
fi

if common::is_windows_bash; then
    MAVEN_CMD=("$PROJECT_ROOT/mvnw.cmd" -q)
else
    MAVEN_CMD=("$PROJECT_ROOT/mvnw" -q)
fi

if [[ ! -f "${MAVEN_CMD[0]}" ]]; then
  common::print_error "Maven wrapper not found: ${MAVEN_CMD[0]}"
  return 1 2>/dev/null || exit 1
fi

run_maven() {
  "${MAVEN_CMD[@]}" "$@"
}

