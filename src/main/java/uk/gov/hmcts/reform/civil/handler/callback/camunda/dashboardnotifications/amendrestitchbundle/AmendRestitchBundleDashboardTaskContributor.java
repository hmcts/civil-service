package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.amendrestitchbundle;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class AmendRestitchBundleDashboardTaskContributor extends DashboardTaskContributor {

    public AmendRestitchBundleDashboardTaskContributor(AmendRestitchBundleClaimantDashboardTask claimantTask,
                                                       AmendRestitchBundleDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.AMEND_RESTITCH_BUNDLE,
            claimantTask,
            defendantTask
        );
    }
}
