package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION_DEADLINE_CHECK;

@Service
@RequiredArgsConstructor
public class RequestForReconsiderationNotificationDeadlineCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(REQUEST_FOR_RECONSIDERATION_DEADLINE_CHECK);
    protected final DashboardNotificationsParamsMapper mapper;
    private final ObjectMapper objectMapper;
    private final DashboardScenariosService dashboardScenariosService;

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
        CaseData caseData = callbackParams.getCaseData().toBuilder()
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.requestForReconsiderationDeadlineChecked(YesOrNo.YES).build();
        HashMap<String, Object> paramsMap = mapper.mapCaseDataToParams(caseData);

        dashboardScenariosService.recordScenarios(
            authToken,
            DashboardScenarios.SCENARIO_AAA6_CP_SDO_MADE_BY_LA_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(paramsMap).build()
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
