package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseProgressionDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_LR_CLAIMANT;

@Service
public class TrialArrangementsNotifyOtherPartyNotificationHandler extends CaseProgressionDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_CLAIMANT);
    public static final String TASK_ID = "GenerateClaimantDashboardNotificationTrialArrangementsNotifyParty";

    public TrialArrangementsNotifyOtherPartyNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                                DashboardNotificationsParamsMapper mapper,
                                                                FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented()
            ? SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_CLAIMANT.getScenario()
            : SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_NOTIFY_OTHER_PARTY_LR_CLAIMANT.getScenario();
    }
}
