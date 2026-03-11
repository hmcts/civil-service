package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.UnAssignCaseReference;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

@Component
public class UnAssignUserWithACaseRole extends MigrationTask<UnAssignCaseReference> {

    public final CoreCaseUserService coreCaseUserService;

    public UnAssignUserWithACaseRole(CoreCaseUserService coreCaseUserService1) {
        super(UnAssignCaseReference.class);
        this.coreCaseUserService = coreCaseUserService1;
    }

    protected CaseData migrateCaseData(CaseData caseData, UnAssignCaseReference unassignCaseReference) {
        if (caseData == null || unassignCaseReference.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseData and CaseReference must not be null");
        }

        coreCaseUserService.unassignCase(unassignCaseReference.getCaseReference(),
                                         unassignCaseReference.getUserId(),
                                         unassignCaseReference.getOrganisationId(),
                                         CaseRole.valueOf(unassignCaseReference.getCaseRole())
        );
        return caseData;
    }

    @Override
    protected String getEventSummary() {
        return "UnAssign a case role to a case";
    }

    @Override
    protected String getEventDescription() {
        return "This task is used to unassign a case role to a case";
    }

    @Override
    protected String getTaskName() {
        return "UnAssignUserWithACaseRole";
    }
}
