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
| cftlib runtime: S2S simulator, dm-store, IDAM `/loginUser`, HMI | :8489 |
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
- **Hearings:** cftlib runs HMC on the Service Bus emulator. This service calls it at `HMC_API_URL` and receives hearing updates on the `hmc-to-cft` topic, through the subscription in `src/cftlib/servicebus.json`. HMI is a stand-in in cftlib's runtime that accepts each request; tests list a hearing, or have HMI reject a request, through the cftlib API. The system update user holds the `hearing-manager` and `hearing-viewer` roles HMC checks.
- **Users:** the professional users civil's Playwright tests sign in as (`hmcts.civil+organisation.*`), the system update user, the PRD admin and the cross-access user. All have the password `password`. See `src/cftlib/java/.../CivilCftlibConfig.java`.
- **Stand-ins for services outside CCD:**
  - Wiremock serves [civil-wiremock-mappings](https://github.com/hmcts/civil-wiremock-mappings) (fees, Docmosis, PRD, location ref data, send-letter, SendGrid and more), plus the stubs in `src/cftlib/wiremock` for GOV.UK Notify and the payments service request.
  - cftlib's runtime stands in for dm-store, keeping documents as files under `build/cftlib/dm-store`, and answers IDAM's `/loginUser`, which civil's Playwright tests sign in with and the IDAM simulator lacks.

Everything under `src/cftlib` runs only under cftlib. It holds configuration, and stand-ins only for what is particular to civil, such as its GOV.UK Notify client.

## Checking the stack

`./gradlew cftlibTest` starts the same stack and runs `src/cftlibTest` against it, in about five minutes:
- the claimant solicitor starts `CREATE_CLAIM_SPEC`, which runs this service's about-to-start callback against the imported definition;
- the CIVIL and GENERALAPPLICATION case types are in CCD;
- civil's business processes are deployed to Camunda;
- this service stores a document through CDAM and cftlib's dm-store and reads it back;
- tests can sign in through cftlib's `/loginUser`;
- a hearing HMI cannot list makes this service trigger `REVIEW_HEARING_EXCEPTION`, whose work allocation task appears in Camunda and is configured by task management;
- a listed hearing makes this service trigger `UPDATE_NEXT_HEARING_DETAILS`.

It is not part of `check`, since it needs Docker and access to `hmctsprod.azurecr.io` (`az acr login --name hmctsprod`).

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
