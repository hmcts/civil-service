package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT;

@Service
public class CaseProceedOfflineClaimantNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE);
    public static final String TASK_ID = "GenerateClaimantDashboardNotificationCaseProceedOffline";

    public CaseProceedOfflineClaimantNotificationHandler(DashboardApiClient dashboardApiClient,
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
        return SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return((CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT == caseData.getPreviousCCDState()
               || CaseState.AWAITING_APPLICANT_INTENTION == caseData.getPreviousCCDState())
                && caseData.isLipvLipOneVOne());
    }
}
