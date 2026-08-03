# Fitbit Health Gateway

Private FastAPI gateway for the Android client. It reads the local Fitbit SQLite database and exposes only health endpoints.

## Local setup

```powershell
cd gateway
python -m pip install -e ".[test]"
Copy-Item .env.example .env
# Edit .env locally; never commit it.
python -m uvicorn app.runtime:app --host 0.0.0.0 --port 8844
```

The current development factory is exposed through `create_app(...)`; production wiring will load `GatewaySettings` and the private database path.

## Tailscale

Run the gateway on the Hermes PC and access it from an Android device connected to the same tailnet. Use the PC's Tailscale hostname or `100.x.y.z` address in the app settings. Do not put a personal address in this repository.

Restrict the Windows Firewall rule to the Tailscale interface and keep `FITBIT_GATEWAY_TOKEN` configured with a long random value.

## Security

The gateway token is required for all `/api/v1/*` routes. Google credentials, Fitbit tokens, SQLite files, chat data, and host-specific configuration remain outside Git.

The Android client should store the token using encrypted storage and should never log it.

## Tests

```powershell
python -m pytest tests -q
```
