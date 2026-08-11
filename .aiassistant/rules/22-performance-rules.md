# Performance Rules

- Preserve the service's sequential/chunked bias and small executors. Consider `SystemService.canCreateThreadSafely()` before asynchronous work.
- Avoid loading full HTML, base64 documents, or large batch payloads into memory more than necessary.
- Keep hot-folder scans bounded and idempotent; avoid duplicate conversion, upload, or Kafka publication.
- Use timeouts and bounded retries for S3, URL fetches, PDF conversion, and Kafka sends.
- Respect the Spring Batch parameter workaround: serialized input is chunked to approximately 2450 characters.
- Measure processing duration, failure rate, and backlog before optimizing. Do not add high-cardinality metrics.
- Preserve the configured Java/Playwright/Puppeteer conversion choice and resource limits.

✅ Good

```java
if (systemService.canCreateThreadSafely()) {
    batchFacade.submitInConfiguredChunks(request);
} else {
    log.warn("Deferring background processing; thread capacity is unavailable");
}
```

❌ Bad

```java
requests.parallelStream().forEach(request -> {
    convertPdf(request);
    uploadToS3(request);
    kafkaService.send(request);
});
```

