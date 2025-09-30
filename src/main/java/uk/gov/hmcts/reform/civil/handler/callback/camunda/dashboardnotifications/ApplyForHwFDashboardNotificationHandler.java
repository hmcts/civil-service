package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.utils.HwFFeeTypeService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@Service
@RequiredArgsConstructor
public class ApplyForHwFDashboardNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_HELP_WITH_FEE);
    private final ObjectMapper objectMapper;
    private final DashboardScenariosService dashboardScenariosService;
    protected final DashboardNotificationsParamsMapper mapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::updateHWFDetailsAndSendNotification
        );
    }

    private CallbackResponse updateHWFDetailsAndSendNotification(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData().toBuilder().build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = HwFFeeTypeService.updateHwfDetails(caseData);
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        HashMap<String, Object> paramsMap = mapper.mapCaseDataToParams(caseData);
        dashboardScenariosService.recordScenarios(authToken,
                DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_REQUESTED_APPLICANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(paramsMap).build()

        );
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
