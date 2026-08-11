# Security Rules

- Treat uploaded CSV files, multipart names, path parameters, and configuration as untrusted input.
- Enforce multipart limits and validate CSV headers, delimiters, dates, identifiers, numeric ranges, and business values before persistence or calculation.
- Keep API DTO and validation boundaries explicit; never bind arbitrary request maps directly to JPA entities.
- Use parameterized Spring Data/JPA operations and preserve repository constraints; do not build SQL from input.
- Do not add credentials, secrets, passwords, or environment-specific values to source, configuration examples, logs, or docs.
- Return sanitized errors and avoid logging uploaded content or sensitive data.
