# Cutover verification harness

Checks a Civil cutover from data store to civil-service's decentralised runtime, end to end, for one environment:
preview, AAT or perftest. It seeds cases before the migration's `CUTOVER` run, then after the cutover it checks
each case reads, searches, keeps its work allocation tasks and takes new events as it did before. Each
environment's cutover runs it as its sign-off check.

It is modelled on ET's harness (hmcts/et-ccd-callbacks#3214). It differs in three ways:

- **No hand-kept expectations.** The seed step records how each case looks to each user. The verify steps compare
  against that record.
- **It writes after cutover.** It runs a general application, a Notice of Change and a new claim through the
  decentralised runtime.
- **No new dependencies.** It uses Node's `fetch` and `psql`.

It never runs against prod: `CUTOVER_ENV` accepts only `preview`, `aat` and `perftest`.

## Order

| Step | When | Command | Fails when |
| --- | --- | --- | --- |
| 1. Seed | Before `CUTOVER`, centralised | `npm run cutover:seed` | A journey or its Camunda process fails |
| 2. Migrate | `CIVIL_CCD_DATA_MIGRATION_MODE=CUTOVER` until progress is `COMPLETE` | (Flux) | |
| 3. DB check | After `CUTOVER`, before routing | `npm run cutover:check-db` | Civil's copy of a seeded case differs from data store's |
| 4. Route | data store's `ccd.decentralised.case-type-service-urls` names CIVIL and GENERALAPPLICATION; `CIVIL_DECENTRALISED_ENABLED=true` | (Flux) | |
| 5. Cases | After routing | `npm run cutover:verify-cases` | Any user sees a seeded case differently from step 1 |
| 6. Search | After routing | `npm run cutover:verify-search` | A case isn't found, or its index entry is stale |
| 7. Tasks | After routing | `npm run cutover:verify-tasks` | A task the case had before cutover has gone |
| 8. Writes | Last: it changes the cases | `npm run cutover:verify-writes` | A post-cutover event, NoC or new claim fails |
| 9. Search again | After writes | `npm run cutover:verify-search` | The decentralised indexer hasn't picked up the writes |

Don't reseed after step 2: step 1's manifest is the baseline. Each step writes `build/cutover/<step>-report.json`
and exits non-zero when anything failed. To rerun one case, set `CUTOVER_PROFILE_IDS`.

## What each step checks

**Seed** creates these cases through the same API calls as civil-ccd-definition's API tests (`profiles.mjs`):

| Profile | Case | State |
| --- | --- | --- |
| `claim-pending-issue` | LR v LR specified small claim, fee unpaid | `PENDING_CASE_ISSUED` |
| `claim-issued` | The same claim, paid and issued, defendant LR assigned | `AWAITING_RESPONDENT_ACKNOWLEDGEMENT` |
| `ga-awaiting-judge` | Paid application without notice on `claim-issued` | `APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION` |

For each case, and for each of the claimant's LR, the defendant's LR and a caseworker admin, it records the
following, and the WA tasks the tasks user can see:
- whether data store lets them read the case;
- its state and security classification;
- a hash of each field;
- its event history;
- the events they're offered, and data store's answer when they start each one (this runs its about-to-start
  callback but saves nothing).

**DB check** compares the case's row and events in data store's database with civil-service's `ccd` schema. It
compares every column the SDK copies as it is:
- state, version, dates and security classification;
- the `data` and `supplementary_data` hashes;
- the event count, a hash of every event, and the latest event.

It also checks the columns the SDK renumbers:
- event revisions are 1..N in event order;
- `case_data.case_revision` is the highest event revision plus 1,000,000,000.

**Cases** takes the same record again through data store, which now calls civil-service, and requires it to match:
the same access, state, fields, history, events offered and start results. `SearchCriteria` is ignored, because
the runtime rebuilds it on every read.

**Search** finds each case by reference through data store's `searchCases`. It requires the indexed copy's state
and `last_modified` to match the case, waiting for the index to catch up. `CUTOVER_SEARCH_GLOBAL=true` also checks
global search.

**Tasks** requires every task recorded at seed to still exist, with the same id and type. A case with no tasks at
seed is skipped, so read the report: a skip means WA wasn't creating that task before cutover either.

**Writes:**
- On the migrated `claim-issued`, the claimant's LR applies for a GA. This tests a CIVIL event, Camunda, a new
  GENERALAPPLICATION case and payment.
- Another organisation then takes over the defendant through Notice of Change.
- Finally it creates and issues a new claim.

The report names the new cases.

## Configuration

```bash
export CUTOVER_ENV=aat            # preview | aat | perftest
export CUTOVER_PR_ID=1234         # preview only: the civil-service PR

# Secrets. Jenkins reads the same ones (see Jenkinsfile_CNP)
export DEFAULT_PASSWORD=...                     # civil-<env> vault: default-password
export CCD_API_GATEWAY_IDAM_CLIENT_SECRET=...   # ccd-<env> vault: ccd-api-gateway-oauth2-client-secret
export S2S_SECRET=...                           # civil-<env> vault: microservicekey-civil-service
export XUI_S2S_SECRET=...                       # s2s-<env> vault: microservicekey-xui-webapp (tasks only)
```

AAT and perftest use internal URLs, so they need the VPN. Preview uses the PR's preview hosts. Override any URL
with one of these:
- `CUTOVER_CCD_DATA_STORE_URL`
- `CUTOVER_CIVIL_SERVICE_URL`
- `CUTOVER_AAC_URL`
- `CUTOVER_WA_TASK_MGMT_URL`
- `CUTOVER_IDAM_API_URL`
- `CUTOVER_S2S_URL`

Users default to civil-ccd-definition's API test users. Override them with `CUTOVER_<ROLE>_EMAIL`,
`CUTOVER_<ROLE>_PASSWORD` and `CUTOVER_<ROLE>_ORG_ID`, where `<ROLE>` is one of:
- `APPLICANT_SOLICITOR`
- `DEFENDANT_SOLICITOR`
- `NOC_SOLICITOR`
- `ADMIN`
- `TASKS_USER`

Polling uses `CUTOVER_POLL_ATTEMPTS` (default 40) and `CUTOVER_POLL_INTERVAL_MS` (default 15000).

Other switches:
- `CUTOVER_SKIP_EVENT_STARTS=true`: verify-cases skips starting each event.
- `CUTOVER_ALLOW_NEW_EVENTS=true`: lets verify-cases run after verify-writes.
- `CUTOVER_SKIP_NEW_CLAIM=true`: verify-writes skips the new claim.

### Databases (DB check only)

```bash
export CUTOVER_CCD_DB_URL='postgresql://<user>@<host>:5432/<data store db>?sslmode=require'
export CUTOVER_CCD_DB_PASSWORD=...
export CUTOVER_CIVIL_DB_URL='postgresql://<user>@<host>:5432/cmc?sslmode=require'
export CUTOVER_CIVIL_DB_PASSWORD=...
```

The default schemas are `public` for data store and `ccd` for civil-service. Override them with
`CUTOVER_CCD_DB_SCHEMA` and `CUTOVER_CIVIL_DB_SCHEMA`. The check only reads.
- **AAT and perftest:** the credentials are the migration's own: data store's in the `ccd-<env>` vault, civil's
  in `cmc-<env>`.
- **Preview:** both databases are on the PR's in-cluster Postgres (`data-store` and `cmc`, user `hmcts`). Reach
  them with `kubectl port-forward svc/civil-service-pr-<id>-postgresql 5432`.

## Tests

`npm run test:cutover` runs the unit tests for the comparison logic, TOTP and configuration.
