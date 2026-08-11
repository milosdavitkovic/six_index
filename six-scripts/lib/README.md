# `six-scripts/lib`

Internal Bash libraries shared by the helper scripts for the SIX index calculation application.
These files are not standalone entry points.

## Contents

```text
six-scripts/lib/
├── README.md
├── common.sh
└── maven-wrapper.sh
```

## `common.sh`

Provides the small set of shared functions currently needed by this project:

- consistent informational, warning, and error output;
- repository-root discovery using `pom.xml`; and
- Windows Git Bash detection.

It intentionally contains no AWS, Helm, Docker, Tekton, chart, or other unrelated workflow logic.

## `maven-wrapper.sh`

Sources `common.sh`, resolves the repository root, selects `mvnw.cmd` on Windows Git Bash or `mvnw`
elsewhere, and exposes the `run_maven` function.

## Conventions

- Keep shared functions project-relevant and minimal.
- Preserve repository-root discovery through the root `pom.xml`.
- Test Bash changes with the scripts under `six-scripts/`.

See [`../README.md`](../README.md) for the script overview and [`../java-spring/README.md`](../java-spring/README.md)
for the Maven verification workflow.
