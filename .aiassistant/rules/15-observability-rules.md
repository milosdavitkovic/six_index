# Observability Rules

- Use the repository's Micrometer configuration for counters, timers, and failure counts; do not use logs as metrics.
- Expose and secure the actuator/Prometheus endpoints only when enabled by the application and deployment configuration.
- Propagate correlation identifiers through controller, facade, hot-folder processing, S3 operations, and Kafka metadata/payloads where the contract supports it.
- Instrument meaningful outcomes: accepted requests, each HTA+ flow, PDF conversion failures, hot-folder backlog, S3 failures, Kafka send failures, and processing duration.
- Health checks must represent real readiness dependencies. Do not claim S3/Kafka readiness unless the check is safe, bounded, and configured for the deployment.
- Keep metric names low-cardinality; never use document IDs, outbound IDs, or raw URLs as metric tags.
- Review OpenShift probes and resource settings alongside observability changes.

✅ Good

```java
Timer.Sample sample = Timer.start(registry);
try {
    facade.process(request);
    registry.counter("docstore.htaplus.completed", "flow", flow).increment();
} finally {
    sample.stop(registry.timer("docstore.htaplus.duration", "flow", flow));
}
```

❌ Bad

```java
log.info("Uploaded document count = {}", documents.size());
registry.counter("upload", "documentId", documentId.toString()).increment();
```

