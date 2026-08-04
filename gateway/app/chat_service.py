from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
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

    def __init__(self, *, max_context_chars: int = 20_000, hermes_bin: str | None = None, timeout_s: float = 120.0):
        self.max_context_chars = max_context_chars
        self.hermes_bin = hermes_bin or os.environ.get("HERMES_BIN") or shutil.which("hermes") or "hermes"
        self.timeout_s = timeout_s

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

    def ask_hermes(self, prompt: str) -> str:
        """Runs a one-shot Hermes query and returns only the agent's answer."""
        result = subprocess.run(
            [self.hermes_bin, "chat", "-q", prompt],
            capture_output=True,
            text=True,
            timeout=self.timeout_s,
            encoding="utf-8",
            errors="replace",
        )
        if result.returncode != 0:
            detail = (result.stderr or result.stdout or "").strip()[:400]
            raise RuntimeError(f"hermes exited {result.returncode}: {detail}")
        output = (result.stdout or "").strip()
        if not output:
            raise RuntimeError("hermes returned an empty answer")
        return self._extract_answer(output)

    @staticmethod
    def _extract_answer(output: str) -> str:
        """Extrae la respuesta del box decorativo de Hermes, sin el prompt ni metadatos."""
        # Quitar códigos ANSI de color de terminal
        output = re.sub(r"\x1b\[[0-9;]*m", "", output)
        lines = output.splitlines()
        # Buscar el bloque entre ╭─ ... ╰─
        start = next((i for i, line in enumerate(lines) if "╭" in line and "Hermes" in line), None)
        end = next((i for i, line in enumerate(lines) if "╰" in line), None)
        if start is not None and end is not None and end > start:
            body = lines[start + 1:end]
            # Quitar indentación decorativa del box (4 espacios) y líneas vacías extremas
            cleaned = [line.strip() for line in body if line.strip()]
            return "\n".join(cleaned).strip()
        # Fallback: quitar primeras líneas de metadatos (Query:/Initializing/separadores)
        meaningful = [
            line for line in lines
            if line.strip()
            and not line.startswith("Query:")
            and not line.startswith("Initializing")
            and not line.startswith("─")
            and not line.startswith("Resume this session")
            and not line.startswith("Session:")
            and not line.startswith("Duration:")
            and not line.startswith("Messages:")
        ]
        return "\n".join(meaningful).strip()

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
