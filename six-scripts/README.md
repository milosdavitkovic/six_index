# `six-scripts`

Project-specific helper scripts for the SIX index calculation application.

The folder currently provides a focused Maven/Spring Boot verification workflow. It does not contain
AWS, Helm, Docker, Tekton, or branch-management helpers.

## Contents

```text
six-scripts/
├── java-spring/
│   ├── README.md
│   └── verify-java-app.sh
└── lib/
    ├── README.md
    ├── common.sh
    └── maven-wrapper.sh
```

## Verify the application

Run the verifier from the repository root, or invoke it with its path from any directory:

```bash
bash six-scripts/java-spring/verify-java-app.sh
```

The script resolves this repository from `pom.xml` and then:

1. runs Maven `clean`;
2. runs Maven `install`, including compilation, tests, packaging, and the Spring Boot repackage step;
3. checks that Surefire produced reports and that tests ran without failures;
4. checks JaCoCo line coverage when the project produces a report; and
5. checks that a deployable JAR exists in `target/`.

The Maven wrapper is preferred. On Windows Git Bash the script uses `mvnw.cmd`; otherwise it uses
`mvnw`, falling back to `mvn` only when no wrapper is available.

The verifier writes Maven output to `target/verify-java-app-maven.log`. JaCoCo is optional by default;
set `REQUIRE_COVERAGE=true` to fail when the project does not produce a JaCoCo report, or set
`MIN_COVERAGE` to change the required percentage.

## Shared libraries

The files in `lib/` are internal support libraries:

- `common.sh` contains repository-root discovery, Windows Bash detection, and consistent status output.
- `maven-wrapper.sh` selects the repository's Maven wrapper and exposes `run_maven` for future scripts
  in this folder.

Keep shared code here only when it supports the six-index scripts. Do not add helpers for unrelated
repositories or workflows.

## Project conventions

- The repository is Maven-based (`pom.xml`, `mvnw`, and `mvnw.cmd`).
- The application uses Java 21 and Spring Boot.
- The Java package roots are `com.six.indexreview` and `six.indices.calculation`.
- Build artifacts and verification logs are written under `target/`.

## Related documentation

- [`java-spring/README.md`](java-spring/README.md) — verifier details
- [`lib/README.md`](lib/README.md) — shared shell library details
- [`../docs/`](../docs/) — application architecture, business rules, API examples, and testing strategy
