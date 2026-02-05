---
name: WireMock Easiest Service Plan
overview: Recommend which downstream API is easiest to mock first in civil-service using the existing WireMock setup, and outline a phased approach to add more mocks for pipeline stability.
todos: []
isProject: false
---

# WireMock: Easiest Service to Mock First and Rollout Plan

## Current state

- **WireMock is already in use** in civil-service preview: [values.preview.template.yaml](charts/civil-service/values.preview.template.yaml) points three APIs to WireMock and [load-wiremock-mappings.sh](bin/load-wiremock-mappings.sh) runs in Jenkins before functional tests ([Jenkinsfile_CNP](Jenkinsfile_CNP) around line 174).
- **Already mocked (URL + mappings):**
  - **Docmosis** (`DOCMOSIS_TORNADO_URL` → wiremock): 1 endpoint `POST /rs/render` — mapping in [mappings/docmosis.json](mappings/docmosis.json).
  - **RD Professional / Organisation** (`RD_PROFESSIONAL_API_URL` → wiremock): 3 endpoints — mappings in [mappings/](mappings/) (organisation1/2/3, userOrganisationByExternalApi, civilDamagesClaimsOrganisation1ByInternalApi).
- **URL points to WireMock but no mappings in repo:** **HMC (Hearings)** (`HMC_API_URL` → wiremock). HearingsApi has 5 endpoints; adding mappings would complete this mock and reduce flakiness if FTs touch hearings.

APIs still hitting real services in preview (see [values.preview.template.yaml](charts/civil-service/values.preview.template.yaml) and [values.yaml](charts/civil-service/values.yaml)): Fees, Claim Store, Role Assignment, Location Ref (GENAPP_LRD_URL), Case Assignment (ACA), Send Letter, Document Management, Payments, etc.

---

## Recommendation: Easiest service to mock first — **Fees API**

**Why Fees API is the best first candidate**

1. **Single base URL** — `FEES_API_URL` is already used in [application.yaml](src/main/resources/application.yaml) (e.g. `fees.api.url`). One env change in preview points all fee lookups to WireMock.
2. **Feign client** — [FeesApiClient](src/main/java/uk/gov/hmcts/reform/civil/client/FeesApiClient.java) is a standard Feign client; no SDK or async complexity.
3. **Contract tests define the contract** — [FeesLookupApiConsumerTest](src/contractTest/java/uk/gov/hmcts/reform/civil/consumer/FeesLookupApiConsumerTest.java) and [FeesRangeGroupApiConsumerTest](src/contractTest/java/uk/gov/hmcts/reform/civil/consumer/FeesRangeGroupApiConsumerTest.java) document exact paths, query params and response shapes (e.g. `ENDPOINT = "/fees-register/fees/lookup"` with `service`, `jurisdiction1`, `jurisdiction2`, `channel`, `event`, `keyword`, optional `amount_or_volume`). You can derive WireMock stubs from these.
4. **Limited endpoints** — Two main patterns: fee lookup with amount, fee lookup without amount; plus range/group endpoints. A small set of mappings can cover the FT scenarios (e.g. issue fee, hearing fees, GA fees).
5. **On critical path** — Fee lookup is used for issue, hearings and general applications; mocking it removes dependency on fees-register-api availability and latency during FTs.

**Alternative first choice: Claim Store API**

- Also easy: Feign client, single `CLAIM_STORE_URL`, 3 endpoints ([ClaimStoreApi](src/main/java/uk/gov/hmcts/reform/cmc/client/ClaimStoreApi.java)), and consumer tests ([CmcClaimsForClaimantApiConsumerTest](src/contractTest/java/uk/gov/hmcts/reform/civil/consumer/CmcClaimsForClaimantApiConsumerTest.java), etc.). Good second candidate if FTs are more often blocked by claim-store than by fees.

---

## Implementation plan (Fees API first)

### Phase 1: Add Fees API to WireMock (recommended first)

