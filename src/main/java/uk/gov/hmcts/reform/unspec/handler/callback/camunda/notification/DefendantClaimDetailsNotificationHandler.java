package uk.gov.hmcts.reform.unspec.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.service.NotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDate;

@Service
@RequiredArgsConstructor
public class DefendantClaimDetailsNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC);

    public static final String TASK_ID = "NotifyClaimDetailsRespondentSolicitor1";
    public static final String TASK_ID_CC = "NotifyClaimDetailsApplicantSolicitor1CC";

    private static final String REFERENCE_TEMPLATE = "claim-details-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForClaimDetails
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isCcNotification(callbackParams) ? TASK_ID_CC : TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForClaimDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var recipient = isCcNotification(callbackParams)
            ? caseData.getApplicantSolicitor1UserDetails().getEmail()
            : caseData.getRespondentSolicitor1EmailAddress();

        notificationService.sendMail(
            recipient,
            notificationsProperties.getRespondentSolicitorClaimDetailsEmailTemplate(),
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
            CLAIM_DETAILS_NOTIFICATION_DEADLINE, formatLocalDate(caseData.getIssueDate(), DATE),
            FRONTEND_BASE_URL_KEY, FRONTEND_BASE_URL
        );
    }

    private boolean isCcNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC.name());
    }
}
