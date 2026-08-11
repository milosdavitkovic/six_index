# Logging Rules

- Use the class's established Lombok or SLF4J logger and parameterized messages.
- Include a correlation/request identifier and safe business identifiers where available (for example SAP outbound ID, flow, topic, partition, or S3 operation).
- Log business milestones at INFO, diagnostic detail at DEBUG, and failures at ERROR with the cause.
- Never log passwords, tokens, credentials, authorization headers, full documents, HTML, base64 payloads, or unnecessary personal data.
- Avoid logging the same failure at every layer; add context when rethrowing.
- Prefer the repository's structured logging/JSON configuration when available; do not invent a second logging format.

✅ Good

```java
log.info("HTA+ flow completed. correlationId={}, flow={}, outboundId={}",
        correlationId, flow, outboundId);
```

❌ Bad

```java
log.info("Request={}", request);
log.info("Authorization={}", authorizationHeader);
```

