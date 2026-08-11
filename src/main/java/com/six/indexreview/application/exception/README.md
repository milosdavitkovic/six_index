# Application exceptions

Use-case failures that cross the application/API boundary:

- `DataImportException`: empty, malformed, incomplete, or conflicting CSV input.
- `ResourceNotFoundException`: requested review data does not exist.
- `ReviewValidationException`: review input or calculation validation failed; carries structured `ValidationError` values.
- `ReviewExecutionException`: the configured review pipeline cannot execute, for example because a rule is unsupported.

`GlobalExceptionHandler` converts these exceptions into stable error codes and HTTP statuses.
