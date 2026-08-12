# Documentation Rules

- Keep `docs/architecture.md`, `docs/business-rules.md`, `docs/api-examples.md`, and `docs/testing-strategy.md` aligned with implementation.
- Document changes to methodology, configuration, API DTOs/routes, CSV formats, validation, persistence, audit output, and deterministic invariants in the appropriate existing document.
- Use actual package boundaries: `api`, `application`, `domain`, `infrastructure`, `reporting`, and `validation`.
- Include examples runnable against this service and avoid unavailable integrations or deployment systems.
- Distinguish enabled SMI behavior from disabled/future configuration such as SMIM.
