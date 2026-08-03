from __future__ import annotations

import json
import re
from typing import Any


class HealthChatService:
    """Builds a bounded, Fitbit-only prompt boundary for the gateway."""

    BLOCKED_PATTERNS = (
        r"\bterminal\b",
        r"\bshell\b",
        r"\bcomando\b",
        r"\bejecuta\b",
        r"\bexec(?:ute|ution)?\b",
        r"\barchivo\b",
        r"\bfile\b",
    )

    def __init__(self, *, max_context_chars: int = 20_000):
        self.max_context_chars = max_context_chars

    def validate_message(self, message: str) -> str:
        if not isinstance(message, str) or not message.strip():
            raise ValueError("message cannot be empty")
        if len(message) > 5000:
            raise ValueError("message exceeds 5000 characters")
        lowered = message.casefold()
        if any(re.search(pattern, lowered) for pattern in self.BLOCKED_PATTERNS):
            raise ValueError("only Fitbit health questions are supported")
        return message.strip()

    def build_prompt(self, message: str, context: dict[str, Any]) -> str:
        message = self.validate_message(message)
        serialized = json.dumps(context, ensure_ascii=False, separators=(",", ":"))
        if len(serialized) > self.max_context_chars:
            raise ValueError("health context exceeds configured limit")
        return (
            "Eres un asistente de salud centrado exclusivamente en datos Fitbit. "
            "No diagnostiques ni ejecutes herramientas generales. Explica límites y recomienda "
            "consultar a un profesional cuando corresponda.\n"
            f"Pregunta del usuario: {message}\n"
            f"Datos Fitbit disponibles: {serialized}"
        )

    @staticmethod
    def redact(value: str) -> str:
        value = re.sub(r"Bearer\s+[^\s]+", "Bearer [REDACTED]", value, flags=re.IGNORECASE)
        value = re.sub(
            r"(?i)(client_secret|api[_-]?key|token|password)\s*[:=]\s*[^\s,;]+",
            r"\1=[REDACTED]",
            value,
        )
        return value

    @staticmethod
    def safe_error(error: Exception) -> dict[str, Any]:
        if isinstance(error, TimeoutError):
            return {"detail": "El chat ha superado el tiempo límite; inténtalo de nuevo.", "retryable": True}
        return {"detail": "No se pudo procesar la consulta de salud.", "retryable": False}

    @staticmethod
    def format_response(answer: str, sources: list[str]) -> dict[str, Any]:
        return {
            "answer": answer,
            "sources": sources,
            "medical_disclaimer": True,
        }
