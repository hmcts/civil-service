package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Component
public class RemoveCaseDismissedHearingDueDateMigrationTask extends MigrationTask<CaseReference> {

    private static final String CASE_DISMISSED_HEARING_FEE_DUE_DATE = "caseDismissedHearingFeeDueDate";

    public RemoveCaseDismissedHearingDueDateMigrationTask() {
        super(CaseReference.class);
    }

    @Override
    protected String getTaskName() {
        return "RemoveCaseDismissedHearingDueDateMigrationTask";
    }

    @Override
    protected String getEventDescription() {
        return "This task removes the hearing dismissed due date for cases based on the provided case references.";
    }

    @Override
    protected List<String> getFieldsToNullify() {
        return List.of(CASE_DISMISSED_HEARING_FEE_DUE_DATE);
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseReference) {
        if (caseData == null || caseReference == null) {
            throw new IllegalArgumentException("CaseData and CaseReference must not be null");
        } else {
            return caseData;
        }
    }

    @Override
    protected String getEventSummary() {
        return "Remove hearing dismissed due date Migration";
    }
}
