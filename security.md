# Security Policy

## Purpose

This document outlines how security vulnerabilities should be reported for this
repository.

HMCTS is committed to responsible vulnerability disclosure and to addressing
legitimate security issues in a timely and coordinated manner.

## Reporting a vulnerability

If you believe you have identified a security vulnerability in this repository, please report it by email to: 

HMCTSVulnerabilityDisclosure@justice.gov.uk

This email address is the sole approved point of contact for vulnerability disclosures relating to HMCTS-owned repositories and services.

Please **do not** create public GitHub issues or pull requests to report security vulnerabilities.

## What to Include in a Report

When reporting a vulnerability, please provide as much of the following information as possible:

- The repository, service, or component affected
- A clear description of the vulnerability
- Steps required to reproduce the issue
- Any non-destructive proof of concept or exploitation details

Where available, the following additional information is helpful:

- The suspected vulnerability type (for example, an OWASP category)
- Relevant logs, screenshot or error messages

Reports do not need to be fully validated before submission. If you are unsure whether an issue is exploitable or security-relevant, you are still encouraged to report it.

## Responsible Disclosure Guidelines

When investigating or reporting a vulnerability affecting HMCTS systems, reporters must not:

- Break the law or breach applicable regulations
- Access unnecessary, excessive, or unrelated data
- Modify or delete data
- Perform denial-of-service or other disruptive testing
- Use high-intensity, invasive, or destructive scanning techniques
- Publicly disclose the vulnerability before it has been addressed
- Attempt social engineering, Phishing, or physical attacks
- Demand payment or compensation in exchange for disclosure

These guidelines are intended to protect users, services, and data while allowing good-faith security research.


## Bug Bounty

HMCTS does not operate a paid bug bounty programme.

## Code of Conduct

All contributors and reporters are expected to act in good faith and in accordance with applicable laws and professional standards.

## ZAP findings — civil-service decisions

This section documents the security scan posture for civil-service, including
accepted residual risks, remediation decisions, and ownership boundaries.
It was last reviewed in June 2026.

### Swagger / OpenAPI exposure

Swagger UI and OpenAPI spec endpoints (`/swagger-ui.html`, `/swagger-ui/**`,
`/v3/api-docs/**`) are enabled and unauthenticated across all environments.

**Decision: accepted.** The OpenAPI schema only describes the API contract;
all business endpoints require IDAM user authentication and role-based
authorisation. The schema is also published via `OpenAPIPublisherTest` on
master for downstream consumers. Disabling it would reduce convenience for
developers without materially reducing attack surface.

### Testing-support endpoints

Testing-support endpoints (`/testing-support/**`) are conditionally enabled via
`TESTING_SUPPORT_ENABLED` (default `false`). They are active in AAT PR staging
and preview environments to support functional test automation.
### Response hardening headers

The following headers are enforced via `SecurityConfiguration`:

- `X-Content-Type-Options: nosniff` (addresses ZAP rule 10021)
- `X-Frame-Options: SAMEORIGIN`
- `Referrer-Policy: no-referrer`
- `Permissions-Policy` (restrictive default)
- `Strict-Transport-Security` (1 year, includeSubDomains)
- `Cache-Control: no-cache, no-store, max-age=0, must-revalidate`
### Cookie-related findings (ZAP 10010, 10054, 90033)

Civil-service is stateless (`SessionCreationPolicy.STATELESS`), does not use
`HttpSession`, and sets no cookies anywhere in application code.

Cookies observed in ZAP scans against AAT originate from upstream
infrastructure layers:

- **Azure Application Gateway / Front Door** affinity cookies (e.g.
  `ARRAffinity`, `ARRAffinitySameSite`)
- **IDAM** session cookies on proxied authentication paths

**Decision: accepted as upstream responsibility.** Cookie hardening (HttpOnly,
SameSite, Secure, domain scope) must be applied at the layer that sets them.
The per-URL ignores in `audit.json` and the global rule suppressions in
`zap_hooks.py` (rules 90033, 10010, 10054) reflect this ownership boundary.
Remediation should be raised with HMCTS platform-engineering (for App Gateway
cookies) and the IDAM/identity team (for session cookies).

### ZAP scan rule suppressions (zap_hooks.py)

The following passive scan rules are globally suppressed in `zap_hooks.py`
with justification:

| Rule  | Name                                | Justification |
|-------|-------------------------------------|---------------|
| 90033 | Loosely Scoped Cookie               | Cookies set by Azure App Gateway / IDAM, not civil-service |
| 10010 | Cookie No HttpOnly Flag             | Same upstream cookie origin as 90033 |
| 10054 | Cookie without SameSite Attribute   | Same upstream cookie origin as 90033 |
| 10023 | Info Disclosure - Debug Error Msgs  | Error message reduction handled by sanitised `@ControllerAdvice` handlers |
| 10096 | Timestamp Disclosure - Unix         | False positives from numeric case IDs and epoch-based identifiers |
| 100000| Client Error response code          | Expected 4xx from ZAP probing endpoints that require valid payloads or auth |
| 100001| Unexpected Content-Type             | Binary document download endpoints return non-JSON content types |

Rule **10021** (X-Content-Type-Options Header Missing) was previously
suppressed. It has been **removed from the suppression list** so it will alert
if the header is ever missing, serving as a regression detector.

### ZAP per-URL ignores (audit.json)

Per-URL ignores in `audit.json` cover specific endpoint + rule combinations
that generate noise in an unauthenticated scan context. The file includes a
`_comment` key pointing back to this document. Entries should be reviewed when
endpoints are added or removed.

Legacy `/v2/api-docs` entries (rules 10010, 10054, 90033, 10096, 90022) were
removed in the June 2026 review because the application uses springdoc
OpenAPI v3 (`/v3/api-docs`) and no longer serves the v2 endpoint.
## Further Reading

- https://www.ncsc.gov.uk/information/vulnerability-reporting
- https://www.gov.uk/help/report-vulnerability
- https://github.com/Trewaters/security-README
