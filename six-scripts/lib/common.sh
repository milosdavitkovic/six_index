#!/usr/bin/env bash
# Shared shell utilities for the six-index scripts.
# This file contains only helpers used by the scripts in this repository.

set -euo pipefail
IFS=$'\n\t'

common::print_info() {
  printf '[INFO] %s\n' "$1"
}

common::print_warn() {
  printf '[WARN] %s\n' "$1" >&2
}

common::print_error() {
  printf '[ERROR] %s\n' "$1" >&2
}

common::repo_root_from() {
  local source_path="${1:-}"
  local current_dir
  local parent_dir

  if [[ -z "$source_path" ]]; then
    common::print_error 'common::repo_root_from requires a script path'
    return 1
  fi

  if [[ -d "$source_path" ]]; then
    current_dir="$(cd "$source_path" && pwd)"
  else
    current_dir="$(cd "$(dirname "$source_path")" && pwd)"
  fi

  while true; do
    if [[ -f "$current_dir/pom.xml" ]]; then
      printf '%s\n' "$current_dir"
      return 0
    fi

    parent_dir="$(cd "$current_dir/.." && pwd)"
    if [[ "$parent_dir" == "$current_dir" ]]; then
      common::print_error "Could not resolve repository root from: $source_path"
      return 1
    fi

    current_dir="$parent_dir"
  done
}

common::os_name() {
  uname -s
}

common::is_windows_bash() {
  case "$(common::os_name)" in
    MINGW*|MSYS*|CYGWIN*) return 0 ;;
    *) return 1 ;;
  esac
}
