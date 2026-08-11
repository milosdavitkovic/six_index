# Persistence adapters

Spring Data JPA storage for imported inputs and review output. Repositories manage securities, SPI universe members, market data, index composition, review results, and audit events.

`ReviewDataLoader` builds a `ReviewDataSnapshot` for an index, review period, cut-off date, and review date. `ReviewResultMapper` translates between the engine context/domain result and the aggregate JPA entities; `AuditEventMapper` maps audit rows for API access. Review results retain constituents, decisions, and audit events.

The default application profile uses an in-memory H2 database with schema generation set to `create-drop`.
