package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class DefendantResponseCuiDashboardTaskContributor extends DashboardTaskContributor {

    public DefendantResponseCuiDashboardTaskContributor(DefendantResponseCuiClaimantDashboardTask claimantTask,
                                                        DefendantResponseDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.DEFENDANT_RESPONSE_CUI,
            claimantTask,
            defendantTask
        );
    }
}
