package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.judgementbyadmission;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedDefendantDashboardTask;

@Component
public class JudgementByAdmissionNonDivergentSpecDashboardTaskContributor extends DashboardTaskContributor {

    public JudgementByAdmissionNonDivergentSpecDashboardTaskContributor(
        JudgmentByAdmissionIssuedClaimantDashboardTask claimantTask,
        JudgmentByAdmissionIssuedDefendantDashboardTask defendantTask
    ) {
        super(
            DashboardTaskIds.JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC,
            claimantTask,
            defendantTask
        );
    }
}
