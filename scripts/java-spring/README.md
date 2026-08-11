# `scripts/java-spring`

## Purpose

This folder contains the Java/Maven verification helper script for the index calculation application.

For this repository, the main entry point is:

- `verify-java-app.sh`

Use it for a focused validation of the Java 21 / Spring Boot 3 application.

## Folder contents

```text
scripts/java-spring/
├── verify-java-app.sh
└── README.md
```

## Main script

### `verify-java-app.sh`

Current behavior verified from the script:

- resolves the repository root automatically
- runs:
  - `clean`
  - `install` (compile, unit tests, and package)
  - Surefire test-report validation
  - optional JaCoCo coverage validation
  - deployable Spring Boot JAR validation

The script selects the available Maven entry point:

- `mvnw.cmd` on Windows Git Bash
- `mvnw` on other Bash environments

Example:

```bash
bash scripts/java-spring/verify-java-app.sh
```

## How this folder fits the project

This repository is Maven-first:

- `pom.xml` is the source of truth for the build
- `mvnw` and `mvnw.cmd` are the supported entry points
- the committed `package.json` is not the main build system

The verification script is useful after changes in areas such as:

- `src/main/java/com/six/indexreview/api/`
- `src/main/java/com/six/indexreview/application/`
- `src/main/java/com/six/indexreview/domain/`
- `src/main/resources/`

## Related workflows

### Local compile and test without the helper

```bash
./mvnw.cmd clean compile
./mvnw.cmd test
./mvnw.cmd clean package
```

## Important project conventions

- Prefer the Maven wrapper over a machine-global Maven installation.
- Run the script from Git Bash on Windows when following the repository’s documented local workflow.
- The repository does not currently configure JaCoCo. Set `REQUIRE_COVERAGE=true` when coverage is mandatory.

## Related documentation

- Repository overview: `README.md`
- Test layout: `src/test/`

## Minimal validation checklist

- Confirm `pom.xml` changes still build through the Maven wrapper.
- Run the script and verify all Maven phases complete successfully.
- If application behavior changed, follow with a targeted integration or endpoint check.
