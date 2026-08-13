# Testing strategy

## Unit tests

Pure domain services and rules are tested without Spring:

- FFMCAP normal, zero/full free float, invalid values, and BigDecimal behavior.
- ranking order, current-member tie-breaker, ID tie-breaker, and repeated determinism.
- equal-FFMCAP ties at the top-N selection cut-off, including the final security-ID tie-breaker.
- top-N selection and no-op buffer audit output.
- joiner, leaver, unchanged, not-selected, and rejection decisions.
- raw weight validation and the capping rule.
- capping with no breach, one breach, multiple breaches, repeated redistribution, residual rounding, and impossible capacity.
- capping assertions verify every output weight is at output scale, does not exceed the configured cap after rounding, and the rounded weights sum exactly to the output-scale one.
- security-level missing cut-off price, review-date shares, and review-date free-float rejection reasons.
- UTF-8 BOM, delimiter, missing-column, empty-price, and duplicate CSV cases.
- fixed-clock audit event creation.

## Integration tests

Spring Boot/H2 tests verify:

- the supplied CSV files import successfully and duplicate SPI rows are reduced from 409 to 205;
- ID 166 is rejected with a warning;
- the Q3-2026 SMI result contains the expected 20 IDs, joiners, leavers, exact output sum, and cap compliance;
- the result and per-security audit trail can be loaded from JPA;
- multipart import, health, run, latest, and 404 API behavior.

## Invariants asserted

Every successful review must satisfy:

1. selected count equals the configured count;
2. selected FFMCAP values are positive;
3. final weights sum to output-scale one exactly;
4. no final weight exceeds the configured cap after rounding;
5. every security considered has a decision or rejection explanation;
6. rule and security ordering is stable.

## Running tests

```powershell
./mvnw.cmd -s .mvn/settings-central.xml test
```

H2 is enough for these integration tests; Testcontainers is intentionally not required.
