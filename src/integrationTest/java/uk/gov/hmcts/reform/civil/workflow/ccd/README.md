# Judgment & Default Judgment Integration Tests

## Test Ownership

### civil-service integration tests (this directory)

Owns callback handler logic for judgment events:
- State transitions (e.g. `CASE_ISSUED` -> `All_FINAL_ORDERS_ISSUED` / `JUDGMENT_REQUESTED` / `PROCEEDS_IN_HERITAGE_SYSTEM`)
- Case data mutations (activeJudgment, businessProcess, joFields)
- Validation rules (eligibility, date validation, breathing space)
- Business process setup (camunda event, status)
- Error/rejection paths (invalid state, missing data, future dates)

### civil-ccd-definition E2E/API tests

Owns cross-service validation:
- CCD definition wiring (event access, field visibility)
- Work Allocation task creation/completion
- Camunda process orchestration (multi-step flows)
- Real environment behaviour (ES queries, scheduler timing)
- UI wizard flows (ExUI page interactions)
- Payment/Fees integration

### civil-citizen-ui functional tests

Owns CUI portal interactions:
- LiP journey UX (default judgment request flow)
- Frontend validation and rendering
- Dashboard notification display (citizen view)

## Covered Handlers

| Handler | Event | Test File |
|---------|-------|-----------|
| DefaultJudgementSpecHandler | DEFAULT_JUDGEMENT_SPEC | DefaultJudgementSpecWorkflowTest |
| DefaultJudgementHandler | DEFAULT_JUDGEMENT | DefaultJudgementWorkflowTest |
| DefaultJudgementGrantedSpecCallbackHandler | DEFAULT_JUDGEMENT_GRANTED_SPEC | DefaultJudgementGrantedSpecWorkflowTest |
| SetAsideJudgmentCallbackHandler | SET_ASIDE_JUDGMENT | SetAsideJudgmentWorkflowTest |
| RecordJudgmentCallbackHandler | RECORD_JUDGMENT | RecordJudgmentWorkflowTest |
| JudgmentPaidInFullCallbackHandler | JUDGMENT_PAID_IN_FULL | JudgmentPaidInFullWorkflowTest |
| EditJudgmentCallbackHandler | EDIT_JUDGMENT | EditJudgmentWorkflowTest |
| RequestJudgementByAdmissionForSpecCuiCallbackHandler | REQUEST_JUDGEMENT_ADMISSION_SPEC | RequestJudgementByAdmissionWorkflowTest |

## Skipped API Test Triage

Equivalent Codecept/Playwright API scenarios in `civil-ccd-definition` were classified in
`civil-ccd-definition/e2e/tests/api_tests/JUDGMENT_API_OWNERSHIP.md`.

| Skipped API Scenario | Decision | Rationale |
|---------------------|----------|-----------|
| Spec 1v2 / 2v1 DJ | Reduce | Party variants of `DEFAULT_JUDGEMENT_SPEC`; spec 1v1 kept as smoke |
| Spec 1v1 set aside after application | Reduce | `SetAsideJudgmentWorkflowTest`; 1v1 DJ+paid+set-aside kept as JO smoke |
| Spec 1v2 set aside / mark paid | Reduce | Same handlers as 1v1 JO smoke |
| Record Judgment mark paid in full (JO spec 1v1/1v2) | Remove | `RecordJudgmentWorkflowTest`, `EditJudgmentWorkflowTest`, `JudgmentPaidInFullWorkflowTest` |
| Refer To Judge Defence Received In Time | Retain skipped | Different event; not migrated |
| Case progression + WA take-offline skips | Remove | Remain skipped; WA listing / CIV-12451 |
| DTSCCI-5943 buffer mark paid/settle | Fix bug first | Service logic fixable, then migrate |
| JBA LiP / LR mark paid API | Retain | CUI and claimant-response CCJ are not the LiP admission callback |

## Remaining E2E/API Smoke Coverage

- Spec 1v1 DJ (`@civil-service-smoke` + Camunda PR/master)
- Spec 1v1 DJ + mark paid + set aside (nightly `@api-jo`)
- Unspec DJ then SDO, WA and case progression
- LR and LiP judgment by admission then mark paid
- Other-remedy DJ
