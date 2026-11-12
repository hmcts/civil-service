# App infrastructure

Add any application specific infrastructure to the terraform files in this folder

This could be things like:
* a database
* redis
* vault
* application insights

If you don't need any application infrastructure here, you can delete the whole folder (it will speed up your Jenkins build)

## Support report infrastructure

`support_report_workflows` is empty by default. Merging this code creates no
reporting resources or schedules. An explicitly configured entry provisions a
Consumption workflow, its managed-identity Monitor Logs connection, resource-scoped
telemetry read, container-scoped artifact/state access, failure alerts and a separate
hourly deadline checker. The checker uses management identity and the existing
operations action group, independently of the report sender. Its own failure or
absence also alerts. Report actions must fail the workflow for incomplete sources
or unsuccessful publication; a successful workflow run is the deadline signal.

Each `concurrency` or `stuck-cases` entry supplies:

| Setting | Meaning |
| --- | --- |
| `enabled` | Deploy disabled first; enable only after validation |
| `frequency`, `interval` | `Day`, `Week` or `Month` and a positive whole interval |
| `start_time`, `time_zone` | Future local wall time `YYYY-MM-DDTHH:MM:SS`, without `Z`, and a Logic Apps timezone such as `GMT Standard Time` |
| `definition_json` | Reviewed workflow JSON with nonempty actions; infrastructure supplies the recurrence and `$connections` parameter |
| `log_resource_id` | Approved App Insights component resource, never a subscription-wide grant |
| `artifact_container_id`, `ledger_container_id` | Existing approved, private blob containers, using full ARM IDs |
| `action_group_id` | Existing independent operations notification route |
| `deadline_hours` | Maximum successful-run age, 1 to 720 hours; include schedule and delivery allowance |

The `azuremonitorlogs` connection is available through the workflow's
`$connections` parameter. Use secure inputs/outputs for query, storage, credential
and publication actions, including actions nested in scopes. Never place secrets
or personal report data into Terraform configuration, outputs or inline defaults.
The workflow run history is retained for 40 days so monthly deadline checks can
observe a previous run. The deadline query uses successful runs started within the
configured interval; report actions must complete within that allowance. Initial
enablement without a successful baseline intentionally alerts until verified.
This is a liveness check, not proof that every required report period was covered:
a successful backfill of an old period can refresh it. Each report implementation
must also reconcile its period/delivery ledger against the periods actually due.

This module is the Consumption deployment path. Confirm the source query endpoint
and approved output storage are reachable by this path before choosing it. It does
not create private endpoints, bypass storage firewalls or claim access to private
storage merely because RBAC exists. Where networking requires Standard, agree that
deployment path with Platform Operations before enabling. Existing containers,
their private access/retention, source access approval, sender and action group
must be supplied by their owners. No access keys, personal logins or new storage
accounts are provisioned here.

The report tickets supply and validate the collector, secure rendering, sender,
immutable artifacts, distributed period lease and per-destination delivery state.
Trigger concurrency bounds one workflow only; it is not a distributed lease or an
exactly-once delivery guarantee. Disabled workflows and their alerts can be
provisioned independently while report definitions are completed.

Use the pinned infrastructure Terraform version. Run `terraform init -backend=false`
and `terraform validate` in `infrastructure`; run `terraform init -backend=false`
and `terraform test` in `infrastructure/modules/support-report` for provider-mocked
safety checks. Deployment uses the existing Civil service infrastructure pipeline.
Review its actual state-backed plan before approval. Local tests do not prove a
successful live deployment or source/destination access.
