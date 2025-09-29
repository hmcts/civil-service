package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;

@Component
public class ChangeSubmittedDateMigrationTask extends MigrationTask<CaseReference> {

    public ChangeSubmittedDateMigrationTask() {
        super(CaseReference.class);
    }

    @Override
    protected String getTaskName() {
        return "ChangeSubmittedDateMigrationTask";
    }

    @Override
    protected String getEventSummary() {
        return "Change Submitted Date Migration";
    }

    @Override
    protected String getEventDescription() {
        return "This task changes the submitted date for cases based on the provided case references.";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseReference) {
        // Implement the logic to change the submitted date in caseData based on caseReference
        // This is a placeholder implementation
        if (caseData == null || caseReference == null) {
            throw new IllegalArgumentException("CaseData and CaseReference must not be null");
        } else {
            // Example logic: Set the submitted date to a fixed date for demonstration purposes
            return caseData.toBuilder().submittedDate(LocalDateTime.now()).build();
        }
    }
}
