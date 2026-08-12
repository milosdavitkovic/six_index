#!/usr/bin/env bash
# Verify a Spring Boot/Maven application and optionally build/package into Docker.
# Run from any directory with: bash six-scripts/docker/pack-java-app.sh
#
# Examples:
# 1) Run full verification (build, tests, packaging). Docker steps run only if Docker is available:
#
#    bash six-scripts/docker/pack-java-app.sh
#
# 2) Build Docker image and run container locally without pushing to a registry:
#
#    IMAGE_TAG=six-index:local APP_PORT=8080 \
#    HEALTH_PATH=/actuator/health INTEGRATION_PATH=/api/health \
#    bash six-scripts/docker/pack-java-app.sh
#
# 3) Build and push to a local registry (for example running on localhost:5000):
#
#    REGISTRY=localhost:5000 IMAGE_TAG=six-index:local APP_PORT=8080 \
#    HEALTH_PATH=/actuator/health INTEGRATION_PATH=/api/health \
#    bash six-scripts/docker/pack-java-app.sh
#
# 4) Use a custom integration path and port (if your app exposes a different endpoint):
#
#    REGISTRY=localhost:5000 IMAGE_TAG=myapp:1.0 APP_PORT=9090 \
#    HEALTH_PATH=/healthz INTEGRATION_PATH=/api/v1/status \
#    bash six-scripts/docker/pack-java-app.sh
#
# Notes:
# - If REGISTRY is set, the script will tag the image as REGISTRY/IMAGE_TAG and push it.
# - To require Docker presence (fail if missing), modify the script logic or run docker login before executing.
# - The script assumes the Dockerfile is located at the repository root and the app listens on container port 8080.

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
  # Allow skipping report/site generation to avoid repository access for site artifacts
  # Set SKIP_REPORTS=true to add -Dpmd.skip=true and -Dsite.skip=true
  local extra_props=""
  if [[ "${SKIP_REPORTS:-false}" == "true" ]]; then
    extra_props+=" -Dpmd.skip=true -Dsite.skip=true"
    print_warn "SKIP_REPORTS=true -> skipping PMD and site generation (adds: $extra_props)"
  fi
  # Quote the array expansion to preserve elements (wrapper path + args)
  if "${MAVEN[@]}" -DskipTests=false -DfailIfNoTests=false $extra_props install 2>&1 | tee "$log_file"; then
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

# Docker packaging and local run checks
check_docker_available() {
  if ! command -v docker >/dev/null 2>&1; then
    print_warn 'Docker CLI not found; skipping Docker packaging and run checks'
    return 2
  fi
  if ! docker info >/dev/null 2>&1; then
    print_warn 'Docker daemon is not running or not accessible; skipping Docker packaging and run checks'
    return 2
  fi
  return 0
}

# Configurable via env vars:
# IMAGE_TAG (default: six-index:local)
# REGISTRY (optional) -> if set, image will be tagged and pushed to this registry (e.g. localhost:5000)
# APP_PORT (default: 8080)
# HEALTH_PATH (default: /actuator/health)
# INTEGRATION_PATH (default: /api/health)
build_docker_image() {
  local base_tag="${IMAGE_TAG:-six-index:local}"
  local registry="${REGISTRY:-}"
  local image_tag
  if [[ -n "${REGISTRY:-}" ]]; then
    image_tag="${REGISTRY%/}/${base_tag}"
  else
    image_tag="$base_tag"
  fi
  # Keep diagnostics off stdout: callers capture stdout to obtain the image tag.
  print_info "Building Docker image $image_tag" >&2
  if docker build -t "$image_tag" -f Dockerfile . | sed -u 's/^/[docker] /' >&2; then
    if [[ -n "${REGISTRY:-}" ]]; then
      print_info "Pushing image $image_tag to registry ${REGISTRY}" >&2
      if ! docker push "$image_tag" | sed -u 's/^/[docker] /' >&2; then
        print_error "Failed to push image $image_tag to registry ${REGISTRY}" >&2
        return 1
      fi
    fi
    echo "$image_tag"
    return 0
  else
    print_error 'Docker build failed' >&2
    return 1
  fi
}

