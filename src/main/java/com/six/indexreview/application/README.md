# Application layer

The application layer coordinates use cases and transaction boundaries without mixing HTTP, persistence, or reporting concerns into the domain model.

## Overview

- resolve the configured index definition and review period
- load the review snapshot for a run
- execute the deterministic domain engine
- persist review results and audit events
- assemble API-ready responses from domain results
- manage CSV imports as transactional workflows

## Components

### `IndexReviewService`

`IndexReviewService` is the main orchestration service for review execution. It:

- resolves an enabled `IndexDefinition` through `IndexDefinitionProvider`
- loads the review snapshot with `ReviewDataLoader`
- creates an `IndexReviewContext` and runs `IndexReviewEngine`
- persists the resulting aggregate through the JPA repositories
- converts the saved result back to a domain model and then to `ReviewResponse`

It also exposes read-only use cases for:

- the latest review result for an index/review period
- a review result by ID
- security-level audit events for a stored review result

### `CsvImportService`

`CsvImportService` owns the import workflow for the three supported datasets:

- SPI universe
- security data
- index composition

It reads BOM-tolerant, header-driven CSV uploads, rejects malformed files, and normalizes duplicates so that:

- conflicting duplicates fail fast with `DataImportException`
- identical duplicates are deduplicated deterministically
- imported rows replace the corresponding stored dataset transactionally
- missing securities are created on demand

`importAll` chains the SPI universe, security data, and default composition imports inside one application transaction.

### `ReviewResultAssembler`

`ReviewResultAssembler` converts `ReviewResult` into the API DTOs used by the controller layer. It delegates report shaping to `ReviewReportGenerator` so the response stays stable and deterministic.

## Notes

Application-level failures are translated at the API boundary via `GlobalExceptionHandler`:

- `ResourceNotFoundException` → `404`
- `DataImportException` and other request/import failures → `400`
- `ReviewValidationException` → `422`
- `ReviewExecutionException` and persistence failures → `500`

See `README.md` in `application/exception` for the exception catalog.
