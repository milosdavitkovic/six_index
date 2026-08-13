# `six-scripts/java-spring`

## Purpose

This folder contains the Maven/Spring Boot verification script for the SIX index calculation
application.

## Contents

```text
six-scripts/java-spring/
├── README.md
├── reproducible-review.sh
├── reproducible-review.ps1
└── verify-java-app.sh
```

## `reproducible-review.ps1`

Execute this from the repository root:

```powershell
.\six-scripts\java-spring\reproducible-review.ps1
```

It reuses an application already running at the configured URL, or starts
`mvnw spring-boot:run` automatically when no service is available. It then
checks health, imports the three sample CSV files, runs `SMI/Q3-2026`, and
writes the JSON responses to `target/reproducible-review/`. An automatically
started application is stopped when the script finishes. See the root
`README.md` for options such as a custom base URL or output directory.

The Git Bash equivalent is:

```bash
bash six-scripts/java-spring/reproducible-review.sh
```

The Bash runner accepts the same settings through environment variables:

```bash
BASE_URL=http://localhost:8090 OUTPUT_DIRECTORY=target/my-review \
  bash six-scripts/java-spring/reproducible-review.sh
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
