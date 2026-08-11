# `six-scripts/java-spring`

## Purpose

This folder contains the Maven/Spring Boot verification script for the SIX index calculation
application.

## Contents

```text
six-scripts/java-spring/
├── README.md
└── verify-java-app.sh
```

## `verify-java-app.sh`

Run it from the repository root or from any other directory:

```bash
bash six-scripts/java-spring/verify-java-app.sh
```

The script locates the repository by walking up to `pom.xml`, selects the Maven wrapper, and checks:

- Maven `clean`;
- Maven `install` with tests enabled;
- Surefire reports and test failures;
- JaCoCo line coverage, when configured; and
- the deployable Spring Boot JAR in `target/`.

Maven selection is:

- `mvnw.cmd` on Windows Git Bash;
- `mvnw` on Linux/macOS Bash; or
- `mvn` as a fallback when neither wrapper exists.

The script stores Maven output in `target/verify-java-app-maven.log`.

Coverage is optional because this project does not currently configure JaCoCo. The default threshold
is 80% when a report is present. Use `REQUIRE_COVERAGE=true` to require a report and `MIN_COVERAGE`
to change the threshold.

## Project context

The script verifies the Maven project described by the root `pom.xml` (`six.indices:calculation`).
Application code is primarily under `src/main/java/com/six/indexreview/`, with tests under
`src/test/java/`.

For business and test documentation, see [`../../docs/`](../../docs/).
