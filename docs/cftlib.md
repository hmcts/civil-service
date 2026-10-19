# Running civil locally on rse-cft-lib

`./gradlew bootWithCCD` runs this service with a local CCD stack ([rse-cft-lib](https://github.com/hmcts/rse-cft-lib)):

| Service | Where |
|---|---|
| civil-service | http://localhost:4000 |
| ExUI | http://localhost:3000 |
| CCD data store, definition store, user profile | :4452, :4451, :4453 |
| Manage case assignment (AAC) | :4454 |
| Case document AM (CDAM) | :4455 |
| Role assignment (AM) | :4096 |
| IDAM simulator | :5062 |
| S2S simulator | :8489 |
| Camunda | http://localhost:8999 (cockpit login `admin` / `admin`) |
| Wiremock | http://localhost:8765 |
| Postgres | :6432 (`postgres` / `postgres`; civil's database is `cmc`) |

Nothing reaches AAT, so no VPN is needed.

## What it sets up

- **CCD definitions:** the CIVIL and GENERALAPPLICATION JSON from civil-ccd-definition, with callbacks to this service. It uses `CCD_DEFINITION_BRANCH` (default `master`), or the checkout named by `CIVIL_CCD_DEFINITIONS_DIR`. Like preview, it leaves out `*-prod.json` and the shuttered authorisations.
- **Business processes:** the BPMN files in `src/main/resources/camunda`, deployed to Camunda once it is up. The WA DMNs live in civil-wa-task-configuration and are not deployed, so `COURT_LOCATION_WA_DMN_ENABLED` is off.
- **Users:** the professional users civil's Playwright tests sign in as (`hmcts.civil+organisation.*`), the system update user, the PRD admin and the cross-access user. All have the password `password`. See `src/cftlib/java/.../CivilCftlibConfig.java`.
- **Stand-ins for services outside CCD:**
  - Wiremock serves [civil-wiremock-mappings](https://github.com/hmcts/civil-wiremock-mappings) (fees, Docmosis, PRD, location ref data, send-letter, SendGrid and more), plus the stubs in `src/cftlib/wiremock` for GOV.UK Notify and the payments service request.
  - CDAM stores documents in an in-memory dm-store that this service serves under cftlib (`LocalDmStoreController`).

Everything under `src/cftlib` runs only under cftlib.

## Running civil's Playwright tests against it

In civil-ccd-definition, create `.env.tests.local` (it is gitignored) pointing the tests at the local services. For example: `URL=http://localhost:3000`, `CIVIL_SERVICE_URL=http://localhost:4000`, `CCD_DATA_STORE_URL=http://localhost:4452`, `IDAM_API_URL=http://localhost:5062`, `SERVICE_AUTH_PROVIDER_API_BASE_URL=http://localhost:8489`, `DEFAULT_PASSWORD=password`, `ENVIRONMENT=preview`, and `PLAYWRIGHT_RUN_*_SETUP=false`. Then run the civil-service smoke tests:

```bash
yarn playwright test --project civil-service-master-pr-smoke --no-deps
```

## Decentralised CCD

`CIVIL_DECENTRALISED_ENABLED=true ./gradlew bootWithCCD` keeps CIVIL and GENERALAPPLICATION cases in this service's database, as preview does on the decentralised branch.

## Troubleshooting

- **`DB not yet available...` repeats forever:** the shared `cftlib` Postgres volume was created by a different major version. civil uses Postgres 15, as dtsse-ccd-config-generator's e2e does. `RSE_LIB_CLEAN_BOOT=1` recreates the containers and their data.
- **Startup hangs in a Flyway migration:** a `CREATE INDEX CONCURRENTLY` migration waits on a transactional Flyway lock. bootWithCCD sets `SPRING_FLYWAY_POSTGRESQL_TRANSACTIONAL_LOCK=false`, as preview does.
