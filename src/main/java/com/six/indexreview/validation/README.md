# Validation

The validation package checks the review snapshot before the engine runs and separates blocking structural failures from security-level warnings.

## Components

- `DataValidationService`: verifies the snapshot contains the required inputs and basic value constraints.
- `ValidationError`: immutable finding with a code, field, message, optional security ID, and severity.
- `ValidationRule`: extension point for additional validation checks.

## Notes

- Structural errors stop the review through `ReviewValidationException`.
- Security-level warnings allow the eligibility rule to reject affected securities while the review continues.
- The validation rules enforce composition size, universe coverage, positive cut-off prices and review-date shares, and review-date free float in `[0, 1]`.
