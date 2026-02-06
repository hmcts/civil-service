package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class ProcessCoscDashboardTaskContributor extends DashboardTaskContributor {

    protected ProcessCoscDashboardTaskContributor(CertificateGeneratedDefendantDashboardTask defendantDashboardTask) {
        super(DashboardTaskIds.PROCESS_COSC, defendantDashboardTask);
    }
}
