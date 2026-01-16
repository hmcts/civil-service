package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseAssignmentMigrationCaseReference;
import uk.gov.hmcts.reform.civil.controllers.testingsupport.CaseAssignmentSupportService;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

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
        ensurePresent(caseReference, "Case reference obj must not be null");
        ensurePresent(caseReference.getCaseReference(), "Case reference must not be null");

        Map<String, String> requiredFields = Map.of(
            "userEmailAddress", caseReference.getUserEmailAddress(),
            "organisationId", caseReference.getOrganisationId()
        );

        requiredFields.forEach((field, value) -> {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(field + " must be provided");
            }
        });
    }

    private void ensurePresent(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
