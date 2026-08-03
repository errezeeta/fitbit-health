from __future__ import annotations

from datetime import date, datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator


Preset = Literal["7d", "30d", "90d"]
SyncStatus = Literal["queued", "running", "completed", "failed"]


class DateRange(BaseModel):
    model_config = ConfigDict(extra="forbid")

    preset: Preset | None = None
    start: date | None = None
    end: date | None = None

    @model_validator(mode="after")
    def validate_range(self) -> "DateRange":
        if self.preset is None and (self.start is None or self.end is None):
            raise ValueError("provide a preset or both start and end")
        if self.preset is not None and (self.start is not None or self.end is not None):
            raise ValueError("preset cannot be combined with custom dates")
        if self.start and self.end:
            if self.end < self.start:
                raise ValueError("end must not precede start")
            if (self.end - self.start).days > 365:
                raise ValueError("date range cannot exceed 365 days")
        return self


class MetricPoint(BaseModel):
    model_config = ConfigDict(extra="forbid")

    timestamp: datetime
    value: float | int | None = None

    @field_validator("timestamp")
    @classmethod
    def require_timezone(cls, value: datetime) -> datetime:
        if value.tzinfo is None or value.utcoffset() is None:
            raise ValueError("timestamp must include a timezone offset")
        return value


class SleepSession(BaseModel):
    model_config = ConfigDict(extra="forbid")

    start_time: datetime
    end_time: datetime
    minutes_asleep: int = Field(ge=0)
    minutes_awake: int = Field(ge=0)

    @model_validator(mode="after")
    def validate_times(self) -> "SleepSession":
        if self.end_time <= self.start_time:
            raise ValueError("end_time must be after start_time")
        return self


class DashboardResponse(BaseModel):
    model_config = ConfigDict(extra="forbid")

    metrics: dict[str, MetricPoint] = Field(default_factory=dict)
    sleep: SleepSession | None = None
    steps: int | None = Field(default=None, ge=0)


class ChatRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    message: str = Field(min_length=1, max_length=5000)
    range: DateRange | None = None


class SyncJobResponse(BaseModel):
    model_config = ConfigDict(extra="forbid")

    job_id: str = Field(min_length=1, max_length=128)
    status: SyncStatus
    detail: str | None = None
