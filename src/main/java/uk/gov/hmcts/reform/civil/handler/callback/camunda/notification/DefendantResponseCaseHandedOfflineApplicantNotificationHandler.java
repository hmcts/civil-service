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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;

@Service
@RequiredArgsConstructor
public class DefendantResponseCaseHandedOfflineApplicantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE);

    public static final String TASK_ID_APPLICANT1 = "DefendantResponseCaseHandedOfflineNotifyApplicantSolicitor1";
    private static final String REFERENCE_TEMPLATE = "defendant-response-case-handed-offline-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForCaseHandedOffline
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_APPLICANT1;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForCaseHandedOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();

        String templateID = is1v1Or2v1Case(caseData)
            ? notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline()
            //1v2 template expects different data
            : notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty();

        sendNotificationToSolicitor(caseData, recipient, templateID);

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void sendNotificationToSolicitor(CaseData caseData, String recipient, String templateID) {
        notificationService.sendMail(
            recipient,
            templateID,
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return NotificationUtils.caseOfflineNotificationAddProperties(caseData);
    }
}
