package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateApplicationRespondedDashboardNotificationHandler extends CallbackHandler {

    private final DocUploadDashboardNotificationService dashboardNotificationService;
    private final GaForLipService gaForLipService;

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.CREATE_APPLICATION_RESPONDED_DASHBOARD_NOTIFICATION
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureDashboardScenario);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getParentClaimantIsApplicant().equals(YesOrNo.NO) && caseData.getGeneralAppType().getTypes().contains(
            GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)) {
            if (gaForLipService.isLipApp(caseData)) {
                dashboardNotificationService.createOfflineResponseDashboardNotification(caseData, "APPLICANT", authToken);
            }
            if (gaForLipService.isLipResp(caseData)) {
                dashboardNotificationService.createOfflineResponseDashboardNotification(caseData, "RESPONDENT", authToken);
            }
        } else {
            if (gaForLipService.isLipApp(caseData)) {
                dashboardNotificationService.createResponseDashboardNotification(caseData, "APPLICANT", authToken);
            }
            if (gaForLipService.isLipResp(caseData)) {
                dashboardNotificationService.createResponseDashboardNotification(caseData, "RESPONDENT", authToken);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
