# API DTOs

## Overview

The API DTO package contains the immutable response records used by the REST layer. The DTOs expose transport-friendly scalar values rather than domain value objects.

## Components

- `HealthResponse`: service status and service name.
- `ImportResponse`: dataset name, input row count, stored row count, and deduplicated row count.
- `ReviewResponse`: review identity, dates, status, counts, members, constituents, decisions, rejected securities, and audit events.
- `ConstituentResponse`: security ID, rank, FFMCAP, raw/final weights, capping factor, decision details, and capped flag.
- `DecisionResponse` and `RejectedSecurityResponse`: security-level outcome and reason.
- `AuditEventResponse`: timestamp, rule code, security ID, message, input value, and output value.
- `ErrorResponse`: timestamp, HTTP status, error code, message/details, and validation errors.
- `ValidationErrorResponse`: code, field, message, optional security ID, and severity (`ERROR` or `WARNING`).

## Notes

- These records are used by the REST layer and `GlobalExceptionHandler`.

