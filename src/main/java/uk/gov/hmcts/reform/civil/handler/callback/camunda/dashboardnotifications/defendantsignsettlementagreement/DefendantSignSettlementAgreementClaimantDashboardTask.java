package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantsignsettlementagreement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantsignsettlementagreement.DefendantSignSettlementAgreementClaimantDashboardService;

@Component
public class DefendantSignSettlementAgreementClaimantDashboardTask extends DashboardServiceTask {

    private final DefendantSignSettlementAgreementClaimantDashboardService dashboardService;

    public DefendantSignSettlementAgreementClaimantDashboardTask(DefendantSignSettlementAgreementClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDefendantSignSettlementAgreement(caseData, authToken);
    }
}
