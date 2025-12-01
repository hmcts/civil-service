package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseAssignmentMigrationCaseReference;
import uk.gov.hmcts.reform.civil.controllers.testingsupport.CaseAssignmentSupportService;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Component
@Slf4j
public class UnassignCaseUserMigrationTask extends MigrationTask<CaseAssignmentMigrationCaseReference> {

    private final CaseAssignmentSupportService caseAssignmentSupportService;

    public UnassignCaseUserMigrationTask(CaseAssignmentSupportService caseAssignmentSupportService) {
        super(CaseAssignmentMigrationCaseReference.class);
        this.caseAssignmentSupportService = caseAssignmentSupportService;
    }

    @Override
    protected String getTaskName() {
        return "UnassignCaseUserMigrationTask";
    }

    @Override
    protected String getEventSummary() {
        return "Unassign user from case via migration task";
    }

    @Override
    protected String getEventDescription() {
        return "Removes the provided user from the supplied cases using CaseAssignmentSupportService";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseAssignmentMigrationCaseReference caseReference) {
        validate(caseReference);
        caseAssignmentSupportService.unAssignUserFromCasesByEmail(
            List.of(caseReference.getCaseReference()),
            caseReference.getOrganisationId(),
            caseReference.getUserEmailAddress()
        );
        log.info(
            "Unassigned user {} from case {}",
            caseReference.getUserEmailAddress(),
            caseReference.getCaseReference()
        );
        return caseData;
    }

    private void validate(CaseAssignmentMigrationCaseReference caseReference) {
        if (caseReference == null) {
            throw new IllegalArgumentException("Case reference must not be null");
        }
        if (caseReference.getCaseReference() == null) {
            throw new IllegalArgumentException("Case ID must not be null");
        }
        if (caseReference.getUserEmailAddress() == null || caseReference.getUserEmailAddress().isBlank()) {
            throw new IllegalArgumentException("userEmailAddress must be provided");
        }
        if (caseReference.getOrganisationId() == null || caseReference.getOrganisationId().isBlank()) {
            throw new IllegalArgumentException("organisationId must be provided");
        }
    }
}
