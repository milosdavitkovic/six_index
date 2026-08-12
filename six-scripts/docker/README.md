Docker-check helper
===================

Purpose
-------
Small helper script to verify Docker CLI and daemon availability from developer shells.

Usage
-----
Run from Git Bash / Linux / macOS:

```bash
bash six-scripts/docker/docker-check.sh
```


Verbose / debug mode
--------------------
To enable verbose diagnostic output (prints PATH, shell detection and locations used), set either the environment variable or the flag:

```bash
DOCKER_CHECK_VERBOSE=1 bash six-scripts/docker/docker-check.sh
# or
bash six-scripts/docker/docker-check.sh --verbose
```

CI mode
-------
When running in non-interactive CI or IDE environments where starting containers is undesired or impossible, pass `--ci` to skip interactive run checks (hello-world and nginx examples):

```bash
bash six-scripts/docker/docker-check.sh --ci
# or combined with verbose
DOCKER_CHECK_VERBOSE=1 bash six-scripts/docker/docker-check.sh --ci
```

Notes for non-interactive environments (IDE/CI)
---------------------------------------------
- Some IDE launchers or CI runners provide a different PATH than your interactive shell. If the script fails with "Docker CLI not found in PATH", print diagnostics:

```bash
echo "PATH=$PATH"
command -v docker || cmd.exe /c where docker || true
```

- On Windows + Git Bash the script will attempt a fallback using `cmd.exe /c where docker` when `command -v` fails.
- Ensure shell scripts in the repo are checked out with LF line endings; CRLF can break POSIX shells. Add `.gitattributes` entry if necessary.

If you want me to add automatic PATH augmentation for common Windows Docker install locations, I can add that as an optional feature.

