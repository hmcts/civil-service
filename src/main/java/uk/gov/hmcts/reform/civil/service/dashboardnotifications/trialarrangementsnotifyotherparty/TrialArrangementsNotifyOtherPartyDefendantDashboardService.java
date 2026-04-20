package uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialarrangementsnotifyotherparty;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_DEFENDANT;

@Service
public class TrialArrangementsNotifyOtherPartyDefendantDashboardService extends DashboardScenarioService {

    public TrialArrangementsNotifyOtherPartyDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                                      DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyTrialArrangementsNotifyOtherParty(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

}
