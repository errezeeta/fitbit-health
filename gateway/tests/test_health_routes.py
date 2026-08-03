import sqlite3

from fastapi.testclient import TestClient

from app.main import create_app


def make_db(tmp_path):
    path = tmp_path / "health.db"
    db = sqlite3.connect(path)
    db.executescript("""
      CREATE TABLE heart_rate_daily (date TEXT PRIMARY KEY, resting_heart_rate INTEGER);
      CREATE TABLE steps_daily (date TEXT PRIMARY KEY, total_steps INTEGER);
      CREATE TABLE sleep (id INTEGER PRIMARY KEY, date_of_sleep TEXT, start_time TEXT, end_time TEXT, minutes_asleep INTEGER, minutes_awake INTEGER, deep_minutes INTEGER, light_minutes INTEGER, rem_minutes INTEGER, awake_minutes INTEGER);
      CREATE TABLE hrv (date TEXT PRIMARY KEY, rmssd REAL);
      CREATE TABLE spo2 (date TEXT PRIMARY KEY, avg_value REAL);
      INSERT INTO heart_rate_daily VALUES ('2026-08-03',71);
      INSERT INTO steps_daily VALUES ('2026-08-03',8000);
      INSERT INTO sleep VALUES (1,'2026-08-03','2026-08-02T23:34:00Z','2026-08-03T05:22:00Z',342,6,111,150,81,5);
      INSERT INTO hrv VALUES ('2026-08-03',38);
      INSERT INTO spo2 VALUES ('2026-08-03',93.7);
    """)
    db.commit(); db.close()
    return path


def client(tmp_path):
    return TestClient(create_app(database_path=str(make_db(tmp_path)), gateway_token="secret"))


def test_health_routes_require_auth(tmp_path):
    response = client(tmp_path).get("/api/v1/dashboard?day=2026-08-03")
    assert response.status_code == 401


def test_dashboard_and_sleep_routes(tmp_path):
    c = client(tmp_path)
    headers = {"Authorization": "Bearer secret"}
    dashboard = c.get("/api/v1/dashboard?day=2026-08-03", headers=headers)
    assert dashboard.status_code == 200
    assert dashboard.json()["resting_heart_rate"] == 71
    sleep = c.get("/api/v1/sleep?start=2026-08-03&end=2026-08-03", headers=headers)
    assert sleep.status_code == 200
    assert sleep.json()[0]["minutes_asleep"] == 342


def test_metric_route_validates_whitelist_and_range(tmp_path):
    c = client(tmp_path); headers = {"Authorization": "Bearer secret"}
    assert c.get("/api/v1/metrics/hrv?start=2026-08-03&end=2026-08-03", headers=headers).status_code == 200
    assert c.get("/api/v1/metrics/passwords?start=2026-08-03&end=2026-08-03", headers=headers).status_code == 400
    assert c.get("/api/v1/metrics/hrv?start=bad&end=bad", headers=headers).status_code == 422


def test_health_endpoint_is_public(tmp_path):
    assert client(tmp_path).get("/health").status_code == 200
