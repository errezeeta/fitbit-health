import sqlite3

from app.repository import FitbitRepository


def make_db(tmp_path):
    path = tmp_path / "fitbit.db"
    conn = sqlite3.connect(path)
    conn.executescript(
        """
        CREATE TABLE heart_rate_daily (date TEXT PRIMARY KEY, resting_heart_rate INTEGER);
        CREATE TABLE sleep (
            id INTEGER PRIMARY KEY, date_of_sleep TEXT, start_time TEXT, end_time TEXT,
            minutes_asleep INTEGER, minutes_awake INTEGER, deep_minutes INTEGER,
            light_minutes INTEGER, rem_minutes INTEGER, awake_minutes INTEGER
        );
        CREATE TABLE steps_daily (date TEXT PRIMARY KEY, total_steps INTEGER);
        CREATE TABLE hrv (date TEXT PRIMARY KEY, rmssd REAL);
        CREATE TABLE spo2 (date TEXT PRIMARY KEY, avg_value REAL);
        INSERT INTO heart_rate_daily VALUES ('2026-08-03', 71);
        INSERT INTO sleep VALUES (1, '2026-08-03', '2026-08-02T23:34:00Z', '2026-08-03T05:22:00Z', 342, 6, 111, 150, 81, 5);
        INSERT INTO steps_daily VALUES ('2026-08-03', 8000);
        INSERT INTO hrv VALUES ('2026-08-03', 38.0);
        INSERT INTO spo2 VALUES ('2026-08-03', 93.7);
        """
    )
    conn.commit()
    conn.close()
    return path


def test_repository_reads_dashboard_and_sleep(tmp_path):
    repo = FitbitRepository(make_db(tmp_path))
    dashboard = repo.dashboard("2026-08-03")
    assert dashboard["resting_heart_rate"] == 71
    assert dashboard["steps"] == 8000
    assert dashboard["sleep"]["minutes_asleep"] == 342
    assert repo.sleep("2026-08-03", "2026-08-03")[0]["rem_minutes"] == 81


def test_repository_returns_metric_series_and_empty_results(tmp_path):
    repo = FitbitRepository(make_db(tmp_path))
    assert repo.metric_series("hrv", "2026-08-03", "2026-08-03")[0]["value"] == 38.0
    # Rango sin datos: devuelve fallback con el histórico disponible (últimos puntos)
    fallback = repo.metric_series("hrv", "2025-01-01", "2025-01-02")
    assert fallback and fallback[-1]["value"] == 38.0


def test_repository_sleep_falls_back_when_range_empty(tmp_path):
    repo = FitbitRepository(make_db(tmp_path))
    fallback = repo.sleep("2025-01-01", "2025-01-02")
    assert fallback and fallback[0]["minutes_asleep"] == 342


def test_repository_rejects_unknown_metric(tmp_path):
    repo = FitbitRepository(make_db(tmp_path))
    try:
        repo.metric_series("passwords", "2026-08-03", "2026-08-03")
    except ValueError as error:
        assert "unsupported metric" in str(error)
    else:
        raise AssertionError("unknown metric was accepted")


def test_repository_limits_intraday_results(tmp_path):
    repo = FitbitRepository(make_db(tmp_path), max_rows=2)
    assert repo.max_rows == 2
