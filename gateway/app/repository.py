from __future__ import annotations

import sqlite3
from pathlib import Path
from typing import Any


class FitbitRepository:
    """Small read-only SQLite repository for the existing Fitbit database."""

    METRICS = {
        "rhr": ("heart_rate_daily", "date", "resting_heart_rate"),
        "hrv": ("hrv", "date", "rmssd"),
        "spo2": ("spo2", "date", "avg_value"),
        "steps": ("steps_daily", "date", "total_steps"),
    }

    def __init__(self, database_path: str | Path, *, max_rows: int = 10_000):
        self.database_path = str(database_path)
        self.max_rows = max(1, max_rows)

    def _connect(self) -> sqlite3.Connection:
        connection = sqlite3.connect(f"file:{self.database_path}?mode=ro", uri=True)
        connection.row_factory = sqlite3.Row
        return connection

    def dashboard(self, day: str) -> dict[str, Any]:
        with self._connect() as connection:
            result: dict[str, Any] = {}
            result["resting_heart_rate"] = self._value(connection, "SELECT resting_heart_rate FROM heart_rate_daily WHERE date = ?", day)
            result["steps"] = self._value(connection, "SELECT total_steps FROM steps_daily WHERE date = ?", day)
            result["hrv"] = self._value(connection, "SELECT rmssd FROM hrv WHERE date = ?", day)
            result["spo2"] = self._value(connection, "SELECT avg_value FROM spo2 WHERE date = ?", day)
            sleep = connection.execute(
                "SELECT * FROM sleep WHERE date_of_sleep = ? ORDER BY end_time DESC LIMIT 1", (day,)
            ).fetchone()
            result["sleep"] = dict(sleep) if sleep else None
            return result

    def sleep(self, start: str, end: str) -> list[dict[str, Any]]:
        with self._connect() as connection:
            rows = connection.execute(
                "SELECT * FROM sleep WHERE date_of_sleep BETWEEN ? AND ? ORDER BY date_of_sleep DESC LIMIT ?",
                (start, end, self.max_rows),
            ).fetchall()
            return [dict(row) for row in rows]

    def metric_series(self, metric: str, start: str, end: str) -> list[dict[str, Any]]:
        if metric not in self.METRICS:
            raise ValueError(f"unsupported metric: {metric}")
        table, date_column, value_column = self.METRICS[metric]
        query = (
            f"SELECT {date_column} AS date, {value_column} AS value "
            f"FROM {table} WHERE {date_column} BETWEEN ? AND ? "
            f"ORDER BY {date_column} LIMIT ?"
        )
        with self._connect() as connection:
            rows = connection.execute(query, (start, end, self.max_rows)).fetchall()
            return [dict(row) for row in rows]

    @staticmethod
    def _value(connection: sqlite3.Connection, query: str, day: str) -> Any:
        row = connection.execute(query, (day,)).fetchone()
        return row[0] if row else None
