# Architecture Rules

Keep the existing MVC and facade/core boundaries clear. The normal flow is `occ` controller → `facade` orchestration and DTO mapping → `core` services and integrations.

## Boundaries

- Controllers accept/validate requests, delegate, and shape responses.
- Facades coordinate HTA+ flows and map DTOs; they do not expose AWS SDK or Kafka-template details.
- Core services own reusable business/integration operations.
- AWS S3, Kafka, PDF conversion, and file-system operations stay behind the existing service abstractions.
- Domain-style logic should remain independent of Spring where practical; do not force a new domain layer into an existing flow without an explicit reason.
- Prefer composition and focused classes. Split orchestration before a service becomes a god class.
- Preserve email-only, email+PDF, SMS, and print behavior. Print uses an existing S3 object; it does not generate a new PDF.
- Document external-contract decisions in an ADR and review APIM assets when the HTA+ API changes.

✅ Good

```java
@RestController
@RequiredArgsConstructor
class HtaPlusController {
    private final HtaPlusFacade facade;

    @PostMapping("/htaplus/pdfSend_V3")
    HtaPlusResponse send(@Valid @RequestBody HtaPlusRequest request) {
        return facade.process(request);
    }
}
```

❌ Bad

```java
@RestController
class HtaPlusController {
    private final S3Client s3Client;

    @PostMapping("/htaplus/pdfSend_V3")
    Object send(@RequestBody Map<String, Object> request) {
        // generate PDF, write hot-folder files, upload S3, and publish Kafka here
        return request;
    }
}
```

