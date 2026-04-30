package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.judgementpaidinfull;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

import org.springframework.stereotype.Component;

@Component
public class JudgmentPaidDashboardTaskContributor extends DashboardTaskContributor {

    public JudgmentPaidDashboardTaskContributor(JudgmentPaidClaimantDashboardTask claimantDashboardTask,
                                                JudgmentPaidDefendantDashboardTask defendantDashboardTask) {
        super(
            DashboardTaskIds.JUDGEMENT_PAID_IN_FULL,
            claimantDashboardTask,
            defendantDashboardTask
        );
    }
}
