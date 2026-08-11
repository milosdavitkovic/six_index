# Domain model

The domain model package contains the framework-independent records and enums that describe an index review.

## Components

- `IndexCode`, `IndexDefinition`, `ReviewDates`, and `ReviewSchedule`: review configuration and schedule metadata.
- `Security`, `SecurityId`, `MarketData`, and `CurrentComposition`: imported input values.
- `SelectedConstituent`, `ReviewDecision`, `ReviewResult`, `AuditEvent`, and `RejectedSecurity`: review output values.
- `DecisionType`: distinguishes `JOINER`, `LEAVER`, `UNCHANGED`, and `NOT_SELECTED`.
- `ReviewStatus`: describes review completion.

## Notes

- Value objects validate and normalize their invariants.
- Selected constituents preserve rank, FFMCAP, raw/final weights, capping metadata, and decision information for auditability.
