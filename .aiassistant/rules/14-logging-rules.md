# Logging Rules

- Use the existing Lombok `@Slf4j`/SLF4J style and parameterized messages.
- Log rule start/completion and useful counts or values such as index code, period, universe size, eligible/rejected counts, and final-weight sum.
- Use INFO for import/review milestones, DEBUG for calculation detail, and WARN for expected rejected data or disabled methodology options.
- Log failures at the boundary that can act on them, with the cause, and avoid repeating the same failure at every layer.
- Never log complete CSV rows, uploaded file contents, credentials, or unnecessarily sensitive data.
