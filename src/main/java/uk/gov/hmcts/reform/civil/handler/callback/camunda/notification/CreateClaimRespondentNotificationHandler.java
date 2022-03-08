package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class CreateClaimRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE
    );

    public static final String TASK_ID_EMAIL_FIRST_SOL = "NotifyDefendantSolicitor1";
    public static final String TASK_ID_EMAIL_APP_SOL_CC = "NotifyApplicantSolicitor1CC";
    public static final String TASK_ID_EMAIL_SECOND_SOL = "NotifyDefendantSolicitor2";
    private static final String REFERENCE_TEMPLATE = "create-claim-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyARespondentSolicitorForClaimIssue
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE:
                return TASK_ID_EMAIL_FIRST_SOL;
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC:
                return TASK_ID_EMAIL_APP_SOL_CC;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE:
                return TASK_ID_EMAIL_SECOND_SOL;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    //    Notify Claim Multiparty:
    //
    //            Scenario         Case Progresses       Callback Notify First Sol     Callback Notify Second Sol
    //            1v1                  Online                    Solicitor 1                      -
    //
    //            1v2                  Online                    Solicitor 1                      -
    //            (Same Sol)
    //
    //            1v2                  Online                    Solicitor 1                 Solicitor 2
    //            (Different Sol
    //            - Non Divergent)
    //
    //            1v2                  Offline             Solicitor 1 || Solicitor 2             -
    //            (Different Sol
    //            - Divergent)
    private CallbackResponse notifyARespondentSolicitorForClaimIssue(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient;

        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE:
                recipient = caseData.getRespondentSolicitor1EmailAddress();
                if (shouldEmailRespondent2Solicitor(caseData)) {
                    recipient = caseData.getRespondentSolicitor2EmailAddress();
                }
                break;
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC:
                recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
                break;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE:
                recipient = caseData.getRespondentSolicitor2EmailAddress();
                break;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }

        sendNotificationToSolicitor(caseData, recipient);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void sendNotificationToSolicitor(CaseData caseData, String recipient) {
        notificationService.sendMail(
            recipient,
            notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    private boolean shouldEmailRespondent2Solicitor(CaseData caseData) {
        return caseData.getDefendantSolicitorNotifyClaimOptions() != null
            && caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel()
            .startsWith("Defendant Two:");
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIM_NOTIFICATION_DEADLINE,
            formatLocalDate(caseData.getClaimNotificationDeadline().toLocalDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }
}
