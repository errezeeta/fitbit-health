from datetime import date

import pytest
from pydantic import ValidationError

from app.schemas import (
    ChatRequest,
    DashboardResponse,
    DateRange,
    MetricPoint,
    SleepSession,
    SyncJobResponse,
)


def test_date_range_accepts_presets_and_custom_dates():
    assert DateRange(preset="7d").preset == "7d"
    custom = DateRange(start=date(2026, 1, 1), end=date(2026, 1, 7))
    assert custom.start < custom.end


def test_date_range_rejects_invalid_ranges_and_presets():
    with pytest.raises(ValidationError):
        DateRange(preset="2d")
    with pytest.raises(ValidationError):
        DateRange(start=date(2026, 1, 8), end=date(2026, 1, 1))


def test_health_payload_models_are_strict_and_null_safe():
    point = MetricPoint(timestamp="2026-08-03T07:22:00+02:00", value=None)
    sleep = SleepSession(
        start_time="2026-08-03T01:34:00+02:00",
        end_time="2026-08-03T07:22:00+02:00",
        minutes_asleep=342,
        minutes_awake=6,
    )
    dashboard = DashboardResponse(sleep=sleep, metrics={"hrv": point})
    assert dashboard.sleep.minutes_asleep == 342
    assert dashboard.metrics["hrv"].value is None


def test_chat_request_is_bounded():
    request = ChatRequest(message="¿Cómo dormí?", range=DateRange(preset="7d"))
    assert request.message == "¿Cómo dormí?"
    with pytest.raises(ValidationError):
        ChatRequest(message="x" * 5001)


def test_sync_job_response_has_allowed_status():
    response = SyncJobResponse(job_id="job-1", status="queued")
    assert response.status == "queued"
    with pytest.raises(ValidationError):
        SyncJobResponse(job_id="job-1", status="unknown")


def test_dashboard_requires_expected_shape():
    response = DashboardResponse()
    assert response.metrics == {}
    assert response.steps is None
    assert response.sleep is None


def test_metric_point_rejects_naive_timestamp():
    with pytest.raises(ValidationError):
        MetricPoint(timestamp="2026-08-03T07:22:00", value=1)


def test_date_range_rejects_ranges_over_365_days():
    with pytest.raises(ValidationError):
        DateRange(start=date(2024, 1, 1), end=date(2026, 1, 1))

# The implementation is intentionally absent at this TDD step; collection should fail.
