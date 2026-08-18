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

| Skipped API Scenario | Decision | Rationale |
|---------------------|----------|-----------|
| Record Judgment mark paid in full (JO spec 1v1) | Migrated | Service logic covered in JudgmentPaidInFullWorkflowTest |
| Refer To Judge Defence Received In Time | Migrated | Set-aside handler logic in SetAsideJudgmentWorkflowTest |
| Record Judgment mark paid 1v2 | Migrated | Same handler, multi-party variant |
| Case progression + WA tasks (DJ scenarios 05-18) | Retain as E2E | Require Work Allocation service |
| DTSCCI-5943 buffer mark paid/settle | Fix bug first | Service logic fixable, then migrate |
| JBA LiP scenario (citizen-ui) | Partially migrated | Service logic in RequestJudgementByAdmissionWorkflowTest; CUI flow retained |

## Remaining E2E/API Smoke Coverage

The following should remain as cross-service smoke tests:
- DJ -> SDO -> Case Progression (full Camunda journey)
- Judgment buffer scheduler timing (real ES + scheduler)
- CJES registration verification (external service)
- ExUI page form validation (CCD field visibility)
- WA task lifecycle after judgment events
