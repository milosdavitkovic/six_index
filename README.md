# SIX Index Review

Deterministic Spring Boot service for SIX index reviews. The application imports SPI universe, security market data, and current composition files, then runs the review pipeline to produce eligibility, FFMCAP ranking, constituent selection, joiner/leaver decisions, raw weights, capped final weights, and an auditable result.

## Read this first

- Repository branch for this setup: `feature/initial_setup`
- This project is Maven-based and includes the Maven Wrapper (`mvnw` / `mvnw.cmd`)
- The application uses Java 21, Spring Boot 3.4.3, and an in-memory H2 database by default
- No real secrets are required
- No external database, Kafka broker, or Docker stack is required for the default local run

## Repository layout

- `src/main/java/com/six/indexreview/api` — REST controllers and DTOs
- `src/main/java/com/six/indexreview/application` — application orchestration and exception handling
- `src/main/java/com/six/indexreview/domain` — deterministic business logic, calculations, and rules
- `src/main/java/com/six/indexreview/infrastructure` — CSV, configuration, and persistence adapters
- `src/main/resources/application.yaml` — default local configuration (the H2 web console is disabled by default and only enabled in the `local` profile)
- `src/test/java` — unit and integration tests
- `data/` — sample CSV inputs used by the tests and local smoke checks
- `postman/` — Postman collection and environment for manual API exploration
- `six-scripts/` — helper scripts, including the repository verifier

## Requirements

Install the following before opening the project:

- **JDK 21**
  - Tested with Temurin/OpenJDK 21
  - Set IntelliJ to use the same JDK for the project and for Maven
- **IntelliJ IDEA 2024.3+** or another IDEA version with full Java 21 and Maven Wrapper support
- **Git**
- **No separate Maven install is required** because the wrapper is committed

Optional but helpful:

- **Git Bash** on Windows for the provided shell commands
- **curl** for the health check and sample API calls

## Clean-machine setup

### 1) Clone the repository and check out the correct branch

```bash
git clone https://github.com/milosdavitkovic/six_index.git
cd six_index
git checkout feature/initial_setup
```

### 2) Import into IntelliJ IDEA

1. Open IntelliJ IDEA.
2. Select **File > Open** and choose the repository root folder.
3. When prompted, import it as a **Maven** project.
4. Wait for indexing and Maven synchronization to finish.

### 3) Select the correct JDK and build tool in IntelliJ

In **File > Project Structure**:

- **Project SDK**: set to **JDK 21**
- **Project language level**: set to **21**
- **Project compiler output**: leave default unless your team prefers a custom folder

In **Settings > Build, Execution, Deployment > Build Tools > Maven**:

- **Maven home path**: keep **Maven Wrapper** / project wrapper if available
- **JDK for importer**: set to **Project SDK (21)**
- **Runner JRE**: set to **Project SDK (21)**

If IntelliJ asks for annotation processing, allow it. Lombok is declared in the build, and the project compiles cleanly with Java 21.

### 4) Configure local settings without real secrets

This service does not require production secrets for local use.

Use the defaults in `src/main/resources/application.yaml`:

- H2 in-memory database
- Spring Data JPA with `ddl-auto: create-drop`
- Default HTTP port `8080`
- Enabled index configuration `SMI`


If you want to override values locally, create an IntelliJ run configuration or environment variables for your session only. Do **not** commit secrets or machine-specific values.

There is currently no `.env`-driven configuration path in the repository, so no `.env.example` file is required for the default setup.

## Build, test, run, and verify

### Build and run tests

Run from the repository root:

```bash
./mvnw test
```

Windows Git Bash:

```bash
./mvnw.cmd test
```

### Build a runnable JAR

```bash
./mvnw clean package
```

Windows Git Bash:

```bash
./mvnw.cmd clean package
```

### Start the application locally

```bash
./mvnw spring-boot:run
```

Windows Git Bash:

```bash
./mvnw.cmd spring-boot:run
```

The application listens on `http://localhost:8080` by default.

### Verify with a health check

```bash
curl http://localhost:8080/api/health
```

Expected result: a successful JSON response from `HealthController`.

### Optional smoke test with sample data

1. Start the application.
2. Import sample CSV files from `data/`.
3. Run the default review endpoint.

```bash
curl -X POST http://localhost:8080/api/import/all \
  -F spiUniverse=@data/spi_universe.csv \
  -F securityData=@data/sec_data.csv \
  -F composition=@data/composition.csv

curl -X POST http://localhost:8080/api/index-reviews/SMI/Q3-2026/run
curl http://localhost:8080/api/index-reviews/SMI/Q3-2026/latest
```

