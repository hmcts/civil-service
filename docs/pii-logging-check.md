# PII logging PR check

DTSCCI-5897 adds a diff-scoped Semgrep check to prevent the PII logging removed
under DTSCCI-5875 from being reintroduced. The check is initially advisory:
findings are attached to the GitHub Actions run as file and line warnings, but do
not fail the build. Invalid rules or scanner failures do fail the build.

## Policy

Do not log:

- names, postal addresses, email addresses, dates of birth, or telephone numbers;
- claim, fee, payment, interest, repayment, or other financial values;
- complete case, claim, application, party, applicant, respondent, payment, or
  financial-detail objects.

CCD case references, field/rule identifiers, template identifiers, and agreed
operational user identifiers may be logged. Prefer stable identifiers and state
transitions over payloads. DTSCCI-5875 separately adds runtime redaction as a
defence-in-depth layer; this PR-time check does not depend on that work.

## Run locally

Install the pinned CI version of Semgrep and run the rule tests:

```shell
python3 -m pip install semgrep==1.136.0
semgrep test .semgrep
```

Scan changes relative to the target branch:

```shell
semgrep scan --strict --metrics=off --no-rewrite-rule-ids \
  --config .semgrep/logging-pii.yml \
  --baseline-commit origin/master \
  src/main
```

The CI checkout uses full Git history and the pull request base SHA, so findings
that already exist on the target branch are not reported on unrelated pull
requests. When DTSCCI-5875 is merged, its cleaned state naturally becomes the
baseline for subsequent pull requests.

## Initial tuning result

On 17 July 2026, a full post-DTSCCI-5875 scan reported four existing Java
findings. Manual review classified all four as policy-relevant: a case name, a
party name, a fee amount, and a payment customer reference. Earlier broad object
rules were narrowed after they produced false positives for payment states and
respondent role labels. The advisory PR phase remains responsible for measuring
the false-positive rate across normal pull requests before the check is made
blocking.

## Justified suppression

First rewrite the log to use an approved identifier. If a finding is demonstrably
safe and cannot be expressed more clearly, add a ticketed reason immediately
above the suppression and limit it to the specific rule:

```java
// PII-LOGGING-SUPPRESSION: DTSCCI-1234 - value is a fixed rule identifier.
// nosemgrep: civil.java.sensitive-object-to-log
log.info("Rule selected: {}", application);
```

Suppressions without a ticket and explanation should not be approved. Do not use
blanket `nosemgrep` comments or add source directories to an ignore file.

After an advisory sample of recent pull requests has an acceptable false-positive
rate, remove advisory mode by running the scan with `--error` and make the
`pii-log-check` job a required branch check.
