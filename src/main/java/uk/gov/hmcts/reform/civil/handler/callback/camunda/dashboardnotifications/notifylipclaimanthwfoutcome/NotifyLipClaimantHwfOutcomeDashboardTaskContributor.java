package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.notifylipclaimanthwfoutcome;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class NotifyLipClaimantHwfOutcomeDashboardTaskContributor extends DashboardTaskContributor {

    public NotifyLipClaimantHwfOutcomeDashboardTaskContributor(NotifyLipClaimantHwfOutcomeDashboardTask task) {
        super(DashboardTaskIds.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME, task);
    }
}
