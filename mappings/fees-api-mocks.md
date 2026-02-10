# Fees API WireMock Mappings

This document describes the WireMock mappings used to mock the Fees Register API in preview environments.

## Health Endpoints

| Mock File | Endpoint | Purpose |
|-----------|----------|---------|
| `fees-actuator-health.json` | `GET /actuator/health` | Main actuator health check with component details |
| `fees-actuator-health-liveness.json` | `GET /actuator/health/liveness` | Kubernetes liveness probe endpoint |
| `fees-actuator-health-readiness.json` | `GET /actuator/health/readiness` | Kubernetes readiness probe endpoint |
| `fees-health.json` | `GET /health` | Legacy health check endpoint with component details |
| `fees-health-liveness.json` | `GET /health/liveness` | Legacy liveness probe endpoint |
| `fees-health-readiness.json` | `GET /health/readiness` | Legacy readiness probe endpoint |

## Money Claim Fee Lookups

| Mock File | Endpoint | Query Parameters | Purpose |
|-----------|----------|------------------|---------|
| `fees-lookup-money-claim.json` | `GET /fees-register/fees/lookup` | `keyword=MoneyClaim` | Standard money claim fee lookup (fallback) |
| `fees-lookup-money-claim-1500.json` | `GET /fees-register/fees/lookup` | `keyword=MoneyClaim`, `amount_or_volume=1500` | Money claim fee for £1,500 claims (£80) |
| `fees-lookup-money-claim-small.json` | `GET /fees-register/fees/lookup` | `keyword=MoneyClaim`, `amount_or_volume=11000` | Money claim fee for £11,000 claims (£550) |
| `fees-lookup-money-claim-intermediate-track.json` | `GET /fees-register/fees/lookup` | `keyword=MoneyClaim`, `amount_or_volume=99000` | Intermediate track fee for £99,000 claims (£4,950) |
| `fees-lookup-money-claim-multi-track.json` | `GET /fees-register/fees/lookup` | `keyword=MoneyClaim`, `amount_or_volume=200001` | Multi-track fee for claims over £200,000 (£10,000 max) |
| `fees-lookup-money-claim-no-keyword.json` | `GET /fees-register/fees/lookup` | `event=issue`, `amount_or_volume=*` | Money claim fallback without keyword |

## Hearing Fee Lookups

| Mock File | Endpoint | Query Parameters | Purpose |
|-----------|----------|------------------|---------|
| `fees-lookup-hearing-small-claims.json` | `GET /fees-register/fees/lookup` | `keyword=HearingSmallClaims` | Small claims track hearing fee (£50) |
| `fees-lookup-hearing-fast-track.json` | `GET /fees-register/fees/lookup` | `keyword=FastTrackHrg` | Fast track hearing fee (£60) |
| `fees-lookup-hearing-multi-track.json` | `GET /fees-register/fees/lookup` | `keyword=MultiTrackHrg` | Multi-track hearing fee (£70) |

## General Application Fee Lookups

| Mock File | Endpoint | Query Parameters | Purpose |
|-----------|----------|------------------|---------|
| `fees-lookup-ga-with-notice.json` | `GET /fees-register/fees/lookup` | `keyword=GAOnNotice` | General application on notice (£100) |
| `fees-lookup-ga-without-notice.json` | `GET /fees-register/fees/lookup` | `keyword=GeneralAppWithoutNotice` | GA without notice or by consent (£110) |
| `fees-lookup-ga-uncloak.json` | `GET /fees-register/fees/lookup` | `keyword=HACFOOnNotice` | HACFO application to lift stay/uncloak (£184) |

## Other Fee Lookups

| Mock File | Endpoint | Query Parameters | Purpose |
|-----------|----------|------------------|---------|
| `fees-lookup-certificate-of-satisfaction.json` | `GET /fees-register/fees/lookup` | `keyword=CoS` | Certificate of satisfaction/cancellation (£15) |
| `fees-lookup-vary-suspend.json` | `GET /fees-register/fees/lookup` | `keyword=AppnToVaryOrSuspend` | Application to vary or suspend enforcement (£120) |
| `fees-lookup-fallback.json` | `GET /fees-register/fees/lookup` | *(catch-all, priority 10)* | Default fallback for any unmatched fee lookup |

## Fee Range Group Lookups

| Mock File | Endpoint | Query Parameters | Purpose |
|-----------|----------|------------------|---------|
| `fees-range-group.json` | `GET /fees-register/fees` | `service=civil money claims`, `event=issue`, `feeVersionStatus=approved` | Returns full fee range group for civil money claims |
| `fees-range-group-fallback.json` | `GET /fees-register/fees` | `feeVersionStatus=approved` *(catch-all, priority 10)* | Default fallback for any fee range group request |

## Priority Rules

WireMock uses priority to determine which mapping to use when multiple mappings match a request:
- **Priority 1**: Specific amount-based lookups (e.g., `amount_or_volume=1500`)
- **Priority 5**: Keyword-based lookups (default)
- **Priority 10**: Catch-all fallbacks

Lower priority number = higher precedence.

## Loading Mappings

Mappings are loaded into WireMock using the `bin/load-wiremock-mappings.sh` script, which:
1. Resets all existing mappings to avoid duplicates
2. Posts each mapping file to the WireMock Admin API

```bash
export WIREMOCK_URL="https://wiremock-<service-name>.preview.platform.hmcts.net"
./bin/load-wiremock-mappings.sh
```
