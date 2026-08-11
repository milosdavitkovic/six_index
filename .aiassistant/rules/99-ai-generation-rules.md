# AI Code Generation Rules

When generating or modifying code:

- Follow the nearest existing implementation and package convention before introducing abstractions or frameworks.
- Keep the MVC `occ` → `facade` → `core` flow; use constructor injection and existing custom stereotypes.
- Use Java 21 and Spring Boot 3-compatible code. Do not introduce reactive patterns into the established MVC path.
- Use DTOs and validation at API boundaries; use `ProblemDetail` or the existing centralized error contract.
- Preserve HTA+ backward compatibility, flow routing, hot-folder processing, batch chunking, APIM assets, and stage-driven configuration.
- Use AWS SDK v2 through existing S3 services and producer-only Kafka abstractions.
- Add focused tests for changed business behavior and external failure paths.
- Use parameterized, safe logging with correlation context; never log secrets, tokens, full documents, HTML, base64 data, or unnecessary personal data.
- Consider security, idempotency, observability, resource pressure, retry behavior, and rollback before finalizing code.
- Do not invent JPA repositories, WebFlux handlers, metrics endpoints, authorization schemes, or dependencies as universal requirements; verify the repository and deployment first.
- Do not hardcode secrets, URLs, buckets, topics, credentials, environment names, or account-specific values.
- Report assumptions, exact files changed, validation performed, and any contract or deployment impact.

✅ Good

```text
Before changing PDF delivery, inspect the existing facade, PDF service, S3 abstraction,
Kafka mapping, properties, and hot-folder utility; then make the smallest compatible change.
```

❌ Bad

```text
Create a new reactive controller, JPA repository, and direct S3 client because they are
common Spring Boot patterns.
```

