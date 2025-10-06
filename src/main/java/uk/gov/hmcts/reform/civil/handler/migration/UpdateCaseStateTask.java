package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Component
public class UpdateCaseStateTask extends MigrationTask<CaseReference> {

    public UpdateCaseStateTask() {
        super(CaseReference.class);
    }

    @Override
    protected String getTaskName() {
        return "UpdateCaseStateTask";
    }

    @Override
    protected String getEventSummary() {
        return "Update case state via migration task";
    }

    @Override
    protected String getEventDescription() {
        return "This task update state on the case";
    }

    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseRef) {
        return caseData;
    }
}
