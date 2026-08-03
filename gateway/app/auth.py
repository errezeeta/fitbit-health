from __future__ import annotations

import secrets

from fastapi import HTTPException, status


def require_gateway_token(
    authorization: str | None,
    *,
    expected_token: str,
) -> bool:
    """Validate a Bearer token without leaking comparison details."""
    if not expected_token:
        raise ValueError("gateway token must be configured")
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="missing or malformed authorization",
            headers={"WWW-Authenticate": "Bearer"},
        )
    supplied = authorization.removeprefix("Bearer ").strip()
    if not supplied or not secrets.compare_digest(supplied, expected_token):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="invalid authorization",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return True
