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

## Configuration

`IndexReviewProperties` binds `index-review.precision` and `index-review.indices`. `IndexDefinitionProvider` resolves an enabled index and review period into an immutable `IndexDefinition`. The engine does not contain the number 20 or the value 0.18; both are configuration.

`SMIM` is included as a disabled example showing how another index can have a different count, cap, selection code, and buffer code. A future implementation can add methodology-specific rules while reusing input loading, persistence, reporting, and validation.

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
