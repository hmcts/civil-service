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
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@Service
@RequiredArgsConstructor
public class BreathingSpaceLiftedLIPNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS =
        List.of(
            CaseEvent. NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED,
            CaseEvent.NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_LIFTED
        );
    public static final String TASK_ID_APPLICANT = "NotifyApplicantBreathingSpaceLifted";
    public static final String TASK_ID_RESPONDENT = "NotifyRespondentBreathingSpaceLifted";
    private static final String REFERENCE_TEMPLATE = "notify-breathing-space-lifted-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final Map<String, Callback> callBackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyBreathingSpaceLifted
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callBackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isApplicantNotification(callbackParams) ? TASK_ID_APPLICANT : TASK_ID_RESPONDENT;
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

    private CallbackResponse notifyBreathingSpaceLifted(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Map<String, String> templateProperties = addProperties(caseData);
        final String applicantEmail = caseData.getApplicant1Email();

        if (isApplicantNotification(callbackParams) && Objects.nonNull(applicantEmail)) {
            notificationService.sendMail(
                applicantEmail,
                notificationsProperties.getNotifyLiPApplicantBreathingSpaceLifted(),
                templateProperties,
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        } else if (Objects.nonNull(caseData.getRespondent1().getPartyEmail())) {
            notificationService.sendMail(
                caseData.getRespondent1().getPartyEmail(),
                notificationsProperties.getNotifyLiPRespondentBreathingSpaceLifted(),
                templateProperties,
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private boolean isApplicantNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(CaseEvent.NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED.name());
    }
}
