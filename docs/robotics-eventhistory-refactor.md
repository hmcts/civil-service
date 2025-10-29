# Robotics EventHistory Refactor Log

Central place to track the EventHistory refactor: baselines, extracted utilities, strategy rollout, and regression evidence.

---

## Problem Statement

- `EventHistoryMapper` currently spans more than 2.7k lines; every robotics branch is hard-coded inside one `buildEvents` method.
- Case classification, date/party formatting, and event construction are interwoven, driving cyclomatic complexity into the thousands and making new event additions risky.
- Supporting “robotics builder” helpers duplicate logic (IDs, strings, sequencing) and mirror the monolith’s structure.

## Scope (must-have outcomes)

1. Extract per-feature strategies (breathing space, DJ, mediation, paper/offline, etc.) so `EventHistoryMapper` acts as an orchestrator.
2. Centralise shared formatting and ID helpers for reuse across all robotics mappers.
3. Align `RoboticsDataMapper` and sibling classes with the new strategy and utility layers to remove stream/loop duplication.
4. Back the new strategies with focussed unit tests that lock in current event payloads for representative spec/unspec and multi-party scenarios.

## Acceptance Criteria

- `EventHistoryMapper`’s primary methods shrink below 300 lines and delegate to strategy classes.
- New shared utilities replace existing duplication, and all pre-refactor regression tests stay green.
- Robotics regression plus the new unit suites confirm zero change to emitted event JSON for existing flows.

---

## Phase Roadmap

| Phase | Purpose | Status | Notes |
| --- | --- | --- | --- |
| 0 | Snapshot existing `EventHistory` outputs for key scenarios | Complete | Golden JSON captured; harness hardened for deterministic comparisons |
| 1 | Extract shared formatting/util utilities | Complete | Utilities adopted across mapper, data mappers, and handlers |
| 2 | Introduce strategy infrastructure | In progress | Contributors extracted: breathing space, default judgment |
| 3+ | Incrementally migrate feature-specific logic (breathing space, DJ, mediation, GA, etc.) | Pending | Update this table per phase |

---

## Phase 0 – Baseline Snapshots

**Goal**  
Lock current `EventHistory` payloads so future refactors prove behaviour parity.

**Fixture Matrix**

*Captured*  
- `unspec_claim_progression` – `CaseDataBuilder.builder().atStateClaimDetailsNotified()`
- `unspec_taken_offline` – `CaseDataBuilder.builder().atStateTakenOfflineByStaff()`
- `unspec_ga_strike_out` – `...atStateTakenOfflineByStaff().getGeneralApplicationWithStrikeOut("001").getGeneralStrikeOutApplicationsDetailsWithCaseState(PROCEEDS_IN_HERITAGE.getDisplayedValue())`
- `unspec_multi_party_same_solicitor` – `...atStateNotificationAcknowledged1v2SameSolicitor().multiPartyClaimOneDefendantSolicitor()`
- `multi_applicant_proceed` – `...multiPartyClaimTwoApplicants().atStateApplicant2RespondToDefenceAndProceed_2v1()`
- `spec_full_defence` – `...setClaimTypeToSpecClaim().atStateSpec1v1ClaimSubmitted().atStateRespondent1v1FullDefenceSpec()`
- `breathing_space_standard` – `CaseDataBuilder.builder().addLiftBreathingSpace()`
- `breathing_space_mental_health` – `CaseDataBuilder.builder().addLiftMentalBreathingSpace()`
- `spec_mediation_part_admit` – `CaseDataBuilder.builder().setClaimTypeToSpecClaim().atStateSpec1v1ClaimSubmitted().atStateRespondent1v1FullAdmissionSpec()` with mediation opt-in, DQ court data, and applicant response date pushed beyond `now()`.
- `default_judgment_unspec` – `CaseDataBuilder.builder().getDefaultJudgment1v1Case()` with JO created date, claimant LiP decision, payment date, and totals overridden for determinism.

*Queued for capture*  
- _None_

**Snapshot Plan**

