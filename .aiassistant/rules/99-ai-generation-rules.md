# AI Code Generation Rules

Before changing code, inspect the nearest controller/service/rule, its configuration, tests, and the relevant `docs/` file. Make the smallest change that fits the existing design.

- Use Java 21, Spring Boot 3, constructor injection, records/value objects, and existing Lombok conventions.
- Preserve `api` -> `application` -> `domain` -> `infrastructure`/`reporting` boundaries and keep domain rules repository-free.
- Preserve configured index definitions, ordered rule execution, deterministic comparators, audit events, precision policy, and weight invariants.
- Use DTO validation and `GlobalExceptionHandler`; do not expose entities, stack traces, or raw uploaded data.
- Add focused tests for changed behavior, including invalid input and invariant/failure paths.
- Do not invent messaging, object storage, reactive handlers, metrics, deployment systems, credentials, or index methodology values.
- Report assumptions, files changed, tests run, and API/configuration or business-rule impact.
