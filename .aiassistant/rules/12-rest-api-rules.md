# REST API Rules

- Preserve Spring MVC routes under `/api/health`, `/api/import`, and `/api/index-reviews` unless compatibility impact is deliberate and documented.
- Keep controllers thin and expose API DTOs, never JPA entities or internal domain objects.
- Preserve multipart field names: `file`, `spiUniverse`, `securityData`, and `composition`.
- Use POST for imports and review execution; use GET for health, result lookup, latest results, and audit lookup.
- Map failures through `GlobalExceptionHandler` into the existing `ErrorResponse` and validation shape. Never expose stack traces or persistence details.
- Update `docs/api-examples.md` when a public route, response, or error contract changes.
