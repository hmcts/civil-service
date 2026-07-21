package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DismissedDeadlineCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;

@Component
public class ChangeDismissedDeadlineDateMigrationTask extends MigrationTask<DismissedDeadlineCaseReference> {

    public ChangeDismissedDeadlineDateMigrationTask() {
        super(DismissedDeadlineCaseReference.class);
    }

    @Override
    protected String getEventSummary() {
        return "Change Dismissed Deadline Date Migration";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, DismissedDeadlineCaseReference caseReference) {
        if (caseData == null) {
            throw new IllegalArgumentException("CaseData must not be null");
        }
        if (caseReference == null || caseReference.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseReference fields must not be null");
        }
        String dismissedDeadline = caseReference.getDismissedDeadline();
        if (dismissedDeadline != null) {
            caseData.setClaimDismissedDeadline(LocalDateTime.parse(caseReference.getDismissedDeadline()));
        }
        return caseData;
    }

    @Override
    protected String getEventDescription() {
        return "This task changes the dismissed deadline date for cases based on the provided case references.";
    }

    @Override
    protected String getTaskName() {
        return "ChangeDismissedDeadlineDateMigrationTask";
    }
}
