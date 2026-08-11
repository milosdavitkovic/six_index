# Java Development

- Use Java 21, Spring Boot 3, Maven, and `mvnw.cmd`.
- Follow the existing `api`, `application`, `domain`, `infrastructure`, `reporting`, and `validation` boundaries.
- Prefer constructor injection, records for immutable data, and existing Lombok conventions.
- Keep controllers focused on HTTP and DTO conversion; put orchestration in application services and calculations in domain services/rules.
- Use `BigDecimal` and `PrecisionPolicy` for index and weight calculations. Never use `double` for business arithmetic.
- Preserve the synchronous Spring MVC path and avoid adding frameworks without a repository-backed reason.
