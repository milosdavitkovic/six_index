# Domain model

Framework-independent records and enums describing an index review.

Core definitions are `IndexCode`, `IndexDefinition`, `ReviewDates`, and `ReviewSchedule`. Input values are represented by `Security`, `SecurityId`, `MarketData`, `CurrentComposition`, and the eligibility/ranking records. Output is represented by `SelectedConstituent`, `ReviewDecision`, `ReviewResult`, `AuditEvent`, and `RejectedSecurity`.

`DecisionType` distinguishes `JOINER`, `LEAVER`, `UNCHANGED`, and `NOT_SELECTED`; `ReviewStatus` describes review completion. Value objects validate and normalize their invariants, while selected constituents preserve rank, FFMCAP, raw/final weights, capping metadata, and decision information.
