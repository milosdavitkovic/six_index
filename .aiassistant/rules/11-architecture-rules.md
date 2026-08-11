# Architecture Rules

- `api` owns HTTP routes, multipart handling, status codes, and API DTOs.
- `application` loads data, resolves configured index definitions, runs reviews, persists results, and assembles responses.
- `domain` contains framework-light models, rules, engines, and calculation services. Rules must not access repositories.
- `infrastructure.csv` parses input; `infrastructure.persistence` owns JPA entities, repositories, and mappings.
- `reporting` converts domain results into stable response/report DTOs; `validation` owns structural and security-level validation.

Preserve the explicit ordered pipeline in `IndexReviewEngine`, immutable review snapshots after insertion, and ordered decisions/audit events. When changing methodology, inspect the properties, definition provider, rule pipeline, docs, and tests together. Do not hardcode counts, caps, dates, or index-specific behavior that belongs in configuration.
