# REST API

Spring MVC adapters for the index-review service. Controllers translate HTTP requests into application-service calls and return the API DTOs from `dto`.

## Overview

- Successful responses are JSON DTOs from `src/main/java/com/six/indexreview/api/dto/README.md`.
- Import endpoints return `201 Created`.
- Review lookup and execution endpoints return `200 OK` on success.
- Missing resources map to `404 NOT_FOUND`.
- Invalid request parameters or unsupported configuration map to `400 INVALID_REQUEST`.
- Review validation failures map to `422 REVIEW_VALIDATION_ERROR` and include structured validation errors.
- Unexpected execution or persistence failures map to `500` responses.

## Endpoints

## `GET /api/health`

Returns a simple readiness-style payload with the application status and service name.

- Response: `HealthResponse`
- Success status: `200 OK`
- Example shape:
  ```json
  {"status":"UP","service":"index-review"}
  ```

## `POST /api/import/spi-universe`

Imports the SPI universe snapshot.

- Multipart field name: `file`
- Expected content: CSV with `;` separators and header-driven columns
- Response: `ImportResponse`
- Success status: `201 Created`
- Notes:
  - Empty, malformed, or conflicting input is rejected as a `400` error.
  - Identical duplicate rows are deduplicated before storage.

## `POST /api/import/security-data`

Imports time-series security market data.

- Multipart field name: `file`
- Expected content: CSV with `;` separators and header-driven columns
- Response: `ImportResponse`
- Success status: `201 Created`
- Notes:
  - The import creates or updates the market-data snapshot used by later reviews.
  - Duplicate handling and error normalization follow the same rules as the other import endpoints.

## `POST /api/import/composition`

Imports the current index composition snapshot.

- Multipart field name: `file`
- Expected content: CSV with `,` or `;` separators depending on the input file
- Response: `ImportResponse`
- Success status: `201 Created`
- Notes:
  - The file identifies the current members used as the review starting point.
  - Conflicting duplicates fail; identical duplicates are deduplicated.

## `POST /api/import/all`

Imports all three supported datasets in one request.

- Multipart field names:
  - `spiUniverse`
  - `securityData`
  - `composition`
- Response: grouped `ImportResponse` payload with one result per dataset
- Success status: `201 Created`
- Notes:
  - Use this endpoint when bootstrapping a full review dataset in one call.
  - The three parts are processed by the application import workflow in a fixed order.

## `POST /api/index-reviews/{indexCode}/{reviewPeriod}/run`

Runs the deterministic review pipeline for a configured index and review period, then persists the result.

- Path variables:
  - `indexCode`: index identifier such as `SMI`
  - `reviewPeriod`: configured period such as `Q3-2026`
- Request body: none
- Response: `ReviewResponse`
- Success status: `200 OK`
- Notes:
  - The index must be enabled and the review period must match a configured index definition.
  - The response includes the review identity, counts, selected constituents, joiners, leavers, rejected securities, and ordered audit events.
  - Validation failures are returned as `422 REVIEW_VALIDATION_ERROR` when the snapshot or calculated output is not acceptable.

## `GET /api/index-reviews/{indexCode}/{reviewPeriod}/latest`

Returns the most recently persisted review for the given index and review period.

- Path variables:
  - `indexCode`: index identifier such as `SMI`
  - `reviewPeriod`: configured period such as `Q3-2026`
- Request body: none
- Response: `ReviewResponse`
- Success status: `200 OK`
- Notes:
  - If no persisted review exists for the pair, the API returns `404 NOT_FOUND`.
  - The lookup is case-insensitive on `indexCode` at the service layer.

## `GET /api/index-reviews/results/{reviewResultId}`

Returns a previously persisted review by numeric identifier.

- Path variables:
  - `reviewResultId`: database identifier of the persisted review
- Request body: none
- Response: `ReviewResponse`
- Success status: `200 OK`
- Notes:
  - Use this endpoint when the client already knows the persisted result ID.
  - Unknown IDs return `404 NOT_FOUND`.

## `GET /api/index-reviews/results/{reviewResultId}/securities/{securityId}/audit`

Returns the ordered audit trail for one security inside a persisted review.

- Path variables:
  - `reviewResultId`: database identifier of the persisted review
  - `securityId`: numeric security identifier
- Request body: none
- Response: `List<AuditEventResponse>`
- Success status: `200 OK`
- Notes:
  - The events are ordered by sequence number ascending.
  - If the review result does not exist, the API returns `404 NOT_FOUND`.
  - If the review exists but the security has no events, the response is an empty JSON array.

## Notes

- `IndexReviewController` contains no business logic; it only forwards requests to `IndexReviewService` and `CsvImportService`.
- `GlobalExceptionHandler` converts application exceptions into the stable JSON error contract used across the API.
- For concrete payload examples, see `docs/api-examples.md`.
