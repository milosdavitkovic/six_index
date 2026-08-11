# Testing Rules

- New or changed business behavior requires tests for the happy path, validation/negative paths, and relevant external failures.
- Prefer focused unit tests for facades and services, MVC slice tests for controllers, and integration tests for S3/Kafka/configuration boundaries.
- Do not mock every collaborator by default; use realistic DTOs and test the boundary that matters.
- Test idempotency, duplicate hot-folder artifacts, retry behavior, and batch/background routing when those paths change.
- Use JUnit 5, Mockito, Spring Boot Test, and AssertJ in the repository's existing style. Do not claim coverage targets without measuring them.
- Test contracts and APIM-visible response/error shapes when an API changes.

✅ Good

```java
@Test
void shouldRoutePrintRequestToExistingS3Document() {
    when(s3Facade.createPresignedUrl(existingKey)).thenReturn(url);

    facade.process(printRequest);

    verify(kafkaFacade).send(argThat(event -> url.equals(event.documentUrl())));
    verify(pdfFacade, never()).createPdf(any());
}
```

❌ Bad

```java
@Test
void test() {
}
```

