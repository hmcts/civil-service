package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class DefendantResponseApplicantNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE,
        NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC
    );

    public static final String TASK_ID = "DefendantResponseFullDefenceNotifyApplicantSolicitor1";
    public static final String TASK_ID_CC = "DefendantResponseFullDefenceNotifyRespondentSolicitor1CC";
    public static final String TASK_ID_CC_RESP2 = "DefendantResponseFullDefenceNotifyRespondentSolicitor2CC";
    private static final String REFERENCE_TEMPLATE = "defendant-response-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForDefendantResponse,
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::notifySolicitorsForDefendantResponse
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE:
                return TASK_ID;
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC:
                return TASK_ID_CC;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC:
                return TASK_ID_CC_RESP2;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForDefendantResponse(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var recipient = isCcNotification(callbackParams)
            ? caseData.getRespondentSolicitor1EmailAddress()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();

        notificationService.sendMail(
            recipient,
            notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private CallbackResponse notifySolicitorsForDefendantResponse(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        //determine recipient for notification, based on Camunda Event ID
        String recipient;
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE:
                recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
                sendNotificationToSolicitor(caseData, recipient, true);
                break;
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC:
                recipient = caseData.getRespondentSolicitor1EmailAddress();
                sendNotificationToSolicitor(caseData, recipient, true);
                break;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC:
                recipient = caseData.getRespondentSolicitor2EmailAddress();
                sendNotificationToSolicitor(caseData, recipient, false);
                break;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    //need to pass in template ID based on Defendant Response data for new MP template
    private void sendNotificationToSolicitor(CaseData caseData, String recipient, boolean addPropertiesRespondentOne) {
        notificationService.sendMail(
            recipient,
            notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence(),
            addPropertiesRespondentOne ? addProperties(caseData) : addPropertiesForMultiparty(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    @Override
    public Map<String, String> addPropertiesForMultiparty(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent2())
        );
    }

    //used by existing production callback - non MP
    private boolean isCcNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC.name());
    }
}
