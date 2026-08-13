# Assumptions

This document records the implementation assumptions used by the deterministic
SIX review example. Methodology-specific choices remain configuration-driven
where the application supports them.

## Ranking and ties

- FFMCAP is ranked in descending order.
- For equal FFMCAP, the configured `CURRENT_CONSTITUENT_FIRST` preference is
  applied when enabled.
- Security ID ascending is the final and mandatory tie-breaker. The current
  input model uses numeric security IDs rather than ISINs; if an ISIN-based
  model is introduced, its lexical ordering must be made an explicit
  methodology choice.

## Dates and missing values

- For Q3-2026, FFMCAP is `price(cut-off date) * shares(review date) *
  free-float(review date)`.
- Review-date price is not used, so it may be absent without rejecting a
  security.
- A missing or non-positive cut-off price, missing or non-positive review-date
  shares, or missing/out-of-range review-date free float rejects that security
  during eligibility. There is no fallback to another date or another value.
- A free float of zero is within the accepted `[0, 1]` input range, but it
  produces zero FFMCAP and cannot be used as a selected constituent because
  selected FFMCAPs and weights must be positive.
- Missing columns, empty required CSV values, malformed values, and empty files
  are structural import failures and raise `DataImportException`.

## Precision and rounding

- Decimal input is parsed directly as `BigDecimal`; no binary floating-point
  conversion is used.
- The default precision policy uses internal scale 16, output scale 10, and
  `HALF_UP` rounding. These values are configurable through
  `index-review.precision`.
- Raw-weight division and iterative cap redistribution use the internal
  precision. Intermediate values are not rounded to the published output
  scale.
- Final weights are rounded once to scale 10 after capping and redistribution.
  Any residual caused by this final rounding is assigned deterministically to
  the largest eligible uncapped constituent with sufficient cap capacity; the
  security ID breaks equal-size ties. The result must sum exactly to
  `1.0000000000` and still respect the cap after rounding.

## Buffer behavior

- The enabled SMI example uses `TOP_N` with buffer rule `NONE`: selection is
  pure top-N ranking, and buffer securities are not retained as alternates.
- The disabled SMIM example demonstrates configurable buffer retention. When
  enabled, an incumbent within the configured retention rank may be retained;
  this is a methodology decision, not a general alternate list emitted by
  the engine.

## Duplicate CSV rows

- Exact duplicate SPI, market-data, and composition rows are deduplicated
  deterministically before persistence.
- Rows with the same natural key but conflicting values fail the import with
  `DataImportException`; no partial market-data persistence is performed.
