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

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

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
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForCaseHandedOffline,
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForCaseHandedOfflineV1
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondent1(callbackParams) ? TASK_ID_RESPONDENT1 : TASK_ID_RESPONDENT2;
    }

    private Boolean isRespondent1(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        return caseEvent.equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForCaseHandedOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            caseData.getRespondentSolicitor1EmailAddress(),
            notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    //Offline notification will point to a new MP template for displaying defendant responses
    private CallbackResponse notifyRespondentSolicitorForCaseHandedOfflineV1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)) {
            String recipient = caseData.getRespondentSolicitor1EmailAddress();
            String templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline();
            sendNotificationToSolicitor(caseData, recipient, templateID, false);
        } else {
            String templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty();
            if (isRespondent1(callbackParams)) {
                String recipient = caseData.getRespondentSolicitor1EmailAddress();
                sendNotificationToSolicitor(caseData, recipient, templateID, true);
            } else {
                String recipient = caseData.getRespondentSolicitor2EmailAddress();
                sendNotificationToSolicitor(caseData, recipient, templateID, true);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void sendNotificationToSolicitor(CaseData caseData, String recipient, String templateID,
                                             boolean loadMultipartyProperties) {
        notificationService.sendMail(
            recipient,
            templateID,
            loadMultipartyProperties ? addPropertiesForMultiparty(caseData) : addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            REASON, caseData.getRespondent1ClaimResponseType().getDisplayedValue()
        );
    }

    @Override
    public Map<String, String> addPropertiesForMultiparty(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            RESPONDENT_ONE_RESPONSE, caseData.getRespondent1ClaimResponseType().getDisplayedValue(),
            RESPONDENT_TWO_RESPONSE, caseData.getRespondent2ClaimResponseType().getDisplayedValue()
        );
    }
}
