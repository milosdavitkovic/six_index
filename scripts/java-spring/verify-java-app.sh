#!/usr/bin/env bash
# Verify a Spring Boot/Maven application.
# Run from any directory with: bash scripts/java-spring/verify-java-app.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$PROJECT_ROOT"

MIN_COVERAGE="${MIN_COVERAGE:-80}"
REQUIRE_COVERAGE="${REQUIRE_COVERAGE:-false}"
START_TIME="$(date +%s)"
PASSED=()
TEST_COUNT=0
FAILED_TESTS=0
FAILED_TEST_NAMES=()
COVERAGE="unavailable"
CURRENT_CHECK="initialization"
FAILURE_REASON=""
PACKAGING_CHECKED=false

if [[ -x "$PROJECT_ROOT/mvnw" ]]; then
  MAVEN=("$PROJECT_ROOT/mvnw")
elif [[ -f "$PROJECT_ROOT/mvnw.cmd" ]]; then
  # Git Bash can execute the Windows wrapper; this also keeps the script usable
  # in checkouts where the Unix wrapper is not executable.
  MAVEN=("$PROJECT_ROOT/mvnw.cmd")
elif command -v mvn >/dev/null 2>&1; then
  MAVEN=(mvn)
else
  printf 'ERROR: Maven wrapper or mvn was not found\n' >&2
  exit 1
fi

print_info() { printf '[INFO] %s\n' "$*"; }
print_warn() { printf '[WARN] %s\n' "$*" >&2; }
print_error() { printf '[ERROR] %s\n' "$*" >&2; }

