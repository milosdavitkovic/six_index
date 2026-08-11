# REST API

Spring MVC adapters for the index-review service. Controllers translate HTTP requests into application-service calls and return the API DTOs from `dto`.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/health` | Returns `{"status":"UP","service":"index-review"}`. |
| `POST` | `/api/import/spi-universe` | Imports a multipart `file` containing SPI universe rows. |
| `POST` | `/api/import/security-data` | Imports a multipart `file` containing dated market data. |
| `POST` | `/api/import/composition` | Imports a multipart `file` containing composition IDs. |
| `POST` | `/api/import/all` | Imports multipart parts `spiUniverse`, `securityData`, and `composition`. |
| `POST` | `/api/index-reviews/{indexCode}/{reviewPeriod}/run` | Executes and persists a review. |
| `GET` | `/api/index-reviews/{indexCode}/{reviewPeriod}/latest` | Returns the newest persisted review. |
| `GET` | `/api/index-reviews/results/{reviewResultId}` | Returns a review by ID. |
| `GET` | `/api/index-reviews/results/{reviewResultId}/securities/{securityId}/audit` | Returns ordered audit events for one security. |

Import endpoints respond with `201 Created`; review and health endpoints use the default successful response status. Errors are normalized by `GlobalExceptionHandler`.
