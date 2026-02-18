package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantsignsettlementagreement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class DefendantSignSettlementAgreementDashboardTaskContributor extends DashboardTaskContributor {

    public DefendantSignSettlementAgreementDashboardTaskContributor(DefendantSignSettlementAgreementDefendantDashboardTask task) {
        super(
            DashboardTaskIds.DEFENDANT_SIGN_SETTLEMENT_AGREEMENT,
            task
        );
    }
}
