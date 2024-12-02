package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseEventsDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT;

@Service
public class CourtOfficerOrderDefendantNotificationHandler extends CaseEventsDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_RESPONDENT_SOLICITOR1_FOR_COURT_OFFICER_ORDER);
    private static final List<CaseState> caseProceedInCaseManStates =
        List.of(CaseState.CASE_PROGRESSION,
                CaseState.HEARING_READINESS,
                CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING,
                CaseState.DECISION_OUTCOME);
    public static final String TASK_ID = "GenerateDefendantDashboardNotificationCourtOfficerOrder";

    public CourtOfficerOrderDefendantNotificationHandler(DashboardApiClient dashboardApiClient,
                                                         DashboardNotificationsParamsMapper mapper,
                                                         FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT.getScenario();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        boolean isLipvLipOrLRvLip = caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne();
        return (caseData.getPreviousCCDState() != null && caseProceedInCaseManStates.contains(caseData.getPreviousCCDState()) && isLipvLipOrLRvLip) && featureToggleService.isCaseEventsEnabled();
    }

}
