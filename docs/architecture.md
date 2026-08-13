# Architecture

## Boundary and flow

The service separates the review into a read/import boundary, a pure calculation boundary, and a persistence/reporting boundary.

1. `api` handles HTTP, multipart files, status codes, and JSON DTOs.
2. `application` loads a configured review, creates an execution context, runs the engine, persists the result, and assembles the response.
3. `domain.model` contains immutable IDs, dates, market data, ranked and selected securities, decisions, weights, and audit events.
4. `domain.rule` contains small deterministic business rules. Rules mutate only the run context and never access repositories.
5. `domain.service` contains reusable calculation services such as FFMCAP, ranking, selection, weights, capping, and audit creation.
6. `infrastructure.csv` parses and validates CSV representations.
7. `infrastructure.persistence` maps the input snapshot and review aggregate to H2/JPA entities.
8. `reporting` converts the domain aggregate into a stable report DTO.
9. `validation` distinguishes blocking structural errors from security-level warnings.

## Rule engine

`IndexReviewEngine` executes an explicit ordered pipeline. The current SMI sequence is:

```text
VALIDATE_INPUT_DATA
BUILD_ELIGIBLE_UNIVERSE
CALCULATE_AND_RANK_FFMCAP
APPLY_TOP_N_SELECTION
BUFFER_NONE or BUFFER_CONFIGURABLE
IDENTIFY_JOINERS_LEAVERS
CALCULATE_RAW_WEIGHTS
APPLY_WEIGHT_CAP
GENERATE_AUDIT_TRAIL
```

This is intentionally a small internal engine rather than a rules framework. Each rule exposes `code`, `description`, and `apply`, logs its start/end, and emits audit events. A rule can be unit tested with an `IndexReviewContext` without Spring or H2.

## Performance and stability

The review is designed for a small-to-medium index universe and has predictable resource usage:

- Eligibility, FFMCAP calculation, selection, joiner/leaver classification, and most weight operations are linear in the number of eligible securities, `O(N)`.
- Deterministic ranking copies the eligible list and sorts it with a comparator whose final tie-breaker is security ID. Its expected time complexity is `O(N log N)` and its additional working memory is `O(N)`.
- Weight capping operates on the selected set and performs a bounded number of redistribution passes. With `K` selected constituents, the current implementation is typically `O(K²)` in the worst case and `O(K)` additional memory. Since `K` is configuration-bounded for normal index reviews, this does not usually dominate the full review.
- The context, ranked/selected lists, lookup maps, and ordered audit events are intentionally materialized to support validation, deterministic reporting, and auditability. Peak application memory therefore grows approximately linearly with the input universe and review result size, with `BigDecimal` values and audit messages contributing to the constant factor.

If the universe grows materially, first measure import, database loading, ranking, capping, and persistence separately. Keep the deterministic comparator and tie-breakers unchanged, add database indexes and batch reads for date/index lookups, and avoid N+1 persistence operations. For very large snapshots, use streaming or chunked CSV ingestion and a staging table, then retain only the data required by the review context. Ranking still needs all eligible values (or an equivalent external-sort/top-N implementation); any replacement must preserve the same ordering and tie-breakers. Reviews should remain isolated per execution, with bounded input sizes, explicit timeouts, and back-pressure or job-based execution rather than unbounded concurrent HTTP work. These measures improve throughput without weakening reproducibility or the validation and audit guarantees.

## Configuration

`IndexReviewProperties` binds `index-review.precision` and `index-review.indices`. `IndexDefinitionProvider` resolves an enabled index and review period into an immutable `IndexDefinition`. The engine does not contain the number 20 or the value 0.18; both are configuration.

`SMIM` is included as a disabled example showing how another index can have a different count, cap, selection code, and buffer code. A future implementation can add methodology-specific rules while reusing input loading, persistence, reporting, and validation.

## Assumptions and ambiguity decisions

The assignment does not define every methodology detail, so the implementation makes the following explicit assumptions:

- **Buffer size:** `buffer-retention-rank` is an inclusive rank limit. A configurable buffer may retain a current constituent ranked from `constituent-count + 1` through that limit, replacing the lowest-ranked non-current selected constituent. If the configured value is absent or non-positive, the fallback is `constituent-count + 5`. `buffer-rule: NONE` means pure top-N selection with no incumbent retention.
- **Missing values:** missing required CSV fields, malformed values, empty files, and missing headers are structural import failures. A security-level missing or invalid price, shares value, or free-float value is instead a warning and rejects only that security; missing review-date price is harmless because it is not used in FFMCAP.
- **Duplicates:** rows with the same logical key and identical values are deduplicated while preserving first-seen order. Conflicting duplicate SPI or market-data rows fail the import. Composition duplicates are deduplicated by security ID; the first occurrence determines the stable member order.
- **Precision and rounding:** calculations use `BigDecimal`, with internal scale 16 and output scale 10 by default. The configured rounding mode is `HALF_UP` by default. Intermediate calculations retain internal precision; final weights are rounded once, then a deterministic residual adjustment makes the published total exactly `1.0000000000` while respecting the cap.
- **Equal FFMCAP:** ranking is descending by FFMCAP. Equal values first prefer current constituents when `CURRENT_CONSTITUENT_FIRST` is configured, then always use security ID ascending as the final tie-breaker. This makes selection and audit output reproducible even when optional tie-breakers are omitted.

## Persistence

Input tables have uniqueness constraints for market data and SPI universe keys. Review results contain constituent rows, all decisions, and ordered audit events. `created_at` and review identity are indexed for latest-result lookup; audit events are indexed by result and security.

Review results are immutable snapshots after insertion. Re-importing input replaces that dataset transactionally; importing all three files uses one application transaction. The default H2 database is in-memory for the assignment.

## Determinism and auditability

The context stores sorted universe IDs, explicit comparator tie-breakers, a fixed pipeline order, and ordered audit events. BigDecimal precision is centrally configured. Output rounding is followed by residual and cap validation. This makes the business result reproducible for the same input/configuration/rule version, while execution timestamps and generated persistence IDs remain operational metadata.

## Extension points

- `ReviewRule`: any isolated methodology step.
- `SelectionRule`: top-N or a methodology-specific selection strategy.
- `BufferRule`: no-op, configurable rank retention, or an SMI methodology implementation.
- `WeightCapper`: alternative cap/redistribution policy.
- `IndexDefinitionProvider`: configuration-backed index definitions.
- `ReviewReportGenerator`: JSON or future export formats.
