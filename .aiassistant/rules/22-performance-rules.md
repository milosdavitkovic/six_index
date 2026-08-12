# Performance Rules

- Preserve synchronous, deterministic review execution unless profiling and a correctness-safe design justify a change.
- Avoid unnecessary copies of complete CSV datasets, domain snapshots, reports, or audit trails.
- Keep parsing and duplicate detection bounded by configured upload limits and use efficient repository operations for imports.
- Do not parallelize rule execution: the pipeline and audit sequence are intentionally ordered.
- Use existing database indexes, constraints, and query methods for latest-result and audit lookups.
- Measure before optimizing, and preserve precision and stable ordering.
