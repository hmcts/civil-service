# Builder Pattern Analysis Report

## REFACTORING CANDIDATES (Internal classes)

### Simple @Builder classes (12 classes):
1. **IdamUserDetails** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model`
   - Usage: 1 instance in `citizen/defendant/LipDefendantCaseAssignmentService.java`
   - Complexity: Simple

2. **EventSubmissionParams** (`@Builder`)
   - Location: `uk.gov.hmcts.reform.civil.service.citizen.events`
   - Usage: 1 instance in `citizen/defendant/LipDefendantCaseAssignmentService.java`
   - Complexity: Simple

3. **DashboardResponse** (`@Builder`)
   - Location: `uk.gov.hmcts.reform.civil.model.citizenui`
   - Usage: 2 instances in `citizenui/DashboardClaimInfoService.java`
   - Complexity: Simple

4. **DashboardClaimInfo** (`@Builder`)
   - Location: `uk.gov.hmcts.reform.civil.model.citizenui`
   - Usage: 2 instances in `citizenui/DashboardClaimInfoService.java`, 1 in `claimstore/ClaimStoreService.java`
   - Complexity: Simple

5. **HelpWithFeesForTab** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model.citizenui`
   - Usage: 2 instances in `citizenui/HelpWithFeesForTabService.java`
   - Complexity: Simple

6. **CaseManagementCategoryElement** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model`
   - Usage: 1 instance in `citizen/UpdateCaseManagementDetailsService.java`
   - Complexity: Simple

7. **CaseManagementCategory** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model`
   - Usage: 1 instance in `citizen/UpdateCaseManagementDetailsService.java`
   - Complexity: Simple

8. **CaseLocationCivil** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model.defaultjudgment`
   - Usage: 2 instances in `citizen/UpdateCaseManagementDetailsService.java`
   - Complexity: Simple

9. **TaskManagementLocationTab** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model.dmnacourttasklocation`
   - Usage: 3 instances in `camunda/UpdateWaCourtLocationsService.java`
   - Complexity: Simple

10. **TaskManagementLocationTypes** (`@Builder(toBuilder=true)`)
    - Location: `uk.gov.hmcts.reform.civil.model.dmnacourttasklocation`
    - Usage: 1 instance in `camunda/UpdateWaCourtLocationsService.java`
    - Complexity: Simple

11. **TaskManagementLocationsModel** (`@Builder(toBuilder=true)`)
    - Location: `uk.gov.hmcts.reform.civil.model.dmnacourttasklocation`
    - Usage: 4 instances in `camunda/UpdateWaCourtLocationsService.java`
    - Complexity: Simple

12. **DefendantLinkStatus** (`@Builder`)
    - Location: `uk.gov.hmcts.reform.cmc.model`
    - Usage: 1 instance in `claimstore/ClaimStoreService.java`
    - Complexity: Simple

### @Builder(toBuilder=true) classes used with toBuilder() (8 classes):
1. **HelpWithFees** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model.citizenui`
   - Usage: 1 instance with builder(), 1 with toBuilder() in `citizen/HWFFeePaymentOutcomeService.java`
   - Complexity: Simple

2. **FeePaymentOutcomeDetails** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model.citizenui`
   - Usage: 1 instance with toBuilder() in `citizen/HWFFeePaymentOutcomeService.java`
   - Complexity: Simple

3. **HelpWithFeesDetails** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model.citizenui`
   - Usage: 2 instances with toBuilder() in `citizen/HWFFeePaymentOutcomeService.java`
   - Complexity: Simple

4. **RequestedCourt** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model.dq`
   - Usage: 1 instance with toBuilder() in `citizen/UpdateCaseManagementDetailsService.java`
   - Complexity: Simple

5. **Party** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model`
   - Usage: 1 instance with toBuilder() in `citizen/defendant/LipDefendantCaseAssignmentService.java`
   - Complexity: Simple

6. **Applicant1DQ** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model.dq`
   - Usage: 1 instance with toBuilder() in `citizen/UpdateCaseManagementDetailsService.java`
   - Complexity: Simple

7. **Respondent1DQ** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model.dq`
   - Usage: 1 instance with toBuilder() in `citizen/UpdateCaseManagementDetailsService.java`
   - Complexity: Simple

8. **CaseDataLiP** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.model.citizenui`
   - Usage: 1 instance with toBuilder() in `citizen/HWFFeePaymentOutcomeService.java`
   - Complexity: Simple

### Special Cases:
1. **LocationRefData** (`@Builder(toBuilder=true)`)
   - Location: `uk.gov.hmcts.reform.civil.referencedata.model`
   - Usage: 1 instance with builder() in `camunda/UpdateWaCourtLocationsService.java`
   - Complexity: **SPECIAL** - Has @JsonCreator annotation, used for JSON deserialization
   - Decision: **SKIP** - This is a DTO used for external API responses, should keep builder

## SKIP (External dependencies or special cases):
- None found in scoped packages (all builders are internal classes)

## Summary:
- **Total refactoring candidates**: 20 classes
- **Simple @Builder**: 12 classes
- **@Builder(toBuilder=true)**: 8 classes
- **Special cases (skip)**: 1 class (LocationRefData)
- **Total builder usages to replace**: ~25 instances across scoped service packages

