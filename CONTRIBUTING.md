# Contributing

Thanks for contributing. Keep the public repository generic and free of personal health data.

## Development

- Read the design and implementation plan in `docs/plans/`.
- Use tests first for gateway and Android changes.
- Keep credentials in local environment configuration only.
- Do not add personal Tailscale hostnames, IPs, tokens, Fitbit exports, or SQLite files.
- Run `python -m pytest gateway/tests -q` before submitting gateway changes.
- Android builds require Android Studio, JDK 17, and Android SDK 35.

## Pull requests

Describe the user-visible change, tests run, security/privacy implications, and any follow-up work. Keep commits focused and avoid unrelated changes.
