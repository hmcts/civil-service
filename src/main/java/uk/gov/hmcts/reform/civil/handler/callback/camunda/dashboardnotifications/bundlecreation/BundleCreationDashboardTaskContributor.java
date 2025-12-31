package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.bundlecreation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class BundleCreationDashboardTaskContributor extends DashboardTaskContributor {

    public BundleCreationDashboardTaskContributor(BundleCreationDefendantDashboardTask defendantTask,
                                                  BundleCreationClaimantDashboardTask claimantTask) {
        super(
            DashboardTaskIds.BUNDLE_CREATION,
            defendantTask,
            claimantTask
        );
    }
}
