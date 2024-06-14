package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT;

@Service
public class CaseProceedOfflineDefendantNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE);
    private static final List<CaseState> caseProceedInCaseManStates = List.of(CaseState.AWAITING_APPLICANT_INTENTION,
            CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
            CaseState.IN_MEDIATION, CaseState.JUDICIAL_REFERRAL);
    public static final String TASK_ID = "GenerateDefendantDashboardNotificationCaseProceedOffline";

    public CaseProceedOfflineDefendantNotificationHandler(DashboardApiClient dashboardApiClient,
                                                          DashboardNotificationsParamsMapper mapper,
                                                          FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
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
        return SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        boolean isLipvLipOrLRvLip = caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne();
        return (caseProceedInCaseManStates.contains(caseData.getPreviousCCDState()) && isLipvLipOrLRvLip);
    }
}
