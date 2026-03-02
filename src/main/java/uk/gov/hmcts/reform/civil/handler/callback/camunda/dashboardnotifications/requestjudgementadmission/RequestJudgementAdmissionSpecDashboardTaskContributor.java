package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.requestjudgementadmission;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ccjrequested.CcjRequestedClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ccjrequested.CcjRequestedDefendantDashboardTask;

@Component
public class RequestJudgementAdmissionSpecDashboardTaskContributor extends DashboardTaskContributor {

    public RequestJudgementAdmissionSpecDashboardTaskContributor(
        ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask,
        CcjRequestedClaimantDashboardTask claimantCcjTask,
        ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask,
        CcjRequestedDefendantDashboardTask defendantCcjTask
    ) {
        super(
            DashboardTaskIds.REQUEST_JUDGEMENT_ADMISSION_SPEC,
            claimantOfflineTask,
            claimantCcjTask,
            defendantOfflineTask,
            defendantCcjTask
        );
    }
}
