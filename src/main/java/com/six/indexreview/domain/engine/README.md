# Review engine

## Overview

The review engine owns the deterministic pipeline and the mutable execution context used by the domain rules.

## Components

- `IndexReviewContext`: combines the resolved `IndexDefinition`, imported `ReviewDataSnapshot`, precision policy, execution timestamp, and accumulated review state.
- `IndexReviewEngine`: executes the ordered rule pipeline.
- `ReviewDataSnapshot`: read-only input boundary for the imported data.
- `RuleExecutionResult`: small value used to capture rule and audit outcomes.

## Notes

1. input validation
2. SPI-universe eligibility
3. FFMCAP ranking
4. top-N selection
5. configured buffer handling
6. joiner/leaver decisions
7. FFMCAP weight calculation
8. iterative weight capping and redistribution
9. audit/report completion


- Rules mutate only the execution context and remain deterministic.
- The engine uses a fixed order rather than a general-purpose rules framework.
