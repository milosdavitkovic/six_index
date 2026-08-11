# Application layer

Coordinates use cases without putting HTTP or persistence concerns into the domain.

`IndexReviewService` resolves an enabled index configuration, loads the review snapshot, executes the domain engine, persists the result, and assembles `ReviewResponse` data. It also serves the latest result, a result by ID, and security-level audit history.

`CsvImportService` reads the three supported datasets, rejects malformed or conflicting duplicates, replaces the corresponding imported dataset, creates missing security records, and returns import statistics. `importAll` runs the SPI universe, security data, and default composition imports as one workflow.

The application exceptions are mapped at the API boundary: missing resources (`404`), import/request errors (`400`), validation errors (`422`), execution and persistence failures (`500`).
