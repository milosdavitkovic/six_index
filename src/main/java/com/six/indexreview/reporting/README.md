# Reporting

`ReviewReportGenerator` converts a domain `ReviewResult` into a transport-neutral `ReviewReport`. It sorts decisions, constituents, current members, outcome lists, and rejected securities deterministically and copies the full audit trail.

`reporting.dto` contains immutable report records for constituents, decisions, rejected securities, audit entries, and the complete report. `ReportFormat` defines the supported report-format vocabulary; the current API exposes the generated structure through `ReviewResponse`.
