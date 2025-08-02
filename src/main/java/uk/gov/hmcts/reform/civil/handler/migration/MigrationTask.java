package uk.gov.hmcts.reform.civil.handler.migration;

import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;

public abstract class MigrationTask<T extends CaseReference> {

    private final Class<T> type;

    protected MigrationTask(Class<T> type) {
        this.type = type;
    }

    protected abstract String getTaskName();

    protected abstract String getEventSummary();

    protected abstract String getEventDescription();

    protected abstract CaseData migrateCaseData(CaseData caseData, T caseReference);

    protected Class<T> getType() {
        return type;
    }
}
