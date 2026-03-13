package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.AssignCaseReference;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

@Component
public class AssignUserWithACaseRole extends MigrationTask<AssignCaseReference> {

    public final CoreCaseUserService coreCaseUserService;

    public AssignUserWithACaseRole(CoreCaseUserService coreCaseUserService1) {
        super(AssignCaseReference.class);
        this.coreCaseUserService = coreCaseUserService1;
    }

    @Override
    protected String getEventSummary() {
        return "Assign a case role to a case";
    }

    protected CaseData migrateCaseData(CaseData caseData, AssignCaseReference assignCaseReference) {
        if (caseData == null || assignCaseReference.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseData and CaseReference must not be null");
        }

        coreCaseUserService.assignCase(assignCaseReference.getCaseReference(),
                                       assignCaseReference.getUserId(),
                                       assignCaseReference.getOrganisationId(),
                                       CaseRole.valueOf(assignCaseReference.getCaseRole())
        );
        return caseData;
    }

    @Override
    protected String getEventDescription() {
        return "This task is used to assign a case role to a case";
    }

    @Override
    protected String getTaskName() {
        return "AssignUserWithACaseRole";
    }
}
