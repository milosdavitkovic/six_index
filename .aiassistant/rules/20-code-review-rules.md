# Code Review Rules

Review every change for:

- Correctness across email-only, email+PDF, SMS, and print flows
- MVC/controller, facade, core, hot-folder, S3, Kafka, and batch boundaries
- Backward compatibility of HTA+, Kafka, APIM, and configuration contracts
- Error handling, idempotency, cleanup, and rollback behavior
- Security, secret/PII exposure, authorization, file/URL validation, and dependency risk
- Logging, correlation, metrics, probes, and operational supportability
- Performance, memory, thread pressure, and bounded external calls
- Tests, documentation, Helm/Tekton/ArgoCD impact, and Java 21/Spring Boot compatibility

Mandatory questions:

- Can this fail? Is the failure visible and recoverable?
- Can this be abused or leak data?
- Can support teams monitor and diagnose it?
- Can it scale within the configured executor and hot-folder limits?
- Can the behavior be tested without real production services?

✅ Good

```text
Review: the PDF change preserves the resource-folder workflow, tests duplicate scans,
records a bounded S3 failure, and leaves Kafka payload compatibility unchanged.
```

❌ Bad

```text
Looks fine; the happy path works locally.
```

