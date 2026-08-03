from app.timezone import format_local_timestamp, utc_to_local


def test_summer_timestamp_uses_cest():
    value = utc_to_local("2026-08-03T05:22:00Z", "Europe/Madrid")
    assert value == "2026-08-03T07:22:00+02:00"


def test_winter_timestamp_uses_cet():
    value = utc_to_local("2026-01-03T05:22:00Z", "Europe/Madrid")
    assert value == "2026-01-03T06:22:00+01:00"


def test_sleep_session_crossing_midnight_is_preserved():
    start = format_local_timestamp("2026-08-02T23:34:00Z", "Europe/Madrid")
    end = format_local_timestamp("2026-08-03T05:22:00Z", "Europe/Madrid")
    assert start.endswith("T01:34:00+02:00")
    assert end.endswith("T07:22:00+02:00")
    assert start < end


def test_invalid_timestamp_is_rejected():
    try:
        utc_to_local("not-a-timestamp", "Europe/Madrid")
    except ValueError as error:
        assert "timestamp" in str(error)
    else:
        raise AssertionError("invalid timestamp was accepted")


def test_invalid_timezone_is_rejected():
    try:
        utc_to_local("2026-08-03T05:22:00Z", "Not/AZone")
    except ValueError as error:
        assert "timezone" in str(error)
    else:
        raise AssertionError("invalid timezone was accepted")


def test_naive_input_is_rejected():
    try:
        utc_to_local("2026-08-03T05:22:00", "Europe/Madrid")
    except ValueError as error:
        assert "UTC" in str(error)
    else:
        raise AssertionError("naive timestamp was accepted")


def test_non_string_input_is_rejected():
    try:
        utc_to_local(None, "Europe/Madrid")
    except ValueError as error:
        assert "timestamp" in str(error)
    else:
        raise AssertionError("non-string timestamp was accepted")


def test_explicit_format_helper_matches_converter():
    assert format_local_timestamp("2026-08-03T05:22:00Z", "Europe/Madrid") == utc_to_local(
        "2026-08-03T05:22:00Z", "Europe/Madrid"
    )


# Implementation is intentionally absent at this TDD step.
# The import above should fail before gateway/app/timezone.py exists.
