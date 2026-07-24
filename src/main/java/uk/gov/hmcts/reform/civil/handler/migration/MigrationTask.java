package uk.gov.hmcts.reform.civil.handler.migration;

import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;
import java.util.List;
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

    protected CaseData migrateGeneralApplicationCaseData(
        CaseData caseData,
        GeneralApplicationCaseData gaCaseData,
        T caseReference
    ) {
        return migrateCaseData(caseData, caseReference);
    }

    protected List<String> getFieldsToNullify() {
        return Collections.emptyList();
    }

    /**
     * Read-only tasks inspect a case and report without changing it. When true,
     * {@link AsyncCaseMigrationService} fetches the case without starting or submitting a CCD
     * event, so no entry is written to the case history.
     */
    public boolean isReadOnly() {
        return false;
    }

    protected Class<T> getType() {
        return type;
    }

    public Optional<String> getUpdatedState(String state) {
        if (state == null || state.isEmpty()) {
            return Optional.empty();
        }

        try {
            CaseState cs = CaseState.valueOf(state);
            return Optional.of(cs.name());
        } catch (IllegalArgumentException e) {
            // State not recognized in the enum
            return Optional.empty();
        }
    }
}
