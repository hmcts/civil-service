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

## Merge window and Jenkins availability

Renovate schedules use the `Europe/London` timezone inherited from the shared HMCTS preset. The repository currently permits Renovate activity after 08:00 and before 11:00 on weekdays. Most automerge-enabled package rules override only the merge window with `before 4pm every weekday`.

The PTL Jenkins configuration is maintained in [`cnp-flux-config`](https://github.com/hmcts/cnp-flux-config/blob/master/apps/jenkins/jenkins/ptl-intsvc/jenkins.yaml). The controller and surrounding Flux manifests do not define an overnight shutdown schedule. The configured Kubernetes agents are created as required and discarded after 10 idle minutes. Azure VM agents have a five-minute idle retention strategy, while their shared template sets `shutdownOnIdle: false`. These agent lifecycle settings are not a scheduled shutdown of the Jenkins controller.

Therefore, the checked-in configuration does not show Jenkins being unavailable outside office hours. However, automerge still depends on all required checks being present and successful for the current PR head:

- A PR whose current head already has successful required checks can merge during its allowed automerge window.
- If Renovate rebases or updates the branch, the new head must complete Jenkins and the other required checks before it can merge.
- Any separate platform maintenance, cluster shutdown or Jenkins outage can leave the required Jenkins status pending or failed and will block automerge safely.

To keep CI and merges inside a clear supported-hours window, prefer an explicit window on every automerge-enabled package rule:

```json
"automergeSchedule": ["after 8am and before 4pm every weekday"]
```

Using only `before 4pm every weekday` also includes the period after midnight. It should not be described as an out-of-hours exclusion.

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

For an emergency stop, the repository can instead be paused in the Mend Developer Portal. Pausing is broader than a release freeze because it also stops update discovery, rebases and PR maintenance. Changing branch protection is not recommended as a routine toggle because it affects all contributors and makes intentional freezes look like authorization failures.
