# Running civil locally on rse-cft-lib

`./gradlew bootWithCCD` runs this service with a local CCD stack ([rse-cft-lib](https://github.com/hmcts/rse-cft-lib)), with cftlib's `wa` (work allocation) and `hmc` (hearings) features:

| Service | Where |
|---|---|
| civil-service | http://localhost:4000 |
| ExUI | http://localhost:3000 |
| CCD data store, definition store, user profile | :4452, :4451, :4453 |
| Manage case assignment (AAC) | :4454 |
| Case document AM (CDAM) | :4455 |
| Role assignment (AM) | :4096 |
| IDAM simulator | :5062 |
| cftlib runtime: S2S simulator, dm-store, IDAM `/loginUser`, HMI and its page for answering hearing requests | :8489 (HMI page: http://localhost:8489/hmi) |
| Camunda | http://localhost:8999 (cockpit login `admin` / `admin`) |
| Work allocation: task management, case event handler, workflow API, task monitor | :8087, :8088, :8099, :8077 |
| Hearings: hmc-cft-hearing-service, HMI inbound and outbound adapters | :4561, :4559, :4558 |
| Service Bus emulator | :5672 (AMQP) |
| Wiremock | http://localhost:8765 |
| Postgres | :6432 (`postgres` / `postgres`; civil's database is `cmc`) |

Nothing reaches AAT, so no VPN is needed.

## What it sets up

- **CCD definitions:** the CIVIL and GENERALAPPLICATION JSON from civil-ccd-definition, with callbacks to this service. It uses `CCD_DEFINITION_BRANCH` (default `master`), or the checkout named by `CIVIL_CCD_DEFINITIONS_DIR`. Like preview, it leaves out `*-prod.json` and the shuttered authorisations.
- **Business processes:** the BPMN files in `src/main/resources/camunda`, deployed to Camunda once it is up.
- **Work allocation:** civil-wa-task-configuration's DMNs, deployed with the tenant `civil`. It uses `WA_TASK_CONFIGURATION_BRANCH` (default `master`), or the folder of DMNs named by `CIVIL_WA_DMNS_DIR`. cftlib delivers each case event whose definition publishes it to wa-case-event-handler, so tasks appear in Camunda and task management as they would in an environment. As in AAT, WA's system user has `caseworker-civil` and `caseworker-civil-systemupdate` (`RSE_LIB_WA_SYSTEM_USER_ROLES`), which let task management read the case to configure its tasks.
- **Hearings:** cftlib runs HMC on the Service Bus emulator. This service calls it at `HMC_API_URL` and receives hearing updates on the `hmc-to-cft` topic, through the subscription in `src/cftlib/servicebus.json`. HMI and List Assist are a stand-in in cftlib's runtime that accepts each request. People answer the requests from its page, and tests through the cftlib API (see [Testing hearings by hand](#testing-hearings-by-hand)). ExUI's hearings calls go to this HMC, not AAT's.
- **Users:** a login for each kind of civil user: solicitors and their organisations' superusers, judges (district, circuit, leadership and fee-paid), hearing centre admins, legal operations, CTSC, the National Business Centre, the Welsh Language Unit, citizens, and the system update, PRD admin and case access users. They are the users civil-ccd-definition's CodeceptJS and Playwright suites sign in as, with the same emails. The login page lists them by kind, and all have the password `password`. See `src/cftlib/java/.../CivilTestUsers.java`.
  - Staff and judges hold the organisational roles `bin/shared/add-org-roles-to-users.sh` gives them in AAT and preview, so their work allocation tasks and hearing access match. A user the suites name but the script doesn't takes the roles of the user it stands in for. Startup fails if the script gives roles to a user `CivilTestUsers` doesn't create.
  - Their IDAM roles are chosen to match the access civil's definitions give each kind of user, and the role names ExUI reads a user's category from.
  - The system update user also holds the `hearing-manager` and `hearing-viewer` roles HMC checks.
- **Stand-ins for services outside CCD:**
  - Wiremock serves [civil-wiremock-mappings](https://github.com/hmcts/civil-wiremock-mappings) (fees, Docmosis, PRD, location ref data, send-letter, SendGrid and more), plus the stubs in `src/cftlib/wiremock` for GOV.UK Notify and the payments service request.
  - ExUI's reference data comes from cftlib's [reference data stand-in](https://github.com/hmcts/rse-cft-lib#reference-data), which `hmc` brings:
    - the lists of values (hearing types, case types, facilities, judge types, cancellation reasons and more) and case flags in `src/cftlib/refdata`, in the shape rd-commondata-api returns;
    - civil-wiremock-mappings' court venues and hearing channels, which `startCftlibWiremock` adds;
    - the local judges, with the personal codes in `CivilTestUsers`.

    To change a list, edit its file under `src/cftlib/refdata`; the stand-in reads it on each request. A list ExUI asks for that no file has is empty.
  - cftlib's runtime stands in for dm-store, keeping documents as files under `build/cftlib/dm-store`, and answers IDAM's `/loginUser`, which civil's Playwright tests sign in with and the IDAM simulator lacks.

Everything under `src/cftlib` runs only under cftlib. It holds configuration, and stand-ins only for what is particular to civil, such as its GOV.UK Notify client.

## Checking the stack

`./gradlew cftlibTest` starts the same stack and runs `src/cftlibTest` against it, in about five minutes:
- the claimant solicitor starts `CREATE_CLAIM_SPEC`, which runs this service's about-to-start callback against the imported definition;
- the CIVIL and GENERALAPPLICATION case types are in CCD;
- civil's business processes are deployed to Camunda;
- this service stores a document through CDAM and cftlib's dm-store and reads it back;
- tests can sign in through cftlib's `/loginUser`;
- `MOVE_TO_DECISION_OUTCOME` runs its business process in Camunda to the end;
- a hearing HMI cannot list makes this service trigger `REVIEW_HEARING_EXCEPTION`, whose work allocation task is created and configured;
- `CASE_PROCEEDS_IN_CASEMAN` cancels that task, and wa-task-monitor's termination job terminates it;
- a listed hearing makes this service trigger `UPDATE_NEXT_HEARING_DETAILS`, whose callback reads the hearing back from HMC, and work allocation reconfigures the case's tasks.

`CivilIntegrationsCftlibTest` holds these journeys. Its cases are written straight into the data store with only the fields the events read, so they start part way through a claim.

It is not part of `check`, since it needs Docker and access to `hmctsprod.azurecr.io` (`az acr login --name hmctsprod`).

## Testing hearings by hand

There is no List Assist locally. A hearing goes from request to listing like this:

1. **Request it in ExUI.** Sign in as a hearing centre admin, such as Hearing Centre Admin Region 1, open the case, and choose **Request a hearing** on its **Hearings** tab. ExUI builds the request from this service's `serviceHearingValues` and the reference data in wiremock, and sends it to the local HMC. A case at judicial referral or later works; civil-ccd-definition's API tests make one, for example `yarn playwright test playwright-e2e/tests/api-tests/hearings/api_1v2SS_spec_fast_hearings.test.ts --project civil-service-nightly --no-deps`.
2. **Answer it at http://localhost:8489/hmi.** HMC sends the request to cftlib's stand-in for HMI, which accepts it, and the hearing waits in `AWAITING_LISTING`. The page lists each hearing HMC has sent a request for since the stack started, newest first, with its status in HMC, and offers the answers HMC accepts in that status:

| Answer on the page | Offered when the hearing is | HMC moves it to | This service then triggers |
|---|---|---|---|
| **List**, at the time given | Awaiting listing, updated, or already listed | `LISTED` | `UPDATE_NEXT_HEARING_DETAILS`, which reads the hearing back from HMC |
| **Confirm cancellation** | Cancellation requested | `CANCELLED` | `UPDATE_NEXT_HEARING_DETAILS` |
| **Cannot list**, with an error code and description | Any of the above | `EXCEPTION` | `REVIEW_HEARING_EXCEPTION`, whose work allocation task is created |

- A listing takes its venue, duration and judge from the hearing's latest request. Its time defaults to the first working day the request allows, at 10:00. Change the time before listing to test a hearing on a particular day.
- **Reject the next request** answers the next hearing request, for any case or one case reference, with an error, as HMI does a request it cannot accept. The hearing goes straight to `EXCEPTION`.
- Refresh the page to see HMC's status after answering. The case's events and tasks follow once this service has handled HMC's update from the `hmc-to-cft` topic.
- **Automatic responses**, switched on from the page or with `RSE_LIB_HMI_AUTO_RESPOND=true ./gradlew bootWithCCD`, list every new or amended hearing, and confirm every cancellation, without waiting. Use this when hearings only need to be listed to get a case to a later step.

The page's actions are also an API, for UI tests and scripts, such as:

```bash
curl -X POST http://localhost:8489/hmi/requests/<hearing id>/listing -d start=2026-12-01T10:00
```

See [rse-cft-lib's README](https://github.com/hmcts/rse-cft-lib#hearings-and-the-hmi-stand-in) for the full list.

## Running civil's Playwright tests against it

In civil-ccd-definition, create `.env.tests.local` (it is gitignored) pointing the tests at the local services:

```properties
ENVIRONMENT=preview
URL=http://localhost:3000
CIVIL_SERVICE_URL=http://localhost:4000
CCD_DATA_STORE_URL=http://localhost:4452
# The tests sign in with IDAM's /loginUser, which cftlib's runtime answers from the IDAM simulator.
IDAM_API_URL=http://localhost:8489
IDAM_WEB_URL=http://localhost:5062
IDAM_TEST_SUPPORT_API_URL=http://localhost:5062
SERVICE_AUTH_PROVIDER_API_BASE_URL=http://localhost:8489
AAC_API_URL=http://localhost:4454
PAYMENT_API_URL=http://localhost:8765
DM_STORE_URL=http://localhost:8489/dm-store
WIREMOCK_URL=http://localhost:8765
DEFAULT_PASSWORD=password
SYSTEM_USER_PASSWORD=password
PLAYWRIGHT_TEST_SPEED=fast
PLAYWRIGHT_WORKERS=1
PLAYWRIGHT_RETRIES=0
PLAYWRIGHT_EXUI_RETRIES=0
PLAYWRIGHT_IDAM_RETRIES=0
PLAYWRIGHT_RUN_DATA_SETUP=false
PLAYWRIGHT_RUN_EXUI_USER_DATA_SETUP=false
PLAYWRIGHT_RUN_CUI_USER_SETUP=false
PLAYWRIGHT_RUN_CUI_USER_DATA_SETUP=false
```

This service's testing-support endpoints are on, as in preview, so the API tests can assign case roles and wait for business processes.

Then run the civil-service smoke tests, which take about 35 seconds:

```bash
yarn playwright test --project civil-service-master-pr-smoke --no-deps
```

## Decentralised CCD

`CIVIL_DECENTRALISED_ENABLED=true ./gradlew bootWithCCD` keeps CIVIL and GENERALAPPLICATION cases in this service's database, as preview does on the decentralised branch.

## Troubleshooting

- **`DB not yet available...` repeats forever:** the shared `cftlib` Postgres volume was created by a different major version. civil uses Postgres 15, as dtsse-ccd-config-generator's e2e does. `RSE_LIB_CLEAN_BOOT=1` recreates the containers and their data.
- **`/health` returns 405:** one health indicator fails against a library version on cftlib's classpath. `/health/liveness` and `/health/readiness` work.
- **Startup hangs in a Flyway migration:** a `CREATE INDEX CONCURRENTLY` migration waits on a transactional Flyway lock. bootWithCCD sets `SPRING_FLYWAY_POSTGRESQL_TRANSACTIONAL_LOCK=false`, as preview does.
