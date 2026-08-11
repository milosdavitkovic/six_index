# Review rules

The rule contracts define the pluggable stages of a review: validation, eligibility, ranking, selection, buffering, joiner/leaver handling, weight calculation, weight capping, and audit reporting.

Implementations live in `impl` and expose a stable `code()`, a human-readable `description()`, and `apply(IndexReviewContext)`. The default pipeline uses FFMCAP ranking and weighting, top-N selection, SPI eligibility, optional configurable buffering, deterministic joiner/leaver decisions, iterative proportional capping, and audit events.

The buffer strategy is selected from configuration. `NoOpBufferRule` leaves selection unchanged; `ConfigurableBufferRule` can retain current constituents through the configured retention rank. Unsupported rule selections fail the review with `ReviewExecutionException`.
