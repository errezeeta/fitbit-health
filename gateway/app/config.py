from __future__ import annotations

import os
from dataclasses import dataclass


@dataclass(frozen=True)
class GatewaySettings:
    gateway_token: str
    database_path: str = ""
    host: str = "0.0.0.0"
    port: int = 8844

    @classmethod
    def from_env(cls) -> "GatewaySettings":
        token = os.environ.get("FITBIT_GATEWAY_TOKEN", "")
        if not token:
            raise ValueError("FITBIT_GATEWAY_TOKEN must be configured")
        port = int(os.environ.get("FITBIT_GATEWAY_PORT", "8844"))
        if not 1024 <= port <= 65535:
            raise ValueError("FITBIT_GATEWAY_PORT must be between 1024 and 65535")
        return cls(
            gateway_token=token,
            database_path=os.environ.get("HERMES_FITBIT_DB", ""),
            host=os.environ.get("FITBIT_GATEWAY_HOST", "0.0.0.0"),
            port=port,
        )
