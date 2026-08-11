---
apply: always
---

# SIX Index Review

This Java 21 / Spring Boot 3 MVC service imports index-input CSV files and runs deterministic index reviews.

The main flow is `api` -> `application` -> `domain` -> `infrastructure`/`reporting`. The domain engine applies ordered rules for eligibility, FFMCAP ranking, selection, buffers, joiners/leavers, weights, capping, and audit events. JPA with in-memory H2 stores imported snapshots and immutable review results.

SMI for Q3-2026 is enabled. SMIM is a disabled configuration example. Do not assume either is the only future index.

Priorities are reproducible results, input integrity, stable API and persistence contracts, and explainable decisions/audit events. Preserve deterministic ordering, BigDecimal precision, configured methodology values, and final-weight/cap invariants. Do not invent external infrastructure or methodology assumptions.
