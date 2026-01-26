package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.createlipclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class CreateLipClaimDashboardTaskContributor extends DashboardTaskContributor {

    public CreateLipClaimDashboardTaskContributor(CreateLipClaimDashboardTask task) {
        super(
            DashboardTaskIds.CREATE_LIP_CLAIM,
            task
        );
    }
}
