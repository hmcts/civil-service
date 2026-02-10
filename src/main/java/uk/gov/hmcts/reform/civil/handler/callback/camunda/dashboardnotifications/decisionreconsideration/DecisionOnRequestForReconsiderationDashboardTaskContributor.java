package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.decisionreconsideration;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.springframework.stereotype.Component;

@Component
public class DecisionOnRequestForReconsiderationDashboardTaskContributor extends DashboardTaskContributor {

    public DecisionOnRequestForReconsiderationDashboardTaskContributor(DecisionOnRequestForReconsiderationClaimantDashboardTask claimantTask,
                                                                       DecisionOnRequestForReconsiderationDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.DECISION_RECONSIDERATION,
            claimantTask,
            defendantTask
        );
    }
}
