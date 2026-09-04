# Renovate automerge investigation

## Current configuration

`civil-service` uses `.github/renovate.json`, extending the shared HMCTS preset and `local>hmcts/.github//renovate/automerge-all`.

Repo-level settings:

- `schedule`: after 8am and before 11am every weekday.
- `automergeSchedule`: after 8am and before 11am every weekday, with most repo package rules overriding to before 4pm every weekday.
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
