# Postman setup

This folder contains Postman assets for the `six_index` API:

- `six_index_api.postman_collection.json`
- `six_index_local.postman_environment.json`

## Import into Postman

1. Open Postman.
2. Import `postman/six_index_api.postman_collection.json`.
3. Import `postman/six_index_local.postman_environment.json`.
4. Select the `six_index local` environment.

## Run the API requests

1. Start the application locally.
   ```bash
   ./mvnw spring-boot:run
   ```
   On Windows, use:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
2. Make sure the Postman environment variables `protocol`, `host`, and `port` point to the running service.
   - Default values: `http`, `localhost`, `8080`
3. Run the requests in this order:
   - `Health` → `Get API health`
   - `Import` → `Import all datasets`
   - `Index reviews` → `Run review`
   - `Index reviews` → `Latest review`
   - `Index reviews` → `Review by ID`
   - `Index reviews` → `Security audit`

## Notes

- The multipart import requests use the sample CSV files from the repo `data/` folder.
- The review example uses the same scenario as the integration test:
  - index code: `SMI`
  - review period: `Q3-2026`
  - audit security id: `177`
- The invalid review example uses `SMIM` to demonstrate the `400 INVALID_REQUEST` path.

