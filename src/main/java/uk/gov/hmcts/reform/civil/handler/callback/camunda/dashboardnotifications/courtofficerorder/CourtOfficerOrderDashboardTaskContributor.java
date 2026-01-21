package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.courtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class CourtOfficerOrderDashboardTaskContributor extends DashboardTaskContributor {

    public CourtOfficerOrderDashboardTaskContributor(CourtOfficerOrderClaimantDashboardTask claimantTask,
                                                      CourtOfficerOrderDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.COURT_OFFICER_ORDER,
            claimantTask,
            defendantTask
        );
    }
}
