package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_ENTER;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BreathingSpaceEnteredLIPNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS =
        List.of(
            CaseEvent.NOTIFY_LIP_APPLICANT1_BREATHING_SPACE_ENTER,
            CaseEvent.NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_ENTER
        );
    public static final String TASK_ID_Applicant1 = "NotifyApplicant1BreathingSpaceLIP";
    public static final String TASK_ID_RESPONDENT = "NotifyRespondentBreathingSpaceLIP";
    private static final String REFERENCE_TEMPLATE = "notify-breathing-space-lip-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final Map<String, Callback> callBackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicant1BreathingSpaceEnteredLip
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callBackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondentNotification(callbackParams) ? TASK_ID_RESPONDENT : TASK_ID_Applicant1;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName()
        );
    }

    private CallbackResponse notifyApplicant1BreathingSpaceEnteredLip(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Map<String, String> templateProperties = addProperties(caseData);

        if (isRespondentNotification(callbackParams)) {
            String recipientEmail = Optional.ofNullable(caseData.getRespondent1())
                .map(Party::getPartyEmail).orElse("");
            if (isNotEmpty(recipientEmail)) {
                notificationService.sendMail(
                    recipientEmail,
                    notificationsProperties.getBreathingSpaceEnterApplicantEmailTemplate(),
                    templateProperties,
                    String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
                );
            }
        } else {
            notificationService.sendMail(
                caseData.getApplicant1Email(),
                notificationsProperties.getBreathingSpaceEnterApplicantEmailTemplate(),
                templateProperties,
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private boolean isRespondentNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_ENTER.name());
    }
}