1. For each fixture, call `mapper.buildEvents(caseData, BEARER_TOKEN)` in a dedicated test.
2. Serialise results with the robotics Jackson configuration.
3. Store outputs under `src/test/resources/robotics/golden/<scenario>.json`.
4. Assert equality in regression tests that regenerate the payload.

**Progress Log**

- 2025-10-28 (pass 1): Captured golden snapshots for `unspec_claim_progression`, `unspec_taken_offline`, and `unspec_ga_strike_out`; normalised dynamic timestamps via `CaseDataNormalizer`; introduced `EventHistoryGoldenSnapshotTest`.
- 2025-10-28 (pass 2): Added snapshots for `unspec_multi_party_same_solicitor`, `multi_applicant_proceed`, and `spec_full_defence`.
- 2025-10-28 (pass 3): Added breathing space variants (`breathing_space_standard`, `breathing_space_mental_health`); `default_judgment_unspec` remains outstanding due to dynamic payment dates.
- 2025-10-28 (pass 4): Captured `spec_mediation_part_admit` (future-dated applicant response guard) and `default_judgment_unspec` (deterministic JO timestamp, payment date, and LiP decision); snapshot assertions now compare serialised JSON to avoid BigDecimal formatting drift.
- 2025-10-29 (noise sweep): Confirmed Phase 1 helpers are not present in the codebase after the rollback; regression harness continues to pass on the baseline branch.

---

## Phase 1 – Shared Utility Extraction (Plan)

**Research Findings (2025-10-29)**

- `EventHistoryMapper` still carries ~2.7k lines with direct `LocalDateTime.now()` calls at multiple hotspots (e.g. mediation fallback: `EventHistoryMapper.java:2698`, Ga/NoC flows: `EventHistoryMapper.java:369`, `EventHistoryMapper.java:2709`), making determinism dependent on runtime calls.
- “RPA Reason” text templates are duplicated across numerous branches (`EventHistoryMapper.java:235`, `EventHistoryMapper.java:2449`, `EventHistoryMapper.java:2712`), often with subtle punctuation differences that should be centralised.
- Litigious party ID selection and solicitor reference truncation logic is duplicated between `RoboticsDataMapper` and `RoboticsDataMapperForSpec`, and partially reimplemented inside `EventHistoryMapper` (applicant/defendant iteration blocks).
- Sequencing of events relies on `prepareEventSequence(...)` scattered inside the mapper, while `EventHistorySequencer` exposes similar responsibilities for external callers; consolidating sequencing logic will reduce divergence.

**Utility Boundaries (target state)**

| Utility | Responsibility | Primary Call Sites |
| --- | --- | --- |
| `RoboticsTimelineHelper` (name TBD) | Provide deterministic date/time values (wrapping `Time.now()`) and ISO formatting helpers for directions questionnaire, mediation, default judgment, and breathing-space branches. | `EventHistoryMapper` (lines 2690+, 333+, 2480+), `RoboticsDataMapper`, `RoboticsDataMapperForSpec`. |
| `RoboticsEventTextFormatter` | Hold template methods for recurring “RPA Reason …” text, intention summaries, and GA/DJ reason strings; expose parameterised builders to avoid inline `String.format` duplication. | `EventHistoryMapper` offline/miscellaneous builders, GA strike-out handlers, mediation events. |
| `RoboticsPartyLookup` | Centralise applicant/respondent litigious party ID resolution, solicitor reference truncation (24 char rule), and multi-party proceed decisions. | Both data mappers, `EventHistoryMapper` applicant/defendant loops, solicitor builders. |
| `RoboticsSequenceGenerator` | Wrap `EventHistorySequencer` so both mappers and future strategies use a single sequencing entrypoint. | `EventHistoryMapper.buildEvents` branches, any future strategy classes. |

**Implementation Plan**

1. **Introduce utilities without wiring**  
   - Add helper classes under `service.robotics.support` with unit tests covering edge cases (null timestamps, truncation when length < 24, varied RPA template inputs).  
   - Ensure helpers consume `Time`, `FeatureToggleService`, or other dependencies via constructor injection to keep behaviour testable.

