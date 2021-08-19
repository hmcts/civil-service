package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class CreateClaimRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE
    );

    public static final String TASK_ID = "NotifyDefendantSolicitor1";
    public static final String TASK_ID_CC = "NotifyApplicantSolicitor1CC";
    public static final String TASK_ID_EMAIL_FIRST_SOL = "NotifyFirstDefendantSolicitor";
    public static final String TASK_ID_EMAIL_SECOND_SOL = "NotifySecondDefendantSolicitor";
    private static final String REFERENCE_TEMPLATE = "create-claim-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForClaimIssue
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) throws Exception {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC:
                return TASK_ID_CC;
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE:
                return TASK_ID_EMAIL_FIRST_SOL;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE:
                return TASK_ID_EMAIL_SECOND_SOL;
            default:
                throw new Exception();
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForClaimIssue(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        /*
            TODO: Implement following logic for determining recipient(s)
                - if null / no value present - (Case is not multiparty + 1v1) send to sol 1
                - if value is only sol 2 - send to sol 2
                - if value is sol 1 - send to sol 1
                - if value is both - send to so 1 then sol 2
         */

        var recipient = isCcNotification(callbackParams)
            ? caseData.getApplicantSolicitor1UserDetails().getEmail()
            : caseData.getRespondentSolicitor1EmailAddress();

        notificationService.sendMail(
            recipient,
            notificationsProperties.getRespondentSolicitorClaimIssueEmailTemplate(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIM_NOTIFICATION_DEADLINE, formatLocalDate(caseData.getClaimNotificationDeadline().toLocalDate(), DATE)
        );
    }

    private boolean isCcNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC.name());
    }

    private boolean isEmailingBothRespondentSolicitors(CaseData caseData){
        return caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel().equals("Both");
    }
}
