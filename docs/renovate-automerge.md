# Renovate automerge investigation

## Current configuration

`civil-service` uses `.github/renovate.json`, extending the shared HMCTS preset and `local>hmcts/.github//renovate/automerge-all`.

Repo-level settings:

- `enabled`: `false`. Renovate is paused for this repository (#8711) while the open dependency PRs are sorted out; see [Pausing Renovate](#pausing-renovate). The rest of this section describes the configuration that applies once it is turned back on.
- `schedule`: after 8am and before 11am every weekday.
- `automergeSchedule`: after 7am and before 8am, and after 6pm and before 8pm, every weekday.
- `platformAutomerge`: `false`, so Renovate enforces the configured merge windows instead of queuing GitHub auto-merge.
- `rebaseWhen`: `behind-base-branch`, so Renovate refreshes branches that fall behind `master`.
- `automergeStrategy`: `squash`.
- `prConcurrentLimit`: `5`.
- Non-major, Helm, Spring-related, Terraform and Lombok updates set `automerge: true` and `automergeType: pr`.
- Camunda updates deliberately set `automerge: false`.
- Pact updates are disabled.

`CODEOWNERS` globally assigns `*` to `@hmcts/civil`, then has more specific ownerless entries intended to exempt some Renovate-updated files from code-owner review. The workflow exemption only covered `.github/workflows/*.yaml`; this repo also uses `.yml` workflow files, so GitHub Actions Renovate PRs still matched the global `* @hmcts/civil` owner rule.

Sibling repo checks under the local HMCTS checkout showed the same `.yaml`-only workflow exemption in `cmc-claim-store`, `cmc-citizen-frontend`, and `civil-ccd-definition`. `civil-citizen-ui` and `civil-wa-task-configuration` have the same global `* @hmcts/civil` owner rule but no Renovate ownerless exemption block. Those repositories have their own Renovate PRs, so any CODEOWNERS exemption changes need to be raised and merged in each affected repository separately.

## Current queue check

Checked on 2026-09-01 using GitHub CLI against `hmcts/civil-service`.

Open Renovate PRs:

- `#8293` Update actions/setup-java action to v6: required checks were green, but GitHub still required code-owner approval from `@hmcts/civil` because the changed `.yml` workflow files were not covered by the existing ownerless `.github/workflows/*.yaml` CODEOWNERS exemption. The branch was also behind.
- `#8278` Update dependency org.apache.tika:tika-core to v4: approved but branch behind; Jenkins status errored and Pact Consumer Verification failed.
- `#8263` Update Spring Boot and related updates (major): approved but branch behind; GitHub build failed and Jenkins errored.
- `#8253` Update Spring Boot and related updates: approved but branch behind; GitHub build failed, Jenkins errored, and Pact Consumer Verification failed.
- `#8252` Update All non-major updates: review required; GitHub `pii-log-check` and `build` failed, Jenkins errored.

Renovate dependency dashboard:

- `#2251` Dependency Dashboard is open and was updated on 2026-09-01.

## Findings

Automerge is configured for the intended Renovate groups, but the current queue is not mergeable. The key blocker for otherwise green PRs is repository governance: code-owner review is required, and the existing CODEOWNERS exemptions do not cover all Renovate-updated file patterns. For `#8293`, `.github/workflows/*.yml` fell through to the global `* @hmcts/civil` owner rule, so approvals from Renovate approval bots did not satisfy GitHub's required code-owner review. Some PRs also have failed required checks or branches behind `master`, but those are secondary blockers rather than the root cause for green PRs remaining open.

The GitHub branch protection endpoint returned `404` for `master` with the available token, and repository rulesets returned no visible rules. The PR merge state and GitHub UI still show the practical blockers on the active Renovate PRs, including code-owner approval requirements on otherwise passing Renovate PRs.

## Fix

Added `.github/workflows/*.yml` to the ownerless Renovate exemption block in `.github/CODEOWNERS`, matching the existing `.github/workflows/*.yaml` exemption. This should prevent GitHub Actions workflow update PRs from requiring `@hmcts/civil` code-owner approval solely because of the global owner rule.

Updated Renovate `rebaseWhen` from `conflicted` to `behind-base-branch`. This keeps automerge candidates up to date with `master`, avoiding the follow-on blocker where green and approved Renovate PRs remain open because the branch is behind the base branch.

This fix is local to `civil-service`. It does not change CODEOWNERS behaviour in `civil-citizen-ui`, `cmc-claim-store`, or `cmc-citizen-frontend`; separate PRs are needed there if their Renovate PRs are blocked by the same ownership gap.

This does not bypass required status checks, stale branch requirements, or code-owner review for files that remain owned. Dependency PRs touching files such as `build.gradle`, `Dockerfile`, Helm chart metadata and Gradle wrapper properties already had ownerless Renovate exemptions in `civil-service`.

## Alerting

`.github/workflows/renovate-stalled-alert.yml` runs at 08:30 UTC Monday to Friday and can also be run manually. It queries open Renovate PRs across:

- `hmcts/civil-service`
- `hmcts/civil-citizen-ui`
- `hmcts/cmc-claim-store`
- `hmcts/cmc-citizen-frontend`
- `hmcts/civil-wa-task-configuration`
- `hmcts/civil-ccd-definition`

It counts PRs older than the configured threshold, writes the stale queue into the workflow summary, optionally posts to Slack, and fails the workflow when the threshold is exceeded.

Defaults:

- `max_age_days`: `3`
- `threshold`: `1`

To enable Slack delivery, add repository secret `RENOVATE_ALERT_SLACK_WEBHOOK_URL` with the target team-channel incoming webhook URL. Without the secret, the workflow still fails visibly and records the stalled PR list in the Actions run summary.

## Merge window and Jenkins availability

Renovate uses the `Europe/London` timezone inherited from the shared HMCTS preset. PTL Jenkins runs on `cft-ptl-00-aks`, which [`hmcts/auto-shutdown`](https://github.com/hmcts/auto-shutdown) normally stops from 20:00 to 06:30 daily. There is no scheduled weekend startup. An approved exclusion is required for overnight or weekend use; it can postpone shutdown until 23:00 or keep PTL running overnight.

If Renovate updates or rebases a PR while Jenkins is unavailable, the required check cannot complete and automerge remains blocked. Each Renovate merge also advances `master`, which can make developer and other Renovate branches stale and trigger rebases or repeated CI. Branch creation remains between 08:00 and 11:00, while automerge is restricted to periods when Jenkins is online and developer activity is lower:

```json
"automergeSchedule": [
  "after 7am and before 8am every weekday",
  "after 6pm and before 8pm every weekday"
],
"platformAutomerge": false
```

The 07:00-08:00 and 18:00-20:00 weekday windows avoid the normal PTL outage and reduce disruption to developer PRs. `platformAutomerge` must be `false`; otherwise Renovate enables GitHub auto-merge and GitHub can merge later whenever checks pass, regardless of `automergeSchedule`. Automerge-enabled package rules inherit the repository-level schedule so they cannot widen it. PRs that miss a window wait for a later Renovate run, and the stalled-queue alert remains the control for updates that do not merge.

## Holds for updates master cannot build

`rebaseWhen: behind-base-branch` rebuilds every open Renovate PR on each merge to `master`. That is what keeps green PRs mergeable, but a PR that can never pass is rebuilt, and fails, every time too. In the 30 days to 2026-11-20, five such PRs took 313 Jenkins builds, about 23 agent-hours, without one green build.

Updates that need a platform the service hasn't moved to yet are therefore held in `.github/renovate.json`, each with the reason in its `description`:

| Package | Held below | Needs |
|---|---|---|
| `com.github.hmcts:core-case-data-store-client` | 5.4.0 | Spring Boot 4 (`spring-boot-http-converter`) |
| `org.springframework.cloud:spring-cloud-dependencies` | 2025.1.0 | Spring Boot 4 (OpenFeign loads Boot 4 converter classes) |
| `com.github.hmcts:send-letter-client` | 6.0.0 | Java 25 |
| Spring, springdoc, Spring Cloud, Jakarta Persistence and Hibernate majors | disabled | Spring Boot 4 |

Remove these holds as part of the Spring Boot 4 and Java 25 migrations. When a new Renovate PR fails the build on every rebase, find the cause and add a hold here rather than leaving it open.

## Release freeze

Use a code-controlled Renovate package rule for a planned release freeze. This is reviewable, auditable and consistent across repository administrators. LaunchDarkly is not suitable because Renovate evaluates repository configuration in Mend and does not call the application or its runtime feature flags.

Add the following rule as the final item in `.github/renovate.json` under `packageRules`:

```json
{
  "description": "Release freeze: disable Renovate automerge",
  "matchPackageNames": ["*"],
  "automerge": false
}
```

The rule must remain last because Renovate combines all matching package rules and later values take precedence. It disables automerge without disabling dependency discovery or stopping Renovate from maintaining PRs.

### Start a freeze

1. Add the final freeze rule in a named pull request, including the release or change reference and intended end date in the PR description.
2. Merge the rule before the freeze starts and manually run Renovate, or wait for its next scheduled run, so the configuration is applied.
3. Check open Renovate PRs and disable any GitHub auto-merge requests that were enabled before the freeze. The new rule prevents Renovate from enabling automerge; it should not be relied on to cancel an auto-merge request already stored by GitHub.
4. Confirm the Mend job log resolves the freeze rule with `automerge: false` and record the verification in the release ticket.

### End a freeze

1. Remove the freeze rule through a pull request after release approval is given.
2. Merge the change and manually run Renovate, or wait for its next scheduled run.
3. Verify a clean, in-scope Renovate PR is offered for automerge and merges only after all required checks pass.
4. Continue monitoring `.github/workflows/renovate-stalled-alert.yml` for queues blocked by failed checks, required reviews or stale branches.

For a full stop, pause Renovate instead (see below). Changing branch protection is not recommended as a routine toggle because it affects all contributors and makes intentional freezes look like authorization failures.

## Pausing Renovate

Pausing is broader than a release freeze. A freeze only stops automerge. A pause also stops update discovery, new PRs, rebases and PR maintenance. Use it when open Renovate PRs need fixing by hand and rebuilding them on every merge to `master` only adds noise, or when no Renovate PR should change at all.

| | Release freeze (`automerge: false` rule) | Pause (`enabled: false`) |
|---|---|---|
| New PRs and rebases | continue | stop |
| Automerge | stops | stops |
| Renovate's PR queue | stays current; merges are manual | builds up until turned back on |

To pause, set `"enabled": false` at the top level of `.github/renovate.json` in a pull request, giving the reason and the expected end. Renovate reads its configuration from `master`, so the pause starts only once the change merges and Renovate next runs. Open Renovate PRs stay open but are no longer rebased, updated or automerged.

To turn Renovate back on, revert that pull request. On its next run Renovate rebases the open PRs against `master`, applying any rules merged in the meantime, and catches up on the updates it skipped.

If Renovate must stop before a configuration change can merge, an organisation admin can pause the repository in the Mend Developer Portal. A portal pause is turned back on in the portal, not by reverting configuration.

### Current pause

Paused by #8711 (2026-11-26) while these are fixed:

- `#8706` All non-major updates: Byte Buddy 1.18.16 breaks Mockito mocking of `CaseData` on Java 25 (held by #8709), and the Micrometer BOM moves to 1.18.0, past the 1.17.x that Spring Boot 4.1 manages.
- `#8571` Spring Boot and related updates: Spring Boot 4.2.0 isn't accepted by Spring Cloud 2025.1.3, and the Reactor, Micrometer and Netty CVE-minimum pins in `build.gradle` sit below the versions Boot 4.2 picks.

Revert #8711 once #8709 has merged and the Spring Boot 4.2 route is agreed.