1. **Add WireMock mapping(s) under [mappings/**](mappings/)
  - Create JSON mapping(s) for `/fees-register/fees/lookup` (and any range/group endpoints used in FTs).  
  - Use contract tests and [FeesApiClient](src/main/java/uk/gov/hmcts/reform/civil/client/FeesApiClient.java) for path and query parameters.  
  - Support at least: success (200 + valid fee response body). Optionally add one error scenario (e.g. 404 or 500) for resilience testing.  
  - Use `urlPathPattern` or `queryParameters` so one mapping can cover the main lookup variants used in FTs, or add a few specific mappings for issue/hearing/GA keywords.
2. **Point Fees API to WireMock in preview**
  - In [values.preview.template.yaml](charts/civil-service/values.preview.template.yaml), set:
    - `FEES_API_URL: http://${SERVICE_NAME}-wiremock`
  - No code changes; only config.
3. **Verify**
  - Run pipeline (smoketest:preview / functional).  
  - Confirm fee-dependent journeys (e.g. issue claim, create GA) pass and that no real calls go to fees-register-api in preview.
4. **Optional**
  - Add a small number of scenario-specific mappings (e.g. different keywords) if FTs need them; keep responses minimal and deterministic.

### Phase 2: Complete HMC (Hearings) mock

- **HMC_API_URL** already points to WireMock; add mapping files under [mappings/](mappings/) for the [HearingsApi](src/main/java/uk/gov/hmcts/reform/hmc/client/HearingsApi.java) endpoints used in FTs (e.g. `GET /hearing/{id}`, `GET /hearings/{caseId}`, etc.).  
- Use [HearingsApiConsumerTest](src/contractTest/java/uk/gov/hmcts/reform/civil/consumer/HearingsApiConsumerTest.java) for request/response shapes.  
- Reduces flakiness when FTs hit hearing-related flows.

### Phase 3: Add more APIs in order of ease and impact


| Order | API                                          | Why                                                                                                                                                                  |
| ----- | -------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1     | **Fees**                                     | Single URL, Feign, contract tests, critical path (done in Phase 1).                                                                                                  |
| 2     | **HMC (Hearings)**                           | URL already wiremock; add mappings (Phase 2).                                                                                                                        |
| 3     | **Claim Store**                              | Feign, 3 endpoints, contract tests; high impact for claimant/defendant lists.                                                                                        |
| 4     | **Location Reference Data**                  | Feign, `GENAPP_LRD_URL`, [LocationReferenceDataApiConsumerTest](src/contractTest/java/uk/gov/hmcts/reform/civil/consumer/LocationReferenceDataApiConsumerTest.java). |
| 5     | **Case Assignment**                          | 2 endpoints, [CaseAssignmentApiConsumerTest](src/contractTest/java/uk/gov/hmcts/reform/civil/consumer/CaseAssignmentApiConsumerTest.java); used for NoC.             |
| 6     | **Role Assignments**                         | Used in many flows; add when prioritised.                                                                                                                            |
| 7     | **Send Letter / Evidence Management / etc.** | As needed; SendGrid is SDK-based and may need a different approach (e.g. HTTP proxy or test double).                                                                 |


---

## Technical notes

- **Loading mappings:** Jenkins runs `./bin/load-wiremock-mappings.sh` before FTs; it POSTs every JSON file from `./mappings` to `$WIREMOCK_URL/__admin/mappings`. So any new mapping file in [mappings/](mappings/) is picked up; no chart change required for loading.  
- **Body files:** If a mapping uses `response.bodyFileName`, the file must live under `__files/` (see [load-wiremock-mappings.sh](bin/load-wiremock-mappings.sh)); the script inlines bodies when loading. For fees, inline JSON in the mapping is usually enough.  
- **ConfigMap:** The chart mounts a ConfigMap `wiremock-wm` at `/home/wiremock/mappings`. How that ConfigMap is populated is pipeline/chart-specific; the important part for new mocks is adding JSON to the repo’s `mappings/` and ensuring the load script runs (it already does).  
- **Other services (civil-general-applications, civil-ccd-definitions, etc.):** The same pattern applies: add WireMock mapping files and point that service’s API base URL to the WireMock instance in the relevant preview/FT environment. civil-service is the best place to start because WireMock and the load script are already there.

---

## Summary

- **Easiest and recommended first mock:** **Fees API** — one env var change in preview plus a small set of mapping JSONs derived from existing contract tests.  
- Then complete **HMC** with mappings, then add **Claim Store**, **Location Ref**, and **Case Assignment** in that order for a good balance of ease and pipeline stability.

