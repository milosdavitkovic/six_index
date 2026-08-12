# API examples

These examples assume the local service is available at the default Postman-style split endpoint values:

- `protocol`: `http`
- `host`: `localhost`
- `port`: `8080`

## Health

```http
GET /api/health
```

```json
{"status":"UP","service":"index-review"}
```

## Import all files

```bash
curl -X POST http://localhost:8080/api/import/all \
  -F spiUniverse=@data/spi_universe.csv \
  -F securityData=@data/sec_data.csv \
  -F composition=@data/composition.csv
```

The response reports input, stored, and deduplicated rows for each dataset.

## Run review

```bash
curl -X POST http://localhost:8080/api/index-reviews/SMI/Q3-2026/run
```

The response includes `reviewResultId`, status, dates, counts, `constituents`, `joiners`, `leavers`, `unchanged`, `notSelected`, `rejectedSecurities`, and `auditEvents`. A constituent entry has this business data:

```json
{
  "securityId": 177,
  "rank": 7,
  "ffmcap": 84534783495.25,
  "rawWeight": 0.0612345678901234,
  "finalWeight": 0.0612345679,
  "cappingFactor": 1.0,
  "decisionType": "JOINER",
  "decisionReason": "Security ranked inside top 20 by FFMCAP.",
  "capped": false
}
```

## Latest and by ID

```bash
curl http://localhost:8080/api/index-reviews/SMI/Q3-2026/latest
curl http://localhost:8080/api/index-reviews/results/1
```

## Security audit

```bash
curl http://localhost:8080/api/index-reviews/results/1/securities/177/audit
```

Each entry contains `timestamp`, `ruleCode`, `securityId`, `message`, `inputValue`, and `outputValue`. Audit events are ordered by rule execution and security/rank processing order.

## Structured validation error

For an empty or structurally invalid review, the API returns a body shaped like:

```json
{
  "timestamp": "2026-09-21T10:00:00Z",
  "status": 422,
  "errorCode": "REVIEW_VALIDATION_ERROR",
  "message": "Input validation failed",
  "details": "Input validation failed",
  "validationErrors": [
    {
      "code": "COMPOSITION_SIZE",
      "field": "composition",
      "message": "Current composition contains 0 members; expected 20",
      "securityId": null,
      "severity": "ERROR"
    }
  ]
}
```
