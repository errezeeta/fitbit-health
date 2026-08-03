$ErrorActionPreference = "Stop"

if (-not $env:FITBIT_GATEWAY_TOKEN) {
    throw "FITBIT_GATEWAY_TOKEN must be set in the local environment."
}
if (-not $env:HERMES_FITBIT_DB) {
    throw "HERMES_FITBIT_DB must point to the private Fitbit SQLite database."
}

python -m uvicorn app.runtime:app --host ($env:FITBIT_GATEWAY_HOST ?? "0.0.0.0") --port ([int]($env:FITBIT_GATEWAY_PORT ?? "8844"))
