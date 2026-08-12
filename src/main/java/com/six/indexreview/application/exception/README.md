# Application exceptions

The application exception package contains the use-case failures that cross the application/API boundary.

## Components

- `DataImportException`: empty, malformed, incomplete, or conflicting CSV input.
- `ResourceNotFoundException`: requested review data does not exist.
- `ReviewValidationException`: review input or calculation validation failed and carries structured `ValidationError` values.
- `ReviewExecutionException`: the configured review pipeline cannot execute, for example because a rule is unsupported.

## Notes

- `GlobalExceptionHandler` converts these exceptions into stable error codes and HTTP statuses.
