# Validation

`DataValidationService` checks the review snapshot before calculation: a universe and current composition must exist, composition size must match the configured constituent count, the universe must be large enough, cut-off prices and review-date shares must be positive, and review-date free float must be present in `[0, 1]`.

Validation findings are immutable `ValidationError` values with a code, field, message, optional security ID, and `ValidationSeverity` (`ERROR` or `WARNING`). Structural errors stop the review through `ReviewValidationException`; security-level data warnings allow the eligibility rule to reject affected securities. `ValidationRule` is the extension point for additional validation rules.
