package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponsedeadlinecheck;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class DefendantResponseDeadlineCheckDashboardTaskContributor extends DashboardTaskContributor {

    public DefendantResponseDeadlineCheckDashboardTaskContributor(
        DefendantResponseDeadlineCheckClaimantDashboardTask claimantTask,
        DefendantResponseDeadlineCheckDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.DEFENDANT_RESPONSE_DEADLINE_CHECK,
            claimantTask,
            defendantTask
        );
    }
}
