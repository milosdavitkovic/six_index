# Review engine

The engine owns the deterministic review pipeline and its mutable execution context.

`IndexReviewContext` combines the resolved `IndexDefinition`, imported `ReviewDataSnapshot`, precision policy, execution timestamp, and accumulated eligible/rejected/ranked/selected/decision/audit/validation data. Rules mutate this context in a controlled sequence.

`IndexReviewEngine` executes:

1. input validation;
2. SPI-universe eligibility;
3. FFMCAP ranking;
4. top-N selection;
5. configured buffer (`NONE`/`NOOP` or `CONFIGURABLE`);
6. joiner/leaver decisions;
7. FFMCAP weight calculation;
8. iterative weight capping and redistribution;
9. audit/report completion.

`ReviewDataSnapshot` is the read-only input boundary; `RuleExecutionResult` is the small rule/audit result value.
