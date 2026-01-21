package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardTask;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardTask;

@Component
public class ClaimantResponseCuiDashboardTaskContributor extends DashboardTaskContributor {

    public ClaimantResponseCuiDashboardTaskContributor(
        ClaimantResponseClaimantDashboardTask claimantTask,
        ClaimantResponseDefendantDashboardTask defendantTask,
        ClaimantCcjResponseClaimantDashboardTask claimantCcjTask,
        ClaimantCcjResponseDefendantDashboardTask defendantCcjTask,
        ApplicationsProceedOfflineClaimantDashboardTask claimantOfflineTask,
        ApplicationsProceedOfflineDefendantDashboardTask defendantOfflineTask,
        JudgmentByAdmissionIssuedClaimantDashboardTask judgmentByAdmissionClaimantTask,
        JudgmentByAdmissionIssuedDefendantDashboardTask judgmentByAdmissionDefendantTask
    ) {
        super(
            DashboardTaskIds.CLAIMANT_RESPONSE_CUI,
            claimantTask,
            defendantTask,
            claimantOfflineTask,
            defendantOfflineTask,
            claimantCcjTask,
            defendantCcjTask,
            judgmentByAdmissionClaimantTask,
            judgmentByAdmissionDefendantTask
        );
    }
}
