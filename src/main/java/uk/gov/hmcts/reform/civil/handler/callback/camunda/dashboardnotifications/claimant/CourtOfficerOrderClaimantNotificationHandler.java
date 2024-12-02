package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

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

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_CLAIMANT;

@Service
public class CourtOfficerOrderClaimantNotificationHandler extends CaseEventsDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANT_SOLICITOR1_FOR_COURT_OFFICER_ORDER);

    private static final List<CaseState> caseMovedInCaseManStates =
        List.of(CaseState.CASE_PROGRESSION,
                CaseState.HEARING_READINESS,
                CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING,
                CaseState.DECISION_OUTCOME);
    public static final String TASK_ID = "GenerateClaimantDashboardNotificationCourtOfficerOrder";

    public CourtOfficerOrderClaimantNotificationHandler(DashboardApiClient dashboardApiClient,
                                                        DashboardNotificationsParamsMapper mapper,
                                                        FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
            return SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_CLAIMANT.getScenario();
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return (caseData.getPreviousCCDState() != null && caseMovedInCaseManStates.contains(caseData.getPreviousCCDState())
                && caseData.isLipvLipOneVOne()) && featureToggleService.isCaseEventsEnabled();
    }

}
