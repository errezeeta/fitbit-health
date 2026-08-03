# Fitbit Health Gateway API

Public contract placeholder for the private Hermes gateway.

Base URL is configured by the Android app; do not commit personal Tailscale addresses.
All endpoints require `Authorization: Bearer <gateway-token>`.

## Endpoints

- `GET /health` — gateway availability and version.
- `GET /api/v1/dashboard?range=7d` — current health summary.
- `GET /api/v1/sleep?start=YYYY-MM-DD&end=YYYY-MM-DD` — sleep sessions and stages.
- `GET /api/v1/heart-rate?date=YYYY-MM-DD&detail=daily|intraday` — heart-rate data.
- `GET /api/v1/steps?start=YYYY-MM-DD&end=YYYY-MM-DD` — steps data.
- `GET /api/v1/trends?metrics=...&start=...&end=...` — metric trends.
- `GET /api/v1/sync/status` — synchronization status.
- `POST /api/v1/sync` — request a manual synchronization.
- `GET /api/v1/sync/{job_id}` — synchronization job status.
- `POST /api/v1/chat` — Fitbit-health-only assistant chat.

## Rules

- Dates use `YYYY-MM-DD`.
- Timestamps are ISO-8601 and include an explicit offset or UTC marker.
- Custom ranges and intraday responses are bounded by the gateway.
- Errors use JSON with `detail`, `code`, and an optional `retryable` field.
- The repository must never contain credentials, tokens, SQLite databases, Fitbit data, or personal hostnames/IP addresses.

The concrete Pydantic schemas will be added in the implementation phase.
