# Domain layer

## Overview

The domain layer contains the framework-independent review model, the deterministic engine pipeline, the reusable calculation services, and the pluggable business rules. It is the core of the application and should remain free of HTTP, persistence, and infrastructure concerns.

## Components

- `model`: immutable records and enums describing the index review domain
- `engine`: the deterministic review pipeline and mutable execution context
- `service`: reusable calculations and deterministic helpers used by rules
- `rule`: pluggable methodology stages that mutate the execution context in a fixed order

## Notes

`IndexReviewEngine` runs a small, explicit pipeline rather than a general rules framework. The default sequence is:

1. input validation
2. SPI-universe eligibility
3. FFMCAP ranking
4. top-N selection
5. configured buffer handling
6. joiner/leaver decisions
7. FFMCAP weight calculation
8. iterative weight capping and redistribution
9. audit/report completion

`IndexReviewContext` carries the resolved `IndexDefinition`, read-only `ReviewDataSnapshot`, precision policy, execution timestamp, and accumulated eligible, ranked, selected, decision, validation, and audit data. Rules mutate only this context and never access repositories.

`IndexCode`, `IndexDefinition`, `ReviewDates`, and `ReviewSchedule` describe the configured review. `Security`, `SecurityId`, `MarketData`, and `CurrentComposition` describe the imported inputs. `SelectedConstituent`, `ReviewDecision`, `ReviewResult`, `AuditEvent`, and `RejectedSecurity` describe the review output. `DecisionType` distinguishes `JOINER`, `LEAVER`, `UNCHANGED`, and `NOT_SELECTED`, and `ReviewStatus` represents the lifecycle state of a review result.

Selected constituents preserve rank, FFMCAP, raw and final weights, capping metadata, and decision information so the review remains auditable and reproducible.


The domain emphasizes stable ordering, explicit tie-breakers, configured precision, and ordered audit events. FFMCAP, ranking, capping, and audit creation are implemented as deterministic services so the same input and configuration always produce the same review outcome.

## References

- `rule/README.md` for the review stages and rule contracts
- `engine/README.md` for the execution context and pipeline order
- `service/README.md` for calculation helpers
- `model/README.md` for the domain records and enums

