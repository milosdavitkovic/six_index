#!/usr/bin/env bash
# Simple interactive Docker check script for developers
# Usage: bash six-scripts/docker/docker-check.sh

set -euo pipefail

# Defaults
VERBOSE=${DOCKER_CHECK_VERBOSE:-0}
CI_MODE=0

dbg() { if [ "${VERBOSE}" != "0" ]; then printf '[DBG] %s\n' "$*"; fi }

# Simple CLI flag parsing (supports --verbose and --ci)
while [ "$#" -gt 0 ]; do
  case "$1" in
    --verbose|-v)
      VERBOSE=1
      shift
      ;;
    --ci)
      CI_MODE=1
      shift
      ;;
    --)
      shift
      break
      ;;
    *)
      # unknown positional arg: break to allow other scripts to forward
      break
      ;;
  esac
done

dbg "CLI args parsed: VERBOSE=${VERBOSE}, CI_MODE=${CI_MODE}, remaining_args=$*"

print_info() { printf '[INFO] %s\n' "$*"; }
print_warn() { printf '[WARN] %s\n' "$*" >&2; }
print_error() { printf '[ERROR] %s\n' "$*" >&2; }

check_docker_version() {
  print_info 'Checking docker version (client + server)...'

  dbg "SHELL=${SHELL:-unknown}"
  dbg "uname=$(uname -s 2>/dev/null || true)"
  dbg "PATH=${PATH}"

  # 1) PATH-level check
  if ! command -v docker >/dev/null 2>&1; then
    dbg 'command -v docker: not found'
    # On MSYS/Git Bash try cmd.exe where as a fallback
    if uname -s 2>/dev/null | grep -Ei 'mingw|msys|cygwin' >/dev/null 2>&1; then
      dbg 'Detected MSYS/Git Bash environment; trying cmd.exe where docker'
      if cmd.exe /c where docker >/dev/null 2>&1; then
        print_info 'docker binary found via cmd.exe where; proceeding to runtime check.'
      else
        print_error 'Docker CLI not found in PATH. Ensure Docker Desktop or Docker Engine is installed and "docker" is available in your shell.'
        return 1
      fi
    else
      print_error 'Docker CLI not found in PATH. Ensure Docker Desktop or Docker Engine is installed and "docker" is available in your shell.'
      return 1
    fi
  else
    dbg "command -v docker: $(command -v docker 2>/dev/null)"
  fi

  # 2) Run-time check: make sure the binary is runnable. Use plain `docker version` without flags.
  if ! docker version >/dev/null 2>&1; then
    print_error 'docker is found in PATH but `docker version` failed. Ensure the docker CLI is runnable in this environment.'
    # show the raw output to help debugging
    docker version || true
    return 1
  fi

  # Show full docker version output for user inspection
  docker version || true
}

check_docker_info() {
  print_info 'Checking docker info (connectivity to daemon)...'
  # Windows-specific helper: if running under Git Bash / MSYS / Cygwin, try to detect Docker Desktop process
  if uname -s 2>/dev/null | grep -Ei 'mingw|msys|cygwin' >/dev/null 2>&1; then
    # tasklist.exe is available on Windows; check common Docker Desktop process names
    if command -v tasklist.exe >/dev/null 2>&1; then
      if ! tasklist.exe /FI "IMAGENAME eq Docker Desktop.exe" 2>/dev/null | grep -i "Docker Desktop.exe" >/dev/null 2>&1 && \
         ! tasklist.exe /FI "IMAGENAME eq com.docker.backend.exe" 2>/dev/null | grep -i "com.docker.backend.exe" >/dev/null 2>&1; then
        print_warn 'Docker Desktop process not found via tasklist.exe. If you use Docker Desktop on Windows, please start it.'
      else
        print_info 'Docker Desktop process appears to be running (Windows).'
      fi
    fi
  fi

  if ! docker info >/dev/null 2>&1; then
    print_error 'Cannot contact Docker daemon. Is Docker Desktop (Windows) or Docker Engine running? Start Docker Desktop or the daemon and try again.'
    return 1
  fi
  docker info | sed -n '1,120p'
}

run_hello_world() {
  print_info 'Running hello-world container to verify run capability...'
  if [ "${CI_MODE}" = "1" ]; then
    dbg 'CI mode enabled: skipping hello-world run'
    return 0
  fi

  if ! docker run --rm hello-world; then
    print_error 'Failed to run hello-world container.'
    return 1
  fi
}

run_nginx_example() {
  print_info 'Starting nginx example container in detached mode (my-nginx -> port 8080:80)'
  docker rm -f my-nginx >/dev/null 2>&1 || true
  if [ "${CI_MODE}" = "1" ]; then
    dbg 'CI mode enabled: skipping nginx run'
    return 0
  fi

  if ! docker run -d --name my-nginx -p 8080:80 nginx >/dev/null; then
    print_error 'Failed to start nginx container.'
    return 1
  fi
  print_info 'nginx started. Validate with: docker ps  and open http://localhost:8080'
}

inspect_nginx() {
  print_info 'Inspecting nginx logs (recent lines):'
  docker logs --tail 50 my-nginx || true
  print_info 'To open a shell inside the container run: docker exec -it my-nginx sh (or bash if available)'
}

cleanup_nginx() {
  print_info 'Stopping and removing my-nginx (if present)'
  docker stop my-nginx >/dev/null 2>&1 || true
  docker rm my-nginx >/dev/null 2>&1 || true
}

print_guidance() {
  cat <<'EOF'
Quick manual commands (copy-paste):

# 1. Verify Docker is running
docker version    # should show Client and Server
docker info       # detailed daemon info

# 2. Test with hello-world
docker run hello-world

# 3. Pull and run nginx on port 8080
docker run -d --name my-nginx -p 8080:80 nginx
docker ps
open http://localhost:8080

# 4. Inspect container
docker logs my-nginx
docker exec -it my-nginx sh

# 5. Stop and remove
docker stop my-nginx
docker rm my-nginx
docker rmi nginx

# 6. Build and run your app (if you have a Dockerfile)
docker build -t my-app .
docker run -d -p 8080:8080 --name my-app my-app

# 7. If using Docker Desktop + WSL/contexts
docker context ls

EOF
}

main() {
  print_info 'Docker local verification script started.'

  if ! check_docker_version; then
	exit 2
  fi

  if ! check_docker_info; then
	exit 3
  fi

  # Attempt lightweight run checks but continue on non-fatal failures
  if ! run_hello_world; then
	print_warn 'hello-world check failed; you can still try other checks manually.'
  fi

  if ! run_nginx_example; then
	print_warn 'nginx example failed; skipping inspect and cleanup.'
	print_guidance
	exit 0
  fi

  inspect_nginx

  print_info 'Cleaning up nginx example container.'
  cleanup_nginx

  print_info 'All automated checks finished. See guidance below for manual commands.'
  print_guidance
}

main "$@"



