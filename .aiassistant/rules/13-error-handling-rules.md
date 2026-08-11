# Error Handling Rules

- Use the existing `DataImportException`, `ReviewValidationException`, `ReviewExecutionException`, and `ResourceNotFoundException` categories where appropriate.
- Translate exceptions centrally in `GlobalExceptionHandler` into stable error codes and HTTP statuses.
- Preserve causes when wrapping failures. Never catch an exception merely to return `null` or continue with incomplete input.
- Distinguish malformed CSV/import data, blocking structural validation, security-level rejection warnings, missing results, persistence failures, and unexpected failures.
- Do not expose stack traces, SQL details, file contents, credentials, or internal implementation data in responses.
- Rejected securities should remain explainable when a review can safely complete; structural failures should stop execution.
