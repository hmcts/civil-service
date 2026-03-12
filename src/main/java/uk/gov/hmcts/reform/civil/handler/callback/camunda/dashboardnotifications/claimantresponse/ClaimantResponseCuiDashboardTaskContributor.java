package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class ClaimantResponseCuiDashboardTaskContributor extends DashboardTaskContributor {

    public ClaimantResponseCuiDashboardTaskContributor(
        ClaimantResponseCuiDashboardNotificationsTask notificationsTask
    ) {
        super(
            DashboardTaskIds.CLAIMANT_RESPONSE_CUI,
            notificationsTask
        );
    }
}
