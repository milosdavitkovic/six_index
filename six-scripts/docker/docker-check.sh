#!/usr/bin/env bash
# Simple interactive Docker check script for developers
# Usage: bash six-scripts/docker/docker-check.sh

set -euo pipefail

print_info() { printf '[INFO] %s\n' "$*"; }
print_warn() { printf '[WARN] %s\n' "$*" >&2; }
print_error() { printf '[ERROR] %s\n' "$*" >&2; }

check_docker_version() {
  print_info 'Checking docker version (client + server)...'
  if ! docker version --format '{{.Client.Version}}' >/dev/null 2>&1; then
	print_error 'Docker CLI not found in PATH. Ensure Docker Desktop or Docker Engine is installed and "docker" is available in your shell.'
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
  if ! docker run --rm hello-world; then
	print_error 'Failed to run hello-world container.'
	return 1
  fi
}

run_nginx_example() {
  print_info 'Starting nginx example container in detached mode (my-nginx -> port 8080:80)'
  docker rm -f my-nginx >/dev/null 2>&1 || true
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



