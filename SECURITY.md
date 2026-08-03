# Security policy

This project handles health data through a user-managed private gateway. Do not open issues containing Fitbit data, Google credentials, OAuth tokens, gateway tokens, Tailscale addresses, or logs with personal information.

## Local security

- Keep `.env`, SQLite databases, OAuth tokens, and gateway tokens outside Git.
- Use Tailscale ACLs and a long random gateway token.
- Store the gateway token only in Android encrypted storage.
- Do not log Authorization headers or health chat contents.
- Treat health output as personal information and do not use it for diagnosis.

## Reporting

For a suspected security issue, contact the repository maintainer privately rather than opening a public issue.
