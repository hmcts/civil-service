package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.DashboardScenarioCaseReference;
import uk.gov.hmcts.reform.civil.handler.event.DashboardScenarioProcessor;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Component
public class RetriggerDashboardScenarioTask extends MigrationTask<DashboardScenarioCaseReference> {

    public final DashboardScenarioProcessor processor;

    public RetriggerDashboardScenarioTask(
        DashboardScenarioProcessor processor) {
        super(DashboardScenarioCaseReference.class);
        this.processor = processor;
    }

    @Override
    protected String getTaskName() {
        return "RetriggerDashboardScenarioTask";
    }

    @Override
    protected String getEventSummary() {
        return "Retrigger dashboard scenario via Migration Task";
    }

    @Override
    protected String getEventDescription() {
        return "This task triggers current scenario on the case";
    }

    protected CaseData migrateCaseData(CaseData caseData, DashboardScenarioCaseReference caseRef) {
        if (caseRef == null || caseRef.getCaseReference() == null || caseRef.getDashboardScenario() == null) {
            throw new IllegalArgumentException("CaseReference fields must not be null");
        }

        // Perform the migration
        processor.createDashboardScenario(caseRef.getCaseReference(), caseRef.getDashboardScenario());

        return caseData;
    }
}
