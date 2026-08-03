import pytest
from fastapi import HTTPException

from app.auth import require_gateway_token
from app.config import GatewaySettings


def test_settings_require_token(monkeypatch):
    monkeypatch.delenv("FITBIT_GATEWAY_TOKEN", raising=False)
    with pytest.raises(ValueError):
        GatewaySettings.from_env()


def test_settings_load_from_env(monkeypatch):
    monkeypatch.setenv("FITBIT_GATEWAY_TOKEN", "secret")
    monkeypatch.setenv("FITBIT_GATEWAY_PORT", "9999")
    settings = GatewaySettings.from_env()
    assert settings.gateway_token == "secret"
    assert settings.port == 9999



def test_missing_token_is_rejected():
    with pytest.raises(HTTPException) as error:
        require_gateway_token(None, expected_token="secret")
    assert error.value.status_code == 401


def test_wrong_token_is_rejected():
    with pytest.raises(HTTPException) as error:
        require_gateway_token("Bearer wrong", expected_token="secret")
    assert error.value.status_code == 401


def test_valid_bearer_token_is_accepted():
    assert require_gateway_token("Bearer secret", expected_token="secret") is True


def test_malformed_authorization_is_rejected():
    with pytest.raises(HTTPException):
        require_gateway_token("secret", expected_token="secret")


def test_empty_configured_token_is_rejected():
    with pytest.raises(ValueError):
        require_gateway_token("Bearer secret", expected_token="")
