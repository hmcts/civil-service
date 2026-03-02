package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT_RESPONSE_DEADLINE_CHECK;

@Service
@RequiredArgsConstructor
public class GaRespondentResponseDeadlineCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(RESPONDENT_RESPONSE_DEADLINE_CHECK);
    protected final GaDashboardNotificationsParamsMapper mapper;
    private final ObjectMapper objectMapper;
    private final DashboardApiClient dashboardApiClient;
    private final DocUploadDashboardNotificationService dashboardNotificationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::deleteNotifications
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse deleteNotifications(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData().copy()
            .build();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        caseDataBuilder.respondentResponseDeadlineChecked(YesOrNo.YES);
        HashMap<String, Object> paramsMap = mapper.mapCaseDataToParams(caseData);
        dashboardApiClient.recordScenario(
            callbackParams.getRequest().getCaseDetails().getId().toString(),
            DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_DELETE_RESPONDENT.getScenario(),
            authToken,
            ScenarioRequestParams.builder().params(paramsMap).build()

        );

        dashboardNotificationService.createResponseDashboardNotification(caseData, "RESPONDENT", authToken);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
