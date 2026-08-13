# Contributing

Author: Milos Davitkovic <info@milosdavitkovic.com>
Website: https://www.milosdavitkovic.com/

Thank you for contributing to six_index. This file contains guidelines for contributors and maintainers.

Start here
- Read `AGENTS.md` for architecture rules and important implementation patterns. The AGENTS.md is the authoritative source for architecture and development guidelines.
- Read `README.md` for quickstart and high-level project overview.

Build & test
- Use the Maven Wrapper included in this repo:

  - Linux/macOS: `./mvnw` or `./mvnw test`
  - Windows (PowerShell/cmd): `.\mvnw.cmd` or `.\mvnw.cmd test`

- Do NOT run the system `mvn` unless explicitly requested — the wrapper ensures consistent Maven version across developers and CI.

Making changes
- Keep HTTP code in `src/main/java/com/six/indexreview/api`.
- Keep orchestration in `application`, domain logic in `domain`, and adapters in `infrastructure`.
- When changing review math, add unit tests in the same module and run the full test suite.

Docs and READMEs
- Per-package READMEs should be maintained; see `src/main/java/com/six/indexreview/api/README.md`, `.../application/README.md`, and `.../domain/rule/README.md` for examples.

Reporting issues
- For security or production incidents, tag `MAINTAINERS` and notify the `build-team` and `infra-team`.

Style
- Follow the project's checkstyle configuration included in `checkstyle.xml`.

Submitting PRs
- Add a clear description of the change and reference relevant docs.
- Update `MAINTAINERS.md` if you change ownership of a component.
- Use the provided pull request template where available.

