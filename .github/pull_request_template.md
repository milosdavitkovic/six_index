## Summary
- What changed?
- Why is this needed?

## Validation
- [ ] `./mvnw test` or `.\mvnw.cmd test`
- [ ] `.\mvnw.cmd -s .mvn/settings-central.xml test` if the change affects CI/build behavior
- [ ] `bash six-scripts/java-spring/verify-java-app.sh` if packaging or release behavior changed

## Notes for reviewers
- Reference `docs/business-rules.md` and the relevant package README when changing review logic, CSV parsing, validation, or persistence.

