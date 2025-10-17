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
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.GaCallbackDataUtil;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DELETE_APPLICATION_PAYMENT_DASHBOARD_NOTIFICATION;

@Service
@RequiredArgsConstructor
public class DeleteApplicationPaymentDashboardNotificationHandler extends CallbackHandler {

    private static final String APPLICANT_ROLE = "APPLICANT";
    private final DashboardApiClient dashboardApiClient;
    private static final String PAYMENT_NOTIFICATION = "Notice.AAA6.GeneralApps.ApplicationFeeRequired.Applicant";

    private static final List<CaseEvent> EVENTS = List.of(DELETE_APPLICATION_PAYMENT_DASHBOARD_NOTIFICATION);
    private final FeatureToggleService featureToggleService;
    private final GaForLipService gaForLipService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return  featureToggleService.isGaForWelshEnabled()
            ? Map.of(callbackKey(ABOUT_TO_SUBMIT), this::deletePaymentDashboardNotification)
            : Map.of(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse deletePaymentDashboardNotification(CallbackParams callbackParams) {
        CaseData caseData = GaCallbackDataUtil.resolveCaseData(callbackParams, objectMapper);
        var gaCaseData = GaCallbackDataUtil.resolveGaCaseData(callbackParams, objectMapper);
        if (caseData == null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .build();
        }
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        if (gaCaseData != null && gaForLipService.isGaForLip(gaCaseData) && gaForLipService.isLipAppGa(gaCaseData)) {
            dashboardApiClient.deleteTemplateNotificationsForCaseIdentifierAndRole(
                caseData.getCcdCaseReference().toString(),
                APPLICANT_ROLE,
                PAYMENT_NOTIFICATION,
                authToken);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }
}
