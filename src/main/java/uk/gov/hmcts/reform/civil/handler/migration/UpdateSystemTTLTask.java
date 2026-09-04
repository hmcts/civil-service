package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.ExcelCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Component
public class UpdateSystemTTLTask extends MigrationTask<ExcelCaseReference> {

    public UpdateSystemTTLTask() {
        super(ExcelCaseReference.class);
    }

    @Override
    protected String getEventDescription() {
        return "This task updates system TTL on the case";
    }

    @Override
    protected String getEventSummary() {
        return "Update case system TTL via migration task";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, ExcelCaseReference caseReference) {
        if (caseData == null || caseReference == null || caseReference.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseData and CaseReference fields must not be null");
        }
        return caseData;
    }

    @Override
    protected String getTaskName() {
        return "UpdateSystemTTLTask";
    }
}
