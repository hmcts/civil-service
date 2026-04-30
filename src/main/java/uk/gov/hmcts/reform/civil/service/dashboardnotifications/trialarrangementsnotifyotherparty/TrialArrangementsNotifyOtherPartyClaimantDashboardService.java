package uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialarrangementsnotifyotherparty;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_LR_CLAIMANT;

@Service
public class TrialArrangementsNotifyOtherPartyClaimantDashboardService extends DashboardScenarioService {

    public TrialArrangementsNotifyOtherPartyClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                                     DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyTrialArrangementsNotifyOtherParty(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented()
            ? SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_CLAIMANT.getScenario()
            : SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_LR_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        // This service is called via the registry when the claimant confirms trial arrangements
        // (APPLICANT_TRIAL_READY_NOTIFY_OTHERS BPMN). The claimant should NOT receive
        // a "the other party confirmed" notification about their own confirmation.
        // Only notify the claimant when the defendant has confirmed trial arrangements.
        return YesOrNo.YES.equals(caseData.getTrialReadyRespondent1());
    }
}
