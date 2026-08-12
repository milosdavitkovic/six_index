# Code Review Rules

Review changes for:

- configured index methodology and ordered rule-pipeline correctness;
- deterministic ranking/tie-breaking, BigDecimal precision, rounding, residual assignment, and weight-cap invariants;
- CSV validation, duplicate/conflicting-row behavior, transactional imports, and immutable review snapshots;
- API DTO/status/error compatibility under `/api/import` and `/api/index-reviews`;
- package boundaries and absence of repository access from domain rules;
- security of uploaded input, persistence queries, logs, and error responses;
- tests, docs, configuration changes, and Java 21/Spring Boot compatibility.

Ask whether invalid data fails visibly, every considered security remains explainable, results are reproducible, and the change can be tested without external services.
