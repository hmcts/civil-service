package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.GeneralApplicationCaseNameService;

import java.util.List;

@Component
public class SyncGeneralApplicationCaseNameTask extends MigrationTask<CaseReference> {

    private final GeneralApplicationCaseNameService generalApplicationCaseNameService;

    public SyncGeneralApplicationCaseNameTask(GeneralApplicationCaseNameService generalApplicationCaseNameService) {
        super(CaseReference.class);
        this.generalApplicationCaseNameService = generalApplicationCaseNameService;
    }

    @Override
    protected CaseData migrateCaseData(CaseData parentCaseData, CaseReference parentCaseReference) {
        validate(parentCaseData, parentCaseReference);
        List<String> failedGaCaseReferences = generalApplicationCaseNameService.updateGeneralApplications(parentCaseData);
        if (!failedGaCaseReferences.isEmpty()) {
            throw new IllegalStateException(String.format(
                "Failed to sync case name on general applications %s for parent case %s",
                failedGaCaseReferences, parentCaseReference.getCaseReference()));
        }
        return parentCaseData;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    private void validate(CaseData parentCaseData, CaseReference parentCaseReference) {
        if (parentCaseData == null || parentCaseReference == null) {
            throw new IllegalArgumentException("Parent case data and parent case reference must be provided");
        }
        if (parentCaseData.getCaseNameHmctsInternal() == null) {
            throw new IllegalStateException(
                "Parent case " + parentCaseReference.getCaseReference() + " has no case name to sync");
        }
    }

    @Override
    protected String getTaskName() {
        return "SyncGeneralApplicationCaseNameTask";
    }

    @Override
    protected String getEventSummary() {
        return "Sync general application case names";
    }

    @Override
    protected String getEventDescription() {
        return "Resets the case name on linked general applications to the parent case name";
    }
}
