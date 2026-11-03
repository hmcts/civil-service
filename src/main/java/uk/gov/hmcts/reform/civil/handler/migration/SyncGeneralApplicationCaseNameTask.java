package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.GeneralApplicationCaseNameService;

@Component
public class SyncGeneralApplicationCaseNameTask extends MigrationTask<CaseReference> {

    private final GeneralApplicationCaseNameService generalApplicationCaseNameService;

    public SyncGeneralApplicationCaseNameTask(GeneralApplicationCaseNameService generalApplicationCaseNameService) {
        super(CaseReference.class);
        this.generalApplicationCaseNameService = generalApplicationCaseNameService;
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

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseReference) {
        if (caseData == null || caseReference == null) {
            throw new IllegalArgumentException("CaseData and CaseReference must not be null");
        }
        generalApplicationCaseNameService.updateGeneralApplications(caseData);
        return caseData;
    }
}
