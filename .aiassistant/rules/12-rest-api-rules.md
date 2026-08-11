# REST API Rules

- Keep the established Spring MVC controller path; do not introduce WebFlux-first handlers without an architectural reason.
- Use request and response DTOs at the API boundary. Never expose internal DTOs, SDK objects, entities, or full inbound payloads accidentally.
- Validate request bodies with `@Valid` and bean-validation constraints where the existing contract permits.
- Use meaningful, versioned paths and preserve `/htaplus/pdfSend_V3` compatibility unless a migration plan exists.
- Use POST for command-style HTA+ processing. Define idempotency behavior for retries and duplicate requests.
- Use OpenAPI/APIM documentation for externally visible contract changes.
- Return stable status codes and structured errors; never return stack traces.

✅ Good

```java
@PostMapping("/htaplus/pdfSend_V3")
ResponseEntity<HtaPlusResponse> send(
        @Valid @RequestBody HtaPlusRequest request) {
    return ResponseEntity.ok(facade.process(request));
}
```

❌ Bad

```java
@PostMapping("/sendEverything")
Object send(@RequestBody Map<String, Object> request) {
    return repository.save(request); // leaks persistence and bypasses the facade
}
```

