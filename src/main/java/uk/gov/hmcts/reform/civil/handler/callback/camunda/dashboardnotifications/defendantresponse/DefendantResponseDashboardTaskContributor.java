package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class DefendantResponseDashboardTaskContributor extends DashboardTaskContributor {

    public DefendantResponseDashboardTaskContributor(DefendantResponseClaimantDashboardTask claimantTask,
                                                   DefendantResponseDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.DEFENDANT_RESPONSE,
            claimantTask,
            defendantTask
        );
    }
}
