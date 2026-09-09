# Security Policy

## Supported versions

| Version | Supported |
|---------|-----------|
| 0.1.x   | Yes       |
| < 0.1   | No        |

Only the latest minor line on the current major receives security fixes while the project is pre-1.0.

## Reporting a vulnerability

**Do not** file a public GitHub issue for security vulnerabilities.

Please report privately via one of:

1. GitHub **Security Advisories** → [Report a vulnerability](https://github.com/vishalsharma7nov/kmp-proto/security/advisories/new) (preferred)
2. Email the maintainer listed on the GitHub profile for `vishalsharma7nov` with subject `[SECURITY] kmp-proto`

Include:

- Affected version / commit
- Description and impact
- Reproduction steps or proof-of-concept (if safe to share)
- Any suggested fix

We aim to acknowledge reports within **72 hours** and to provide a remediation plan or status update within **7 days**.

## Disclosure

We follow coordinated disclosure:

1. Confirm and triage privately
2. Prepare a fix and, when appropriate, a GitHub Security Advisory
3. Publish a patched release and credit the reporter (unless they prefer anonymity)

## Security-sensitive areas in this library

Treat these as high priority when reviewing changes:

- Auth header / token handling (`getHeaders`, auth interceptors)
- TLS / `baseUrl` validation (reject relative or unexpected schemes)
- Response size limits (`maxResponseBytes`)
- Protobuf decode of untrusted payloads
- Credential use for GitHub Packages / Buf / proto sync (never log tokens)

## Application responsibilities

Consumers must:

- Store tokens securely (Keystore / Keychain / server-side) — do not hardcode secrets in apps
- Use HTTPS in production
- Pin proto sources (`ref` / lock file) in CI
- Keep dependencies and this library updated

## Safe harbor

Good-faith security research that follows this policy and avoids privacy/service disruption is welcome.