summary() {
  local exit_code=$?
  local elapsed=$(( $(date +%s) - START_TIME ))
  printf '\n============================================================\n'
  printf 'Verification summary (%ss)\n' "$elapsed"
  printf '============================================================\n'
  printf 'Result       : %s\n' "$([[ $exit_code -eq 0 ]] && echo PASSED || echo FAILED)"
  printf 'Checks passed: %s\n' "${#PASSED[@]}"
  printf 'Tests        : %s\n' "$TEST_COUNT"
  printf 'Failed tests : %s\n' "$FAILED_TESTS"
  if (( ${#FAILED_TEST_NAMES[@]} > 0 )); then
    printf 'Failing tests:\n'
    printf '  - %s\n' "${FAILED_TEST_NAMES[@]}"
  fi
  printf 'Coverage     : %s\n' "$COVERAGE"
  if [[ -n "$FAILURE_REASON" ]]; then
    printf 'Failure      : %s\n' "$FAILURE_REASON"
  elif (( exit_code != 0 )); then
    printf "Failure      : check '%s' did not complete successfully\n" "$CURRENT_CHECK"
  fi
  if [[ "$PACKAGING_CHECKED" == true ]]; then
    printf 'Packaging    : deployable Spring Boot JAR validated\n'
  else
    printf 'Packaging    : NOT CHECKED\n'
  fi
  exit "$exit_code"
}
trap summary EXIT

run_check() {
  local label="$1"; shift
  CURRENT_CHECK="$label"
  print_info "[$label] started"
  if ! "$@"; then
    FAILURE_REASON="$label failed"
    return 1
  fi
  PASSED+=("$label")
  print_info "[$label] passed"
}

run_maven_build() {
  local log_file="target/verify-java-app-maven.log"
  mkdir -p target
  print_info "Maven output is also captured in $log_file"
  if "${MAVEN[@]}" -DskipTests=false -DfailIfNoTests=false install 2>&1 | tee "$log_file"; then
    return 0
  fi
  FAILURE_REASON="Maven install failed; inspect $log_file"
  print_error "Maven verification failed. See $log_file and target/surefire-reports."
  return 1
}

check_tests() {
  local files count
  shopt -s nullglob
  files=(target/surefire-reports/TEST-*.xml)
  shopt -u nullglob
  (( ${#files[@]} > 0 )) || { print_error 'No Surefire test reports found'; return 1; }

  count="$(grep -ho 'tests="[0-9]*"' "${files[@]}" | sed 's/[^0-9]//g' | awk '{s += $1} END {print s+0}')"
  TEST_COUNT="$count"
  FAILED_TESTS="$(grep -rho 'failures="[1-9][0-9]*"\|errors="[1-9][0-9]*"' "${files[@]}" | wc -l | tr -d ' ')"
  (( count > 0 )) || { print_error 'No unit tests were executed'; return 1; }
  if (( FAILED_TESTS > 0 )); then
    print_error "$FAILED_TESTS test report(s) contain failures or errors"
    while IFS= read -r failed_test; do
      [[ -n "$failed_test" ]] || continue
      FAILED_TEST_NAMES+=("$failed_test")
    done < <(perl -0777 -ne '
      while (/<testcase\b(.*?)<\/testcase>/sg) {
        my $case = $1;
        next unless $case =~ /<(?:failure|error)\b/s;
        my ($name) = $case =~ /\bname="([^"]+)"/s;
        my ($class) = $case =~ /\bclassname="([^"]+)"/s;
        print "$class#$name\n";
      }
    ' "${files[@]}" | sort -u)
    return 1
  fi
  print_info "Executed $count unit tests"
}

check_coverage() {
  local report="target/site/jacoco/jacoco.xml" line missed covered total percent
  if [[ ! -f "$report" ]]; then
    if [[ "$REQUIRE_COVERAGE" == true ]]; then
      print_error "JaCoCo report missing: $report"
      return 1
    fi
    print_warn 'JaCoCo report not configured; skipping coverage threshold'
    COVERAGE='not configured'
    return 0
  fi
  line="$(perl -0777 -ne 'while (/<counter\b([^>]*)\/?\s*>/sg) { my $a=$1; next unless $a =~ /\btype="LINE"/; my ($m)=$a =~ /\bmissed="([0-9]+)"/; my ($c)=$a =~ /\bcovered="([0-9]+)"/; print "$m $c\n"; }' "$report" | tail -1)"
  read -r missed covered <<< "$line"
  total=$(( ${missed:-0} + ${covered:-0} ))
  (( total > 0 )) || { print_error 'JaCoCo contains no executable lines'; return 1; }
  percent="$(awk -v c="$covered" -v t="$total" 'BEGIN { printf "%.2f", (c/t)*100 }')"
  COVERAGE="${percent}% (${covered}/${total} lines)"
  awk -v c="$covered" -v t="$total" -v min="$MIN_COVERAGE" 'BEGIN { exit ((c/t)*100+0.00001 >= min) ? 0 : 1 }' || {
    print_error "Line coverage ${percent}% is below required ${MIN_COVERAGE}%"
    return 1
  }
  print_info "JaCoCo line coverage: $COVERAGE"
}

check_packaging() {
  PACKAGING_CHECKED=true
  local jars jar
  shopt -s nullglob
  jars=(target/*.jar)
  shopt -u nullglob
  for jar in "${jars[@]}"; do
    [[ "$jar" != *.original.jar ]] && { print_info "Deployable JAR: $(basename "$jar")"; return 0; }
  done
  print_error 'No deployable JAR found in target'
  return 1
}

print_info "Repository: $PROJECT_ROOT"
print_info "Maven command: ${MAVEN[*]}"
print_info "Coverage threshold: ${MIN_COVERAGE}% (enforced only when JaCoCo is configured; set REQUIRE_COVERAGE=true to require it)"
run_check 'Maven clean' "${MAVEN[@]}" clean
run_check 'Maven install (compile, unit tests, package)' run_maven_build
run_check 'Unit-test execution' check_tests
run_check 'Coverage threshold' check_coverage
run_check 'Spring Boot packaging' check_packaging
print_info 'Application verification completed successfully.'
