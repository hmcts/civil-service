package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.setasidejudgement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class SetAsideJudgementDashboardTaskContributor extends DashboardTaskContributor {

    public SetAsideJudgementDashboardTaskContributor(SetAsideJudgementClaimantDashboardTask claimantTask,
                                                     SetAsideJudgementDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.SET_ASIDE_JUDGMENT,
            claimantTask,
            defendantTask
        );
    }
}
