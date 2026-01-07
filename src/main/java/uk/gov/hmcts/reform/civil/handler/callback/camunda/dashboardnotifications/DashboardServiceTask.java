package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Objects;

@Slf4j
public abstract class DashboardServiceTask extends DashboardWorkflowTask {

    @Override
    public final void execute(DashboardTaskContext context) {
        CaseData caseData = context.caseData();
        String authToken = Objects.requireNonNull(context.authToken(), "Missing auth token for dashboard notification task");
        log.info("Executing dashboard task {} for case {}", taskName(), caseData != null ? caseData.getCcdCaseReference() : null);
        notifyDashboard(caseData, authToken);
    }

    protected String taskName() {
        return getClass().getSimpleName();
    }

    protected abstract void notifyDashboard(CaseData caseData, String authToken);
}
