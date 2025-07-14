package uk.gov.hmcts.reform.civil.handler.migration;

import uk.gov.hmcts.reform.civil.model.CaseData;

public abstract class MigrationTask {

    protected abstract String getTaskName();

    protected abstract String getEventSummary();

    protected abstract String getEventDescription();

    protected abstract CaseData migrateCaseData(CaseData caseData);

}
