# Renovate automerge investigation

## Current configuration

`civil-service` uses `.github/renovate.json`, extending the shared HMCTS preset and `local>hmcts/.github//renovate/automerge-all`.

Repo-level settings:

- `timezone`: `Europe/London`.
- `schedule` and `automergeSchedule`: after 7am and before 9am, and after 5pm and before 8pm, every weekday.
- `platformAutomerge`: `false`, so Renovate enforces the configured merge windows instead of queuing GitHub auto-merge.
- `rebaseWhen`: `behind-base-branch`, so Renovate refreshes branches that fall behind `master`.
- `automergeStrategy`: `squash`.
- `prConcurrentLimit`: `5`.
- Non-major, Helm, Spring-related, Terraform and Lombok updates set `automerge: true` and `automergeType: pr`.
- Camunda updates deliberately set `automerge: false`.
- Pact updates are disabled.

`CODEOWNERS` globally assigns `*` to `@hmcts/civil`, then has more specific ownerless entries intended to exempt some Renovate-updated files from code-owner review. The workflow exemption only covered `.github/workflows/*.yaml`; this repo also uses `.yml` workflow files, so GitHub Actions Renovate PRs still matched the global `* @hmcts/civil` owner rule.

An earlier check found the same `.yaml`-only workflow exemption in `cmc-claim-store` and `cmc-citizen-frontend`; those haven't been rechecked. `civil-citizen-ui`, `civil-ccd-definition` and `civil-wa-task-configuration` now have their own ownerless Renovate blocks covering both `.yaml` and `.yml` workflow files, and each documents its Renovate setup in its own `docs/renovate-automerge.md`. Their Jenkinsfiles are in that block too, so Jenkins library PRs don't need code-owner review (added by hmcts/civil-citizen-ui#8619 and hmcts/civil-ccd-definition#7112). Any CODEOWNERS exemption changes need to be raised and merged in each repository separately.

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

`.github/renovate.json` sets the `Europe/London` timezone. PTL Jenkins runs on `cft-ptl-00-aks`, which [`hmcts/auto-shutdown`](https://github.com/hmcts/auto-shutdown) normally stops from 20:00 to 06:30 daily. There is no scheduled weekend startup. An approved exclusion is required for overnight or weekend use; it can postpone shutdown until 23:00 or keep PTL running overnight.

If Renovate updates or rebases a PR while Jenkins is unavailable, the required check cannot complete and automerge remains blocked. Each Renovate merge also advances `master`, which can make developer and other Renovate branches stale and trigger rebases or repeated CI. Branch creation and automerge therefore share the same windows, when Jenkins is online and developer activity is lower:

```json
"schedule": [
  "after 7am and before 9am every weekday",
  "after 5pm and before 8pm every weekday"
],
"automergeSchedule": [
  "after 7am and before 9am every weekday",
  "after 5pm and before 8pm every weekday"
],
"platformAutomerge": false
```

The 07:00-09:00 and 17:00-20:00 weekday windows fall inside PTL's normal hours and avoid the busiest part of the developer day. A PR created or rebased late in the evening window may not finish its Jenkins build before the 20:00 shutdown; it then merges in a later window. `platformAutomerge` must be `false`; otherwise Renovate enables GitHub auto-merge and GitHub can merge later whenever checks pass, regardless of `automergeSchedule`. Automerge-enabled package rules inherit the repository-level schedule so they cannot widen it. PRs that miss a window wait for a later Renovate run, and the stalled-queue alert remains the control for updates that do not merge.

## Holds for updates master cannot build

`rebaseWhen: behind-base-branch` rebuilds every open Renovate PR on each merge to `master`. That is what keeps green PRs mergeable, but a PR that can never pass is rebuilt, and fails, every time too. In the 30 days to 2026-11-20, five such PRs took 313 Jenkins builds, about 23 agent-hours, without one green build.

Updates that master can't build, or that break the tests, are therefore held in `.github/renovate.json`, each with the reason in its `description`:

| Package | Hold | Reason |
|---|---|---|
| `com.github.hmcts:*` | skips versions with a ticket suffix, e.g. `4.1.0-CCD-8220`, `9.2.12_CCD-8388` | Feature-branch builds. Maven ranks the suffix above the release, so Renovate would otherwise offer them as the newest version. This rule comes first so the package-specific holds below override it |
| `net.bytebuddy:byte-buddy`, `byte-buddy-agent` | skips 1.18.17 | Breaks Mockito mocking of `CaseData` on Java 25 ("Could not modify all classes") |
| `python` (`actions/setup-python`) | below 3.15 | semgrep's `ruamel.yaml.clib` has no 3.15 build (pii-log-check), and the email-docs generator tests fail on 3.15 |
| Pact packages | disabled | |
| CCD Helm chart | disabled | DTSCCI-7065, chart size limit |
| Spring, springdoc, Spring Cloud, Jakarta Persistence and Hibernate majors | disabled | Set before the Spring Boot 4 migration |

#8709, still open, adds two more: Byte Buddy 1.18.16 is skipped as well, and `io.projectreactor:reactor-bom` and `io.micrometer:micrometer-bom` take patch updates only, because they only set CVE minimums. Update this table when it merges.

These older holds are out of date. The service is now on Spring Boot 4.1 and Java 25, so their reasons no longer apply:

| Package | Hold | In use | Effect now |
|---|---|---|---|
| `com.github.hmcts:core-case-data-store-client` | below 5.4.0 | 5.3.0 | Blocks 5.4.x, which was held for Spring Boot 4. Version 6 moves to Jackson 3, which the CCD SDK's Jackson 2 guard rejects |
| `org.springframework.cloud:spring-cloud-dependencies` | below 2025.1.0 | 2025.1.3 | Blocks every update, including a release train that supports Spring Boot 4.2 |
| `com.github.hmcts:send-letter-client` | below 6.0.0 | 5.1.1 | Blocks 6.x, which was held only for Java 25 |
| `com.github.hmcts:auth-checker-lib` | below 3.2.0 | 3.2.2 | Blocks every update |
| `com.github.hmcts:ccd-case-document-am-client` | below 1.60.0 | 2.0.0 | Blocks every update |

A rule that sets only `matchUpdateTypes` for `auth-checker-lib`, `payments-java-client`, `ccd-case-document-am-client` and `core-case-data-store-client` has no effect: it doesn't set `enabled`, `allowedVersions` or a group.

Review each of these holds and remove or update them. When a new Renovate PR fails the build on every rebase, find the cause and add a hold here rather than leaving it open.

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
