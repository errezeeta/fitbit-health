from __future__ import annotations

from datetime import date
from pathlib import Path

from fastapi import Depends, FastAPI, Header, HTTPException, Query

from app.auth import require_gateway_token
from app.chat_service import HealthChatService
from app.repository import FitbitRepository
from app.sync_service import SyncService
from app.config import GatewaySettings


def create_app(*, database_path: str, gateway_token: str) -> FastAPI:
    app = FastAPI(title="Fitbit Health Gateway", version="0.1.0")
    repository = FitbitRepository(Path(database_path))
    sync_service = SyncService()
    chat_service = HealthChatService()

    def auth(authorization: str | None = Header(default=None)) -> bool:
        return require_gateway_token(authorization, expected_token=gateway_token)

    @app.get("/health")
    def health():
        return {"status": "ok", "service": "fitbit-health-gateway"}

    @app.get("/api/v1/dashboard")
    def dashboard(day: date = Query(...), _: bool = Depends(auth)):
        return repository.dashboard(day.isoformat())

    @app.get("/api/v1/sleep")
    def sleep(start: date = Query(...), end: date = Query(...), _: bool = Depends(auth)):
        if end < start:
            raise HTTPException(status_code=422, detail="end must not precede start")
        return repository.sleep(start.isoformat(), end.isoformat())

    @app.get("/api/v1/trends")
    def trends(start: date = Query(...), end: date = Query(...), _: bool = Depends(auth)):
        if end < start:
            raise HTTPException(status_code=422, detail="end must not precede start")
        return {
            metric: repository.metric_series(metric, start.isoformat(), end.isoformat())
            for metric in ("rhr", "hrv", "spo2", "steps")
        }

    @app.get("/api/v1/metrics/{metric}")
    def metric(metric: str, start: date = Query(...), end: date = Query(...), _: bool = Depends(auth)):
        if end < start:
            raise HTTPException(status_code=422, detail="end must not precede start")
        try:
            return repository.metric_series(metric, start.isoformat(), end.isoformat())
        except ValueError as error:
            raise HTTPException(status_code=400, detail=str(error)) from error

    @app.post("/api/v1/sync", status_code=202)
    def start_sync(_: bool = Depends(auth)):
        try:
            job = sync_service.start()
        except RuntimeError as error:
            raise HTTPException(status_code=409, detail=str(error)) from error
        return {"job_id": job.job_id, "status": job.status, "detail": job.detail}

    @app.get("/api/v1/sync/{job_id}")
    def sync_status(job_id: str, _: bool = Depends(auth)):
        job = sync_service.get(job_id)
        if job is None:
            raise HTTPException(status_code=404, detail="sync job not found")
        return {"job_id": job.job_id, "status": job.status, "detail": job.detail}

    @app.post("/api/v1/chat")
    def chat(payload: dict, _: bool = Depends(auth)):
        try:
            message = chat_service.validate_message(payload.get("message", ""))
            context = payload.get("context", {})
            prompt = chat_service.build_prompt(message, context)
        except ValueError as error:
            raise HTTPException(status_code=400, detail=str(error)) from error
        return chat_service.format_response(
            "Chat adapter ready for Hermes health integration.",
            ["fitbit-context"],
        ) | {"prompt_preview": prompt[:500]}

    return app
