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
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@Service
@RequiredArgsConstructor
public class DefendantClaimDetailsNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC
    );

    public static final String TASK_ID_EMAIL_FIRST_SOL = "NotifyClaimDetailsRespondentSolicitor1";
    public static final String TASK_ID_EMAIL_APP_SOL_CC = "NotifyClaimDetailsApplicantSolicitor1CC";
    public static final String TASK_ID_EMAIL_SECOND_SOL = "NotifyClaimDetailsRespondentSolicitor2";

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
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS:
                return TASK_ID_EMAIL_FIRST_SOL;
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC:
                return TASK_ID_EMAIL_APP_SOL_CC;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS:
                return TASK_ID_EMAIL_SECOND_SOL;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForClaimDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        String recipient = getRecipientEmail(caseData, caseEvent);
        String emailTemplate = notificationsProperties.getRespondentSolicitorClaimDetailsEmailTemplateMultiParty();

        notificationService.sendMail(
            recipient,
            emailTemplate,
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private String getRecipientEmail(CaseData caseData, CaseEvent caseEvent) {

        switch (caseEvent) {
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS:
                return shouldEmailRespondent2Solicitor(caseData)
                    ? caseData.getRespondentSolicitor2EmailAddress()
                    : caseData.getRespondentSolicitor1EmailAddress();
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC:
                return caseData.getApplicantSolicitor1UserDetails().getEmail();
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS:
                return caseData.getRespondentSolicitor2EmailAddress();
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    private boolean shouldEmailRespondent2Solicitor(CaseData caseData) {
        return Optional.ofNullable(caseData.getDefendantSolicitorNotifyClaimDetailsOptions())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse("")
            .startsWith("Defendant Two:");
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIM_DETAILS_NOTIFICATION_DEADLINE, formatLocalDate(caseData.getIssueDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }

}