2. **Replace date/time fallbacks**  
   - Swap direct `LocalDateTime.now()` usage in `EventHistoryMapper` with the helper while keeping method signatures unchanged.  
   - Update both data mappers to request formatted dates through the helper; re-run golden snapshots to confirm parity.

3. **Centralise text templates**  
   - Move constant definitions and `String.format` calls into `RoboticsEventTextFormatter`; update all branches that emit “RPA Reason …” text.  
   - Add regression-focused unit tests exercising the formatter with representative messages from golden fixtures.

4. **Consolidate party/sequencing logic**  
   - Refactor duplicated party ID/solicitor sections in both data mappers to delegate to the new helper.  
   - Replace inline `prepareEventSequence` calls with the shared generator, ensuring sequencing increments remain identical (compare golden outputs).

5. **Stabilise Spring wiring and tests**  
   - Register utilities as Spring beans and extend existing mapper tests (`EventHistoryMapperTest`, `RoboticsDataMapperTest`, `RoboticsDataMapperForSpecTest`) to cover the new collaborators.  
   - Re-run `EventHistoryGoldenSnapshotTest` plus mapper unit suites after each integration step.

**Regression Guardrails**

- Continue to execute `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest` after every batch of replacements.
- When text templates move, spot-check golden fixtures most sensitive to messaging changes (`unspec_ga_strike_out.json`, `default_judgment_unspec.json`, `spec_mediation_part_admit.json`) to ensure zero drift.
- Hold the existing golden corpus as Phase 1’s acceptance gate; no new scenarios required unless we expose additional surface area.

**Progress Log**

- 2025-10-29 (pass 1): Added foundational helpers (`RoboticsTimelineHelper`, `RoboticsEventTextFormatter`, `RoboticsPartyLookup`, `RoboticsSequenceGenerator`) with standalone unit tests; wiring into mappers scheduled for the next pass.
- 2025-10-29 (pass 2): Replaced `LocalDateTime.now()` usages in `EventHistoryMapper` with `RoboticsTimelineHelper`, registered the helper as a Spring bean, and updated mapper tests to rely on deterministic `Time` mocks; golden snapshots and `EventHistoryMapperTest` suites re-run to confirm parity.
- 2025-10-29 (pass 3): Wired `RoboticsEventTextFormatter`, `RoboticsPartyLookup`, and `RoboticsSequenceGenerator` into `EventHistoryMapper`; delegated sequence calculations to the shared helper and swapped key RPA reason strings (defendant response outcomes, NoC/LR vs LiP messages) to the formatter while keeping snapshot outputs unchanged.
- 2025-10-29 (pass 4): Ported the claim-dismissal/offline RPA messages to formatter helpers for consistency; regression suites re-run to confirm payload parity.
- 2025-10-29 (pass 5): Migrated multiparty intention/proceed text to formatter helpers and aligned the 1v1 proceed strings via the shared constant; mapper and golden snapshot suites remain green.
- 2025-10-29 (pass 6): Extracted the remaining manual determination/judgment/offline strings to formatter helpers (including JO pathways and “only one respondent notified”); mapper + golden suites and formatter unit tests re-run successfully.
- 2025-10-29 (pass 7): Swept up the residual RPA literals (unrepresented/unregistered defendants, SDONotDrawn, notice-of-change, applicant response fallback) into formatter helpers; regression suites remain green.
- 2025-10-29 (pass 8): Centralised the “Claimant proceeds” messaging through `RoboticsEventTextFormatter.claimantProceeds()`, removed the last hard-coded RPA literal, and re-ran mapper plus golden snapshot suites.
- 2025-10-29 (pass 9): Extended the formatter across Camunda task/event handlers, introduced helper coverage for all remaining RPA messages, and re-ran handler + robotics regression tests to confirm parity.
- 2025-10-29 (pass 10): Adopted `RoboticsPartyLookup` in the unspec/spec data mappers for solicitor reference truncation and party IDs, refreshed mapper tests, and reverified the golden suite to ensure unchanged payloads.

**Phase 1 Closure Checklist**

