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
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION_DEADLINE_CHECK;

@Service
@RequiredArgsConstructor
public class RequestForReconsiderationNotificationDeadlineCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(REQUEST_FOR_RECONSIDERATION_DEADLINE_CHECK);
    private final ObjectMapper objectMapper;
    private final DashboardApiClient dashboardApiClient;

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
        dashboardApiClient.deleteNotificationByReferenceAndNameAndRole(
            "Notice.AAA6.CP.SDOMadebyLA.Claimant",
            callbackParams.getRequest().getCaseDetails().getId().toString(),
            "CLAIMANT", authToken, "application/json");
        dashboardApiClient.deleteNotificationByReferenceAndNameAndRole(
            "Notice.AAA6.CP.SDOMadebyLA.Defendant",
            callbackParams.getRequest().getCaseDetails().getId().toString(),
            "DEFENDANT", authToken, "application/json");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
