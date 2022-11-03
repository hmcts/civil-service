package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR_FOR_EVIDENCE_UPLOAD;

@Service
@RequiredArgsConstructor
public class EvidenceUploadRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_RESPONDENT_SOLICITOR_FOR_EVIDENCE_UPLOAD);

    private static final String REFERENCE_TEMPLATE = "evidence-upload-notification-%s";
    public static final String TASK_ID = "EvidenceUploadNotifyRespondentSolicitors";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentEvidenceUpload
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

    private CallbackResponse notifyRespondentEvidenceUpload(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        notificationService.sendMail(
            caseData.getRespondentSolicitor1EmailAddress(),
            notificationsProperties.getEvidenceUploadTemplate(),
            addProperties(caseData),
            String.format(
                REFERENCE_TEMPLATE,
                caseData.getLegacyCaseReference()
            )
        );
        //send email to respondent2
        if (caseData.getAddRespondent2() != null
            && caseData.getAddRespondent2().equals(YesOrNo.YES)
            && caseData.getRespondentSolicitor2EmailAddress() != null) {

            notificationService.sendMail(
                caseData.getRespondentSolicitor2EmailAddress(),
                notificationsProperties.getEvidenceUploadTemplate(),
                addProperties(caseData),
                String.format(
                    REFERENCE_TEMPLATE,
                    caseData.getLegacyCaseReference()
                )
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
    }
}
