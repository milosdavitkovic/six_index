# Testing Rules

- Every changed business rule needs focused tests for normal, boundary, invalid, and deterministic behavior.
- Prefer unit tests for domain rules/services, CSV readers, validation, ranking, selection, weights, capping, and audit creation.
- Use Spring Boot/H2 integration tests for imports, persistence mappings, orchestration, and API contracts.
- Cover duplicate/conflicting CSV rows, missing market data, validation, joiner/leaver decisions, buffers, rounding residuals, impossible caps, and tie-breakers when relevant.
- Assert selected count, positive selected FFMCAP, exact output-scale weight sum, cap compliance, complete explanations, and stable ordering.
- Run `./mvnw.cmd -s .mvn/settings-central.xml test` for repository-level verification.
