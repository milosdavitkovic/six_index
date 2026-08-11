# Java Development

- Follow the existing `occ` → `facade` → `core` layering.
- Use constructor injection and the repository's custom stereotypes where applicable.
- Keep controllers thin; DTO mapping and orchestration belong in facades.
- Match existing Lombok and parameterized SLF4J logging conventions.
- Use the Maven wrapper and Java 21 with preview-enabled compiler settings.
- Do not introduce WebFlux-first code into the existing MVC request path without a clear reason.
