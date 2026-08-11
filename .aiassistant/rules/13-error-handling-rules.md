# Error Handling Rules

- Use meaningful application or integration exceptions and preserve the original cause.
- Centralize HTTP translation with `@RestControllerAdvice` (or the repository's existing equivalent).
- Return stable error codes/messages suitable for callers; hide stack traces, credentials, URLs, and implementation details.
- Never catch `Exception` merely to continue or return `null`. Catch only when translating, compensating, or applying an established retry policy.
- Treat S3, Kafka, PDF conversion, and hot-folder failures as observable failures. Do not silently swallow them.
- Distinguish validation, unsupported flow, external-system, and unexpected failures.

✅ Good

```java
@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(DocumentProcessingException.class)
    ProblemDetail handle(DocumentProcessingException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
        problem.setTitle("Document processing failed");
        problem.setProperty("code", "DOCSTORE_EXTERNAL_FAILURE");
        return problem;
    }
}
```

❌ Bad

```java
try {
    facade.process(request);
} catch (Exception ex) {
    log.error("failed", ex);
    return null;
}
```

