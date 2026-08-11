# Business rules

## Observation dates

For Q3-2026, the cut-off date is 2026-09-10 and the review date is 2026-09-21.

```text
FFMCAP = price(2026-09-10)
       * shares(2026-09-21)
       * freeFloat(2026-09-21)
```

Price on the review date is not used. A missing review-date price is therefore harmless, while a missing cut-off price rejects the security.

## Eligibility

A security must be in the SPI universe dated on the review date and must have a positive cut-off price, positive review-date shares, and review-date free float in `[0, 1]`. The supplied ID 166 is in the universe but lacks review-date data, so it is rejected with a warning. Structural problems, such as an empty universe or a current composition of the wrong configured size, stop the review.

## Ranking and selection

Eligible securities are sorted by FFMCAP descending. For equal FFMCAP, configured `CURRENT_CONSTITUENT_FIRST` is applied, followed by security ID ascending. The selected list is the first configured `constituent-count` items. Every ranked security gets a rank and an audit explanation.

The initial SMI implementation intentionally does not retain an incumbent outside top-N. The resulting decision answers the pure-ranking question: IDs 177, 249, and 28 join; IDs 103, 160, and 81 leave. The buffer rule is separate so a future methodology can answer whether incumbents should remain. For example, enabling `CONFIGURABLE` with retention rank `25` on the supplied data retains IDs 160 and 81 and leaves ID 103 outside the buffer band. This is a configuration/methodology decision, not a change to the review engine.

## Decisions

- `JOINER`: selected and absent from current composition.
- `LEAVER`: current member and not selected; the reason states whether it ranked outside the selected range or was absent from the eligible universe.
- `UNCHANGED`: selected and already current.
- `NOT_SELECTED`: eligible but ranked below the selected range.
- `REJECTED`: considered universe member failing security-level eligibility.

## Weights

For selected constituents:

```text
rawWeight = constituentFFMCAP / sum(selectedFFMCAP)
```

The denominator must be positive and every selected FFMCAP must be positive. Raw division uses the internal scale and configured rounding mode.

## 18% capping

The cap rule starts with raw weights. In each iteration, every uncapped constituent above the maximum is set to the maximum. The excess is redistributed to the remaining uncapped constituents in proportion to their current weights. The process repeats because redistribution can create another breach.

After convergence, weights are rounded to output scale 10. The rule assigns the residual difference required to reach exactly `1.0000000000` to the largest uncapped constituent with enough cap capacity, using security ID as a deterministic tie-breaker. It validates the sum and rechecks the cap after rounding. A mathematically impossible cap (`count * cap < 1`) or a zero raw weight fails safely.

## Duplicate and warning policy

Exact duplicate SPI rows and exact duplicate market rows are deduplicated deterministically. Conflicting market rows for the same security/date fail import. Security-level missing/invalid values are emitted as warnings and rejected by eligibility if enough securities remain. The report status becomes `COMPLETED_WITH_WARNINGS` and retains the rejection audit trail.
