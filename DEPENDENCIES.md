# DEPENDENCIES

This project uses Maven. The canonical source of pinned dependency versions is `pom.xml` and the parent BOMs it references.

When to update
- Security fixes, CVE patches, or critical bugfixes.
- Test and CI dependency updates should be validated by running `./mvnw -s .mvn/settings-central.xml test` (Windows: `.\mvnw.cmd -s .mvn/settings-central.xml test`).

Maven wrapper policy
- Use the included Maven Wrapper (`./mvnw` / `mvnw.cmd`) for all local and CI runs.
- Do NOT run the system `mvn` unless explicitly requested; this prevents version drift between developers and CI.

Checking for vulnerable dependencies
- Use `mvn dependency:tree` and `mvn dependency:analyze` to inspect transitive dependencies.
- CI may be configured to run dependency checks; see CI configuration if present.

Where to change versions
- Direct dependencies: edit `pom.xml` at the top-level.
- Shared versions/BOMs: check parent POM or `<dependencyManagement>` sections.

If in doubt, open an issue and tag `build-team` and `MAINTAINERS` for guidance.

