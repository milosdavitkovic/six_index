# Reporting

The reporting package converts a domain review result into a transport-neutral report structure that the application and API layers can expose without leaking domain internals.

## Components

- `ReviewReportGenerator`: builds a stable `ReviewReport` from a domain `ReviewResult`.
- `reporting.dto`: immutable report records for constituents, decisions, rejected securities, audit entries, and the full report.
- `ReportFormat`: defines the supported report-format vocabulary.

## Notes

- Decisions, constituents, current members, outcome lists, rejected securities, and audit events are ordered deterministically.
- The generated report is exposed to the API through `ReviewResponse`.
