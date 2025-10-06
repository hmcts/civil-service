package uk.gov.hmcts.reform.civil.handler.migration;

import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

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

    public Optional<String> getUpdatedState(String state) {
        if (state == null || state.isEmpty()) {
            return Optional.empty();
        }

        try {
            CaseState cs = CaseState.valueOf(state.toUpperCase());
            return Optional.of(cs.name());
        } catch (IllegalArgumentException e) {
            // State not recognized in the enum
            return Optional.empty();
        }
    }
}
