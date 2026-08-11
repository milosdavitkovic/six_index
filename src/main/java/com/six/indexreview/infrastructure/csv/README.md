# CSV adapters

Readers consume UTF-8 CSV and return typed rows. Headers are trimmed, case-insensitive, and BOM-tolerant; dates use ISO-8601 `yyyy-MM-dd`; decimal values use Java `BigDecimal` syntax.

| Dataset | Required columns | Delimiter |
|---|---|---|
| SPI universe | `date`, `id` | `;` |
| Security data | `id`, `date`, `price`, `free_float`, `shares` | `;` |
| Composition | `id` | `,` or `;` (detected from the header) |

Empty files, missing headers, empty required values, invalid dates/decimals, and files without rows raise `DataImportException`. `CsvParsingUtils` centralizes header lookup and scalar parsing. Import services deduplicate identical keys and reject conflicting duplicates.
