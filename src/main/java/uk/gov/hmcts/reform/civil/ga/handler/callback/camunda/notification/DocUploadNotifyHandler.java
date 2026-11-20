package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaNotificationHistoryService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GA_EVIDENCE_UPLOAD_CHECK;

@Service
@RequiredArgsConstructor
public class DocUploadNotifyHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private final ObjectMapper objectMapper;
    private final DocUploadNotificationService docUploadNotificationService;
    private final GaNotificationHistoryService notificationHistoryService;
    private static final List<CaseEvent> EVENTS = List.of(
        GA_EVIDENCE_UPLOAD_CHECK
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::notifyDocUpload,
                callbackKey(SUBMITTED), this::recordNotifications
        );
    }

    private CallbackResponse notifyDocUpload(CallbackParams callbackParams) {

        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();

        caseData.setNotificationSummary(docUploadNotificationService.notifyEvidenceUploadAndGetSummary(caseData));
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    private CallbackResponse recordNotifications(CallbackParams callbackParams) {
        var caseData = callbackParams.getGeneralApplicationCaseData();
        if (caseData.getNotificationSummary() != null) {
            notificationHistoryService.recordNotifications(caseData.getCcdCaseReference().toString(), caseData.getNotificationSummary());
        }
        return SubmittedCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