run_container_and_healthcheck() {
  local image_tag="$1"
  local name="six-index-local-verify"
  local port="${APP_PORT:-8080}"
  local health_path="${HEALTH_PATH:-/actuator/health}"
  local integration_path="${INTEGRATION_PATH:-/api/health}"
  # By default keep the container running so the developer can test with Postman
  # Set KEEP_CONTAINER=false to have the script remove the container after checks
  local inspect_mode="${INSPECT:-false}"
  local keep_container="${KEEP_CONTAINER:-true}"
  # ensure previous container is removed
  docker rm -f "$name" >/dev/null 2>&1 || true
  print_info "Starting container $name from $image_tag"
  # Defensive checks: ensure image_tag is non-empty and contains no whitespace
  print_info "DEBUG: final image_tag='$image_tag'"
  if [[ -z "$image_tag" || "$image_tag" =~ [[:space:]] ]]; then
    print_error "Empty or invalid image tag: '$image_tag'"
    return 1
  fi

  # Stricter validation: validate docker image reference format (basic check)
  is_valid_docker_ref() {
    local ref="$1"
    # Docker reference pattern (simplified): [registry/][name][:tag]
    # registry: optional hostname[:port], name: path components with [-_.a-z0-9], tag: [A-Za-z0-9_.-]+
    # This is a pragmatic check, not a full parser.
    if [[ "$ref" =~ ^([a-zA-Z0-9.-]+(:[0-9]+)?/)?([a-z0-9]+([._-][a-z0-9]+)*/)*[a-z0-9]+([._-][a-z0-9]+)*(:[A-Za-z0-9_.-]+)?$ ]]; then
      return 0
    fi
    return 1
  }

  if ! is_valid_docker_ref "$image_tag"; then
    print_error "Image tag does not match expected docker reference pattern: '$image_tag'"
    # Write the captured tag to target/ for easier debugging in CI artifacts
    mkdir -p target
    echo "$image_tag" > target/last_built_image_tag.txt
    print_info "Wrote captured image tag to target/last_built_image_tag.txt"
    return 1
  fi

  # Emit the final docker run command for debugging (won't include credentials)
  print_info "DEBUG: docker run -d --name \"$name\" -p ${port}:8080 \"$image_tag\""
  if ! docker run -d --name "$name" -p ${port}:8080 "$image_tag" >/dev/null; then
    print_error 'Failed to start Docker container'
    return 1
  fi

  # wait for health endpoint up to 60s
  local max_wait=60
  local waited=0
  while ! curl -sSf --connect-timeout 2 http://localhost:${port}${health_path} >/dev/null 2>&1; do
    sleep 2
    waited=$((waited+2))
    if (( waited >= max_wait )); then
      print_error 'Application did not become healthy within timeout'
      docker logs "$name" --tail 200 || true
      docker rm -f "$name" >/dev/null 2>&1 || true
      return 1
    fi
    print_info "Waiting for application to become healthy... (${waited}s)"
  done

  print_info "Application responded to ${health_path}"

  # Integration test: call a simple API endpoint and expect HTTP 200
  print_info "Running integration request against ${integration_path}"
  if ! curl -sSf --connect-timeout 5 http://localhost:${port}${integration_path} >/dev/null 2>&1; then
    print_error "Integration request to ${integration_path} failed"
    docker logs "$name" --tail 200 || true
    docker rm -f "$name" >/dev/null 2>&1 || true
    return 1
  fi

  print_info 'Integration request succeeded'
  # If inspect mode requested, open a shell into the running container for manual inspection
  if [[ "$inspect_mode" == "true" ]]; then
    print_info "Opening shell into container $name for inspection (CTRL-D to exit). Container will be kept: $keep_container"
    # try bash, fallback to sh
    if docker exec -it "$name" /bin/bash 2>/dev/null; then
      true
    else
      docker exec -it "$name" /bin/sh || true
    fi
  fi

  # stop and remove only when the user explicitly requests not to keep it
  if [[ "$keep_container" != "true" ]]; then
    docker rm -f "$name" >/dev/null 2>&1 || true
  else
    print_info "Keeping container $name for manual inspection (you can test with Postman)"
    print_info "To remove the container run: docker rm -f $name"
  fi
  return 0
}

if check_docker_available; then
  # Build image once and capture the tag for subsequent run/healthcheck.
  # Wrap build_docker_image so run_check records the step as passed/failed
  # while also exposing the produced image tag in IMAGE_TAG_BUILT.
  build_and_capture() {
    local tag
    tag=$(build_docker_image) || return 1
    IMAGE_TAG_BUILT="$tag"
    return 0
  }

  run_check 'Docker image build + optional push' build_and_capture
  image_tag="${IMAGE_TAG_BUILT:-}"
  if [[ -n "$image_tag" ]]; then
    run_check 'Docker run + healthcheck + integration' run_container_and_healthcheck "$image_tag"
  fi
else
  print_warn 'Skipping Docker steps'
fi

# Suggested docker CLI commands for manual verification and debugging
print_suggested_commands() {
  local tag="${IMAGE_TAG:-six-index:local}"
  local registry="${REGISTRY:-}"
  local full_tag
  if [[ -n "$registry" ]]; then
    full_tag="${registry%/}/$tag"
  else
    full_tag="$tag"
  fi

  cat <<EOF
Suggested Docker CLI commands for verification and debugging:

# List images (local)
docker images | grep "${tag%%:*}"

# Run the image locally and map host port ${APP_PORT:-8080} to container 8080
docker run --rm -it -p ${APP_PORT:-8080}:8080 $full_tag

# If you pushed to a local registry, pull and run from registry (example):
docker pull $full_tag
docker run --rm -it -p ${APP_PORT:-8080}:8080 $full_tag

# View recent container logs (replace <container> with name or id):
docker logs --tail 200 <container>

# Inspect image or container metadata:
docker inspect $full_tag
docker inspect <container>

# Remove the image locally:
docker rmi $full_tag

# Example docker-compose.yml for local run (save as docker-compose.yml):
cat <<YAML
version: '3.8'
services:
  six-index:
    image: $full_tag
    ports:
      - "${APP_PORT:-8080}:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=local
YAML

# Start with docker-compose:
docker-compose up --build

EOF
}

# Always print suggested commands at the end to help manual checks
print_suggested_commands
