package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.informagreedextensiondatespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class InformAgreedExtensionDateSpecDashboardTaskContributor extends DashboardTaskContributor {

    public InformAgreedExtensionDateSpecDashboardTaskContributor(InformAgreedExtensionDateSpecClaimantDashboardTask claimantTask,
                                                                 InformAgreedExtensionDateSpecDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.INFORM_AGREED_EXTENSION_DATE_SPEC,
            claimantTask,
            defendantTask
        );
    }
}
