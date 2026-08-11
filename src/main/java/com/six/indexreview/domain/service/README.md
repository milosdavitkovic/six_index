# Domain services

The domain service package contains reusable calculations and deterministic helpers used by the review rules.

## Components

- `FreeFloatMarketCapCalculator`: calculates price × shares × free-float.
- `DeterministicRanker`: orders eligible securities by FFMCAP and configured tie-breakers.
- `ConstituentSelector`: selects the configured number of ranked securities.
- `WeightCalculator`: derives raw weights from selected FFMCAP totals.
- `WeightCapper`: abstraction for final-weight capping; the engine uses the iterative implementation.
- `PrecisionPolicy`: applies configured internal/output scales and rounding mode.
- `AuditEventFactory`: creates timestamped audit events using the injected clock.

## Notes

- These services are deterministic and avoid repository access.

