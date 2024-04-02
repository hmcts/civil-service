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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT1_HWF_DASHBOARD_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_FULL_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_INFO_REQUIRED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_INVALID_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_NO_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_PART_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_UPDATED;

@Service
@RequiredArgsConstructor
public class HwFDashboardNotificationsHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CLAIMANT1_HWF_DASHBOARD_NOTIFICATION);
    public static final String TASK_ID = "Claimant1HwFDashboardNotification";
    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper mapper;
    private final Map<CaseEvent, String> dashboardScenarios = Map.of(
        NO_REMISSION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_NO_REMISSION.getScenario(),
        INVALID_HWF_REFERENCE, SCENARIO_AAA6_CLAIM_ISSUE_HWF_INVALID_REF.getScenario(),
        MORE_INFORMATION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_INFO_REQUIRED.getScenario(),
        UPDATE_HELP_WITH_FEE_NUMBER, SCENARIO_AAA6_CLAIM_ISSUE_HWF_UPDATED.getScenario(),
        PARTIAL_REMISSION_HWF_GRANTED, SCENARIO_AAA6_CLAIM_ISSUE_HWF_PART_REMISSION.getScenario(),
        FULL_REMISSION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_FULL_REMISSION.getScenario()
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForHwfEvents
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse configureScenarioForHwfEvents(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        if (caseData.isHWFTypeClaimIssued() && caseData.getHwFEvent() != null) {
            dashboardApiClient.recordScenario(caseData.getCcdCaseReference().toString(),
                                              dashboardScenarios.get(caseData.getHwFEvent()), authToken,
                                              ScenarioRequestParams.builder()
                                                  .params(mapper.mapCaseDataToParams(caseData))
                                                  .build()
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