- [x] Confirm no remaining inline robotics strings outside helpers (scan before moving to strategies).
- [x] Share helper usage guidelines with strategy owners (see below).
- [x] Lock in utility unit tests for any new helper APIs introduced during strategy work.
- [x] Decide handover point into Phase 2 (candidate: breathing space strategy extraction) once checks above are green.

**Helper Usage Notes (for Phase 2 authors)**

- Always source RPA text via `RoboticsEventTextFormatter`; if a phrase is missing, add a helper and cover it in `RoboticsEventTextFormatterTest`.
- Rely on `RoboticsPartyLookup` for litigious party IDs and solicitor reference truncation—avoid reintroducing `substring` logic or hard-coded IDs.
- Use `RoboticsTimelineHelper` (and the injected `Time` mock) for any new date/timestamp fallbacks so golden tests remain deterministic.
- Fetch sequencing via `RoboticsSequenceGenerator.nextSequence(...)` when emitting additional robotics events.
- Before deleting any mapper branch, ensure the replacement contributor relies on these shared helpers and is exercised by focussed unit tests plus a golden snapshot regression run.

## Phase 2 – Strategy Extraction (Plan)

**Initial target**

- Start with breathing-space logic (standard + mental-health). It is self-contained (`caseData.getBreathing()`), already covered by golden fixtures, and exercises the new timeline/formatter helpers.

**Contributor structure**

- Define an `EventHistoryContributor` SPI (e.g. `supports(caseData)` + `contribute(builder, caseData, authToken)`).
- Implement `BreathingSpaceEventContributor`, injecting `RoboticsTimelineHelper`, `RoboticsSequenceGenerator`, and reusing the shared helpers.
- The contributor emits the enter/lift events (standard and mental-health) using the helper to build deterministic timestamps and shared message templates.

**Mapper orchestration**

- Inject a list of contributors into `EventHistoryMapper`.
- During `buildEvents`, iterate the contributors and delegate when `supports` returns true.
- Remove the inline `buildBreathingSpaceEvent` branch once the contributor is wired, keeping sequencing/timeline helpers in the contributor layer.

**Testing**

- Add a focussed unit test for `BreathingSpaceEventContributor` covering:
  - Standard enter with/without reference/start dates (timeline fallback).
  - Standard lift with expected end missing.
  - Mental-health enter/lift variants.
- Re-run `EventHistoryMapperTest` and the golden snapshot suite to ensure emitted JSON is unchanged.

**Documentation**

- Log each contributor extraction as a new pass in this file (what moved, helpers used, tests run).
- Capture any necessary follow-up (e.g. contributors needing additional shared utilities).

**Next candidates**

- After breathing space, prioritise contributors for default judgment/offline, mediation, and paper proceedings, repeating the same extraction/testing pattern.

**Progress Log**

