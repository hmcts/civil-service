package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadNotificationService;
import uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GA_EVIDENCE_UPLOAD_CHECK;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocUploadNotifyHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private final ObjectMapper objectMapper;
    private final DocUploadNotificationService docUploadNotificationService;
    private static final List<CaseEvent> EVENTS = List.of(
        GA_EVIDENCE_UPLOAD_CHECK
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::notifyDocUpload
        );
    }

    private CallbackResponse notifyDocUpload(CallbackParams callbackParams) {

        GeneralApplicationCaseData caseData = (GeneralApplicationCaseData) callbackParams.getBaseCaseData();

        try {
            docUploadNotificationService.notifyApplicantEvidenceUpload(caseData);
        } catch (Exception e) {
            log.warn("Failed to send email notification to applicant for case '{}', {}",
                    caseData.getCcdCaseReference().toString(), e.getMessage());
        }
        if (JudicialDecisionNotificationUtil.isWithNotice(caseData)) {
            try {
                docUploadNotificationService.notifyRespondentEvidenceUpload(caseData);
            } catch (Exception e) {
                log.warn("Failed to send email notification to respondent solicitor for case '{}', {}",
                        caseData.getCcdCaseReference().toString(), e.getMessage());
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
