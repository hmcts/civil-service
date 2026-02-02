package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.decisionoutcome;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class DecisionOutcomeDashboardTaskContributor extends DashboardTaskContributor {

    public DecisionOutcomeDashboardTaskContributor(DecisionOutcomeClaimantDashboardTask claimantTask,
                                                     DecisionOutcomeDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.MOVE_TO_DECISION_OUTCOME,
            claimantTask,
            defendantTask
        );
    }
}
