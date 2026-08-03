import pytest

from app.config import GatewaySettings


def test_production_settings_require_private_database(monkeypatch):
    monkeypatch.setenv("FITBIT_GATEWAY_TOKEN", "secret")
    monkeypatch.delenv("HERMES_FITBIT_DB", raising=False)
    settings = GatewaySettings.from_env()
    assert settings.gateway_token == "secret"
    assert settings.database_path == ""


def test_invalid_port_is_rejected(monkeypatch):
    monkeypatch.setenv("FITBIT_GATEWAY_TOKEN", "secret")
    monkeypatch.setenv("FITBIT_GATEWAY_PORT", "not-a-port")
    with pytest.raises(ValueError):
        GatewaySettings.from_env()


def test_port_range_is_safe(monkeypatch):
    monkeypatch.setenv("FITBIT_GATEWAY_TOKEN", "secret")
    monkeypatch.setenv("FITBIT_GATEWAY_PORT", "80")
    with pytest.raises(ValueError):
        GatewaySettings.from_env()


# Red step: port range validation is not implemented yet.
