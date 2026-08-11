# Documentation Rules

For every externally visible or operationally significant feature, document:

- Business purpose and supported HTA+ flow
- Technical design and affected `occ`, `facade`, `core`, hot-folder, S3, Kafka, batch, and configuration paths
- API/Kafka contract, compatibility and migration behavior
- Validation and error scenarios
- Security, observability, deployment, and operational impacts
- Test and rollback strategy

Update the appropriate README, APIM assets, Helm/Tekton configuration, ADR, and runbook rather than creating disconnected notes. Keep documentation factual and aligned with the current implementation.

✅ Good

```markdown
# PDF hot-folder retry

## Purpose
Explain why a generated PDF remains in the resource folder after a transient S3 failure.

## Operational behavior
The scheduled scan retries ready artifacts and emits Kafka only after a successful upload.

## Failure and rollback
Describe alerts, cleanup, safe retry, and the configuration rollback.
```

❌ Bad

```text
Added PDF support.
```

