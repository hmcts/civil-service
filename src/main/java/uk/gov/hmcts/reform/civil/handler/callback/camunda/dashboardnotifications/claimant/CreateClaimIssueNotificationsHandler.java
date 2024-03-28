package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_APPLICANT1;

@Service
@RequiredArgsConstructor
public class CreateClaimIssueNotificationsHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_APPLICANT1);
    public static final String TASK_ID = "CreateIssueClaimDashboardNotificationsForApplicant1";
    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService toggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return toggleService.isDashboardServiceEnabled()
            ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForClaimSubmission)
            : Map.of(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse configureScenarioForClaimSubmission(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        dashboardApiClient.recordScenario(caseData.getCcdCaseReference().toString(),
                                          DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_AWAIT.getScenario(), authToken,
                                          ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build()
        );
        if (caseData.isHWFTypeClaimIssued() && caseData.claimIssueFullRemissionNotGrantedHWF()) {
            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_PHONE_PAYMENT.getScenario(),
                authToken,
                ScenarioRequestParams.builder()
                    .params(mapper.mapCaseDataToParams(caseData)).build()
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
