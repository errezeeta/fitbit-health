import sqlite3

from fastapi.testclient import TestClient

from app.main import create_app


def make_db(tmp_path):
    path = tmp_path / "health.db"
    db = sqlite3.connect(path)
    db.execute("CREATE TABLE heart_rate_daily (date TEXT PRIMARY KEY, resting_heart_rate INTEGER)")
    db.commit(); db.close()
    return path


def test_sync_requires_auth(tmp_path):
    app = create_app(database_path=str(make_db(tmp_path)), gateway_token="secret")
    response = TestClient(app).post("/api/v1/sync")
    assert response.status_code == 401


def test_sync_creates_job_and_status_is_queryable(tmp_path):
    app = create_app(database_path=str(make_db(tmp_path)), gateway_token="secret")
    client = TestClient(app)
    headers = {"Authorization": "Bearer secret"}
    response = client.post("/api/v1/sync", headers=headers)
    assert response.status_code == 202
    job_id = response.json()["job_id"]
    status = client.get(f"/api/v1/sync/{job_id}", headers=headers)
    assert status.status_code == 200
    assert status.json()["status"] in {"queued", "running", "completed", "failed"}


def test_duplicate_running_sync_is_rejected(tmp_path):
    app = create_app(database_path=str(make_db(tmp_path)), gateway_token="secret")
    client = TestClient(app)
    headers = {"Authorization": "Bearer secret"}
    first = client.post("/api/v1/sync", headers=headers)
    second = client.post("/api/v1/sync", headers=headers)
    assert first.status_code == 202
    assert second.status_code in {202, 409}


def test_unknown_job_is_not_found(tmp_path):
    app = create_app(database_path=str(make_db(tmp_path)), gateway_token="secret")
    response = TestClient(app).get(
        "/api/v1/sync/missing", headers={"Authorization": "Bearer secret"}
    )
    assert response.status_code == 404
