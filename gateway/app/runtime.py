import os

from app.config import GatewaySettings
from app.main import create_app


settings = GatewaySettings.from_env()
app = create_app(
    database_path=settings.database_path,
    gateway_token=settings.gateway_token,
)
