# API DTOs

Immutable response records used by the REST layer. They intentionally expose transport-friendly scalar values rather than domain value objects.

## Responses

- `HealthResponse`: service status and service name.
- `ImportResponse`: dataset name, input row count, stored row count, and deduplicated row count.
- `ReviewResponse`: review identity/dates/status, eligible and selected counts, current members, selected constituents, decisions, joiners, leavers, unchanged and not-selected IDs, rejected securities, and audit events.
- `ConstituentResponse`: security ID, rank, FFMCAP, raw/final weights, capping factor, decision details, and capped flag.
- `DecisionResponse` and `RejectedSecurityResponse`: security-level outcome and reason.
- `AuditEventResponse`: timestamp, rule code, security ID, message, input, and output values.

## Errors

`ErrorResponse` contains timestamp, HTTP status, error code, message/details, and validation errors. Each `ValidationErrorResponse` contains code, field, message, optional security ID, and severity (`ERROR` or `WARNING`).
