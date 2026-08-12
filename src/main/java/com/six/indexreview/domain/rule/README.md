# Review rules

The review rule package defines the pluggable stages of an index review.

## Components

- `ReviewRule`: the base contract for each rule.
- `impl`: the concrete rule implementations.

## Notes

- Rules expose a stable `code()`, a human-readable `description()`, and `apply(IndexReviewContext)`.
- The default pipeline uses SPI eligibility, FFMCAP ranking and weighting, top-N selection, optional buffering, deterministic joiner/leaver decisions, iterative proportional capping, and audit events.
- `NoOpBufferRule` leaves selection unchanged; `ConfigurableBufferRule` can retain current constituents through the configured retention rank.
- Unsupported rule selections fail the review with `ReviewExecutionException`.
