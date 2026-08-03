from __future__ import annotations

from datetime import datetime, timezone
from zoneinfo import ZoneInfo, ZoneInfoNotFoundError


def _zone(name: str) -> ZoneInfo:
    try:
        return ZoneInfo(name)
    except ZoneInfoNotFoundError as error:
        raise ValueError(f"invalid timezone: {name}") from error


def utc_to_local(timestamp: str, timezone_name: str) -> str:
    """Convert an explicit UTC ISO-8601 timestamp to local ISO-8601 with offset."""
    if not isinstance(timestamp, str) or not timestamp:
        raise ValueError("timestamp must be a non-empty string")
    try:
        parsed = datetime.fromisoformat(timestamp.replace("Z", "+00:00"))
    except ValueError as error:
        raise ValueError("invalid timestamp") from error
    if parsed.tzinfo is None or parsed.utcoffset() is None:
        raise ValueError("timestamp must include UTC")
    local = parsed.astimezone(_zone(timezone_name))
    return local.isoformat(timespec="seconds")


def format_local_timestamp(timestamp: str, timezone_name: str) -> str:
    return utc_to_local(timestamp, timezone_name)
