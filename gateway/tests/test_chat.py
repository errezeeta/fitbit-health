import pytest

from app.chat_service import HealthChatService


def test_health_prompt_contains_only_health_context():
    service = HealthChatService()
    prompt = service.build_prompt("¿Cómo dormí?", {"sleep": {"minutes_asleep": 342}})
    assert "¿Cómo dormí?" in prompt
    assert "342" in prompt
    assert "Fitbit" in prompt


def test_non_health_request_is_rejected():
    service = HealthChatService()
    with pytest.raises(ValueError):
        service.validate_message("Ejecuta un comando de terminal")


def test_secret_like_values_are_redacted():
    service = HealthChatService()
    assert "Bearer secret" not in service.redact("Authorization: Bearer secret")
    assert "client_secret=secret" not in service.redact("client_secret=secret")
    assert "[REDACTED]" in service.redact("client_secret=secret")


def test_timeout_is_mapped_to_safe_response():
    service = HealthChatService()
    response = service.safe_error(TimeoutError())
    assert response["retryable"] is True
    assert "tiempo" in response["detail"].lower()


def test_chat_response_has_disclaimer():
    service = HealthChatService()
    response = service.format_response("Dormiste poco.", ["sleep:2026-08-03"])
    assert response["answer"] == "Dormiste poco."
    assert response["sources"] == ["sleep:2026-08-03"]
    assert response["medical_disclaimer"] is True


# TDD red step: app.chat_service is intentionally absent at collection time.


def test_empty_message_is_rejected():
    service = HealthChatService()
    with pytest.raises(ValueError):
        service.validate_message("   ")


def test_message_length_is_bounded():
    service = HealthChatService()
    with pytest.raises(ValueError):
        service.validate_message("x" * 5001)


def test_context_is_bounded():
    service = HealthChatService(max_context_chars=20)
    with pytest.raises(ValueError):
        service.build_prompt("¿Qué tal?", {"large": "x" * 100})
