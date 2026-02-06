package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.finalorder;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.GaDashboardScenarioService;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT;

@Service
public class FinalOrderRespondentDashboardService extends GaDashboardScenarioService {

    public FinalOrderRespondentDashboardService(DashboardApiClient dashboardApiClient,
                                                GaDashboardNotificationsParamsMapper mapper) {
        super(dashboardApiClient, mapper);
    }

    public void notifyFinalOrder(GeneralApplicationCaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(GeneralApplicationCaseData caseData) {
        return isWithNoticeOrConsent(caseData)
            ? SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario()
            : "";
    }

    private boolean isWithNoticeOrConsent(GeneralApplicationCaseData caseData) {
        return YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice())
            || YES.equals(caseData.getGeneralAppConsentOrder());
    }
}