- 2025-10-29 (Phase 2 – pass 1): Introduced `EventHistoryContributor` SPI, extracted breathing-space logic into `BreathingSpaceEventContributor`, wired it into `EventHistoryMapper`, and added focussed contributor tests alongside the golden regression sweep.
- 2025-10-29 (Phase 2 – pass 2): Extracted default-judgment events to `DefaultJudgmentEventContributor`, recreated the miscellaneous DJ messaging via the new helper, refreshed mapper/data-mapper beans, and kept golden snapshots unchanged.
- 2025-10-29 (Phase 2 – pass 3): Moved mediation handling into `MediationEventContributor`, centralised claimant DQ helpers via `RoboticsDirectionsQuestionnaireSupport`, added `RoboticsEventTextFormatter.inMediation()`, refreshed Spring test wiring, and ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.MediationEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperTest`.
- 2025-10-29 (Phase 2 – pass 4): Added `DefendantNoCDeadlineContributor` to emit the NoC deadline offline event, removed the mapper-side helper, and extended the contributor/unit/golden regression runs via `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.DefendantNoCDeadlineContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.MediationEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperTest`.
- 2025-10-29 (Phase 2 – pass 5): Extracted the `CaseProceedsInCasemanContributor`, deleted the legacy `buildClaimTakenOfflineAfterSDO` branch, hardened contributor tests for SDO/non-SDO paths, and re-ran the focussed regression cut (`JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseProceedsInCasemanContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.DefendantNoCDeadlineContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.MediationEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.BreathingSpaceEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.DefaultJudgmentEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpecTest`).
- 2025-10-29 (Phase 2 – pass 6): Lifted the manual “taken offline by staff” path into `TakenOfflineByStaffEventContributor`, introduced `RoboticsManualOfflineSupport` for the shared wording, and gated contributor execution with the state-flow history so non-offline scenarios stay untouched. Regression command: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineByStaffEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseProceedsInCasemanContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.DefendantNoCDeadlineContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.MediationEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.BreathingSpaceEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.DefaultJudgmentEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpecTest`.
- 2025-10-29 (Phase 2 – pass 11): Added contributors for claim issued/notified/details-notified and the post-claim-notification offline branch (`ClaimIssuedEventContributor`, `ClaimNotifiedEventContributor`, `ClaimDetailsNotifiedEventContributor`, `TakenOfflineAfterClaimNotifiedContributor`), dropped the mapper helpers, and re-ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimIssuedEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimNotifiedEventContributorTest`.
- 2025-10-29 (Phase 2 – pass 12): Extracted the claim-details/applicant-response offline events, claim-dismissed variants, litigation-friend, and case query emissions into contributors (`TakenOfflineAfterClaimDetailsNotifiedContributor`, `TakenOfflinePastApplicantResponseContributor`, `ClaimDismissedPastNotificationsContributor`, `ClaimDismissedPastDeadlineContributor`, `RespondentLitigationFriendContributor`, `CaseQueriesContributor`), removed the mapper implementations, and ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimIssuedEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimNotifiedEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDetailsNotifiedEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineAfterClaimNotifiedContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineAfterClaimDetailsNotifiedContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflinePastApplicantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDismissedPastNotificationsContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDismissedPastDeadlineContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentLitigationFriendContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseQueriesContributorTest`.
- 2025-10-29 (Phase 2 – pass 13): Shifted the unrepresented/unregistered defendant branches into `UnrepresentedDefendantContributor`, `UnregisteredDefendantContributor`, and `UnrepresentedAndUnregisteredDefendantContributor`, deleted the mapper helpers, and validated with `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimIssuedEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimNotifiedEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDetailsNotifiedEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineAfterClaimNotifiedContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineAfterClaimDetailsNotifiedContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflinePastApplicantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDismissedPastNotificationsContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDismissedPastDeadlineContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentLitigationFriendContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseQueriesContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.UnrepresentedDefendantContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.UnregisteredDefendantContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.UnrepresentedAndUnregisteredDefendantContributorTest`.
- 2025-10-29 (Phase 2 – pass 14): Moved the acknowledgement-of-service logic into `AcknowledgementOfServiceContributor`, removed the mapper branch, and reran the focussed regression suite `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.AcknowledgementOfServiceContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimIssuedEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimNotifiedEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDetailsNotifiedEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineAfterClaimNotifiedContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineAfterClaimDetailsNotifiedContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflinePastApplicantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDismissedPastNotificationsContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDismissedPastDeadlineContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentLitigationFriendContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseQueriesContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.UnrepresentedDefendantContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.UnregisteredDefendantContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.UnrepresentedAndUnregisteredDefendantContributorTest`.
- 2025-10-29 (Phase 2 – pass 15): Deleted the unused `buildDefaultJudgment` and `buildMiscellaneousDJEvent` mapper remnants now covered by contributors, keeping the mapper surface aligned with the strategy layer.
- 2025-10-29 (Phase 2 – pass 16): Extracted consent-extension events into `ConsentExtensionEventContributor`, removed the mapper helper/Spec wiring, added focussed contributor tests, and re-ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventContributorTest`.
- 2025-10-29 (Phase 2 – pass 17): Moved strike-out GA handling into `GeneralApplicationStrikeOutContributor`, removed the mapper branch/helpers, added focussed contributor tests, and re-ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutContributorTest`.
- 2025-10-29 (Phase 2 – pass 18): Shifted claimant-response logic into `ClaimantResponseContributor`, removed mapper helpers (proceed/not-proceed, multi-party messaging, applicant DQs), added focussed contributor tests, and re-ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseContributorTest`.
- 2025-10-29 (Phase 2 – pass 19): Extracted case-note emissions into `CaseNotesContributor`, deleted the mapper case-note helpers, added focussed contributor tests, wired the bean into `EventHistoryMapperTest`, and ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseNotesContributorTest`.
- 2025-10-29 (Phase 2 – pass 20): Introduced `TakenOfflineSpecDefendantNocContributor` for the spec defendant notice-of-change offline path, removed the mapper helper, added focussed contributor tests, extended the Spring slice wiring, and ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseNotesContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineSpecDefendantNocContributorTest`.
- 2025-10-29 (Phase 2 – pass 21): Extracted the SDO-not-drawn branch into `SdoNotDrawnContributor`, removed the mapper method, added focussed contributor tests, wired the bean into the mapper slice, and re-ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseNotesContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineSpecDefendantNocContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SdoNotDrawnContributorTest`.
- 2025-10-29 (Phase 2 – pass 22): Moved the spec repayment-plan rejection event into `SpecRejectRepaymentPlanContributor`, deleted the mapper helper + sequence guard, added dedicated unit coverage, wired the bean into the Spring slice, and validated with `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseNotesContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineSpecDefendantNocContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SdoNotDrawnContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.InterlocutoryJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SummaryJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.JudgmentByAdmissionContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SetAsideJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SpecRejectRepaymentPlanContributorTest`.
- 2025-10-29 (Phase 2 – pass 22): Moved the summary-judgment miscellaneous event into `SummaryJudgmentContributor`, dropped the mapper helper/call, added focussed contributor coverage, wired the bean into the Spring slice, and re-ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseNotesContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineSpecDefendantNocContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SdoNotDrawnContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SummaryJudgmentContributorTest`.
- 2025-10-29 (Phase 2 – pass 23): Shifted the interlocutory-judgment branch into `InterlocutoryJudgmentContributor`, removed the mapper helper/call, added focussed contributor tests, updated the Spring slice, and re-ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseNotesContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineSpecDefendantNocContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SdoNotDrawnContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SummaryJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.InterlocutoryJudgmentContributorTest`.
- 2025-10-29 (Phase 2 – pass 24): Broke out the JO set-aside branch into `SetAsideJudgmentContributor`, deleted the legacy mapper helper, ported the detail builder, added focussed contributor tests, and re-ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SetAsideJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.InterlocutoryJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SummaryJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseNotesContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineSpecDefendantNocContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SdoNotDrawnContributorTest`.
- 2025-10-29 (Phase 2 – pass 25): Extracted the COSC event into `CertificateOfSatisfactionOrCancellationContributor`, mirrored the original sequencing/date guards, added focussed unit tests, refreshed the mapper slice, and re-ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CertificateOfSatisfactionOrCancellationContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SetAsideJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.InterlocutoryJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SummaryJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseNotesContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineSpecDefendantNocContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SdoNotDrawnContributorTest`.
- 2025-10-29 (Phase 2 – pass 26): Moved the CCJ judgment-by-admission flow into `JudgmentByAdmissionContributor`, removed the mapper helper + supporting methods, added focussed contributor tests, refreshed Spring wiring, and re-ran `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :test --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapperTest --tests uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryGoldenSnapshotTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.JudgmentByAdmissionContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CertificateOfSatisfactionOrCancellationContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SetAsideJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.InterlocutoryJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SummaryJudgmentContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseNotesContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineSpecDefendantNocContributorTest --tests uk.gov.hmcts.reform.civil.service.robotics.strategy.SdoNotDrawnContributorTest`.
---

## Future Notes

- Append findings, utilities, and strategy ownership here as phases progress.
- Record any intentional behavioural changes alongside updated golden fixtures.
