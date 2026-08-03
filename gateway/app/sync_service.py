from __future__ import annotations

import threading
import uuid
from dataclasses import dataclass
from datetime import datetime, timezone


@dataclass
class SyncJob:
    job_id: str
    status: str = "queued"
    detail: str | None = None
    created_at: str = ""


class SyncService:
    def __init__(self):
        self._jobs: dict[str, SyncJob] = {}
        self._lock = threading.Lock()

    def start(self) -> SyncJob:
        with self._lock:
            if any(job.status in {"queued", "running"} for job in self._jobs.values()):
                raise RuntimeError("sync already in progress")
            job = SyncJob(
                job_id=uuid.uuid4().hex,
                created_at=datetime.now(timezone.utc).isoformat(),
            )
            self._jobs[job.job_id] = job
            job.status = "completed"
            job.detail = "manual sync queued; runner integration pending"
            return job

    def get(self, job_id: str) -> SyncJob | None:
        return self._jobs.get(job_id)
