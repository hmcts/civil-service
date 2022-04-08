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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isRespondent1;

@Service
@RequiredArgsConstructor
public class DefendantResponseCaseHandedOfflineRespondentNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE
    );
    public static final String TASK_ID_RESPONDENT1 = "DefendantResponseCaseHandedOfflineNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT2 = "DefendantResponseCaseHandedOfflineNotifyRespondentSolicitor2";
    private static final String REFERENCE_TEMPLATE =
        "defendant-response-case-handed-offline-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForCaseHandedOffline
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondent1(callbackParams, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE) ? TASK_ID_RESPONDENT1
            : TASK_ID_RESPONDENT2;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    //Offline notification will point to a new MP template for displaying defendant responses
    private CallbackResponse notifyRespondentSolicitorForCaseHandedOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient;
        String templateID;

        //Use 1v1 Template
        if (is1v1Or2v1Case(caseData)) {
            recipient = caseData.getRespondentSolicitor1EmailAddress();
            templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline();
        } else {
            //Use Multiparty Template as there are 2 defendant responses
            templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty();
            if (isRespondent1(callbackParams, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE)) {
                recipient = caseData.getRespondentSolicitor1EmailAddress();
            } else {
                recipient = caseData.getRespondentSolicitor2EmailAddress();
            }
        }

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