## Startup and verification commands

Recommended order for a new developer:

1. `./mvnw test`
2. `./mvnw spring-boot:run`
3. `curl http://localhost:8080/api/health`
4. Optional: import sample CSVs and run the `SMI/Q3-2026` review

## Required environment variables and local configuration

The default application run does not require environment variables.

Inventory of meaningful runtime configuration:

- `spring.datasource.url` — defaults to an in-memory H2 database
- `spring.datasource.username` — defaults to `sa`
- `spring.datasource.password` — empty by default
- `spring.jpa.hibernate.ddl-auto` — defaults to `create-drop`
- `server.port` — defaults to `8080` via Spring Boot standard behavior
- `index-review.precision.internal-scale`
- `index-review.precision.output-scale`
- `index-review.precision.rounding-mode`
- `index-review.indices.SMI.constituent-count`
- `index-review.indices.SMI.max-weight`
- `index-review.indices.SMI.ranking-rule`
- `index-review.indices.SMI.selection-rule`
- `index-review.indices.SMI.buffer-rule`
- `index-review.indices.SMI.review-period`
- `index-review.indices.SMI.cut-off-date`
- `index-review.indices.SMI.review-date`
- `index-review.indices.SMI.enabled`

Current status: no undocumented secret-bearing environment variables were found in the codebase.

## Docker Compose and supporting services

Not applicable for the default local setup.

Reason:

- The application uses in-memory H2
- No Kafka, Redis, or external database is required
- No Dockerfile or Compose file is committed in this branch

If you later introduce external services, add a `docker-compose.yml`, document startup order, and update this README accordingly.

## IntelliJ run configuration

Create a run configuration if you do not want to use the Maven tool window:

1. **Run > Edit Configurations**
2. Add **Spring Boot** or **Maven** depending on your IntelliJ edition
3. Main class: `com.six.indexreview.IndexReviewApplication`
4. JDK: `21`
5. Working directory: repository root
6. Program/Maven goal: `spring-boot:run`

For tests, use the Maven goal `test` or run the JUnit test classes directly from IntelliJ.

## Application API

### Import endpoints

- `POST /api/import/all`
- `POST /api/import/spi-universe`
- `POST /api/import/security-data`
- `POST /api/import/composition`

### Review endpoints

- `POST /api/index-reviews/{indexCode}/{reviewPeriod}/run`
- `GET /api/index-reviews/{indexCode}/{reviewPeriod}/latest`
- `GET /api/index-reviews/results/{reviewResultId}`
- `GET /api/index-reviews/results/{reviewResultId}/securities/{securityId}/audit`

### Health endpoint

- `GET /api/health`

## Data import format

Sample CSV files live in `data/`:

- `data/spi_universe.csv`
- `data/sec_data.csv`
- `data/composition.csv`

Expected formats:

| File | Required headers | Delimiter |
|---|---|---|
| SPI universe | `date`, `id` | `;` |
| Security data | `id`, `date`, `price`, `free_float`, `shares` | `;` |
| Composition | `id` | `,` or `;` |

Dates use `yyyy-MM-dd`. Identical duplicate rows are deduplicated. Conflicting duplicates fail the import.

## Project structure notes

The code follows a layered design:

- `api` — REST controllers and response DTOs
- `application` — use-case orchestration and exception mapping contracts
- `domain/engine` — review context and rule pipeline
- `domain/model` — immutable domain records and enums
- `domain/rule` — review rule contracts and implementations
- `domain/service` — deterministic calculations and precision helpers
- `infrastructure` — configuration, CSV, and persistence adapters
- `reporting` — transport-neutral report generation
- `validation` — input validation and structured findings

## Existing verification assets

- `six-scripts/java-spring/verify-java-app.sh` — repository verification script
- `postman/README.md` — manual API exploration
- `docs/testing-strategy.md` — test coverage and invariants

## Notes on verification

I verified the Maven test suite locally on this branch with `./mvnw.cmd test` from Git Bash. The project completed successfully with 21 tests passing.

I did not verify a Docker or Compose workflow because this branch does not provide one.

## Additional context

See also:

- `AGENTS.md`
- `docs/architecture.md`
- `docs/business-rules.md`
- `docs/testing-strategy.md`
- `data/README.pdf`
