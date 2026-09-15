package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_LIFTED;

@Service
public class BreathingSpaceLiftedLipNotificationHandler extends AbstractBreathingSpaceLipNotificationHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED,
        NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_LIFTED
    );

    private static final String REFERENCE_TEMPLATE = "breathing-space-lifted-lip-notification-%s";
    public static final String TASK_ID_APPLICANT = "BreathingSpaceLiftedNotifyLipApplicant";
    public static final String TASK_ID_RESPONDENT = "BreathingSpaceLiftedNotifyLipRespondent1";

    public BreathingSpaceLiftedLipNotificationHandler(NotificationService notificationService,
                                                      NotificationsProperties notificationsProperties,
                                                      NotificationsSignatureConfiguration configuration,
                                                      FeatureToggleService featureToggleService) {
        super(notificationService, notificationsProperties, configuration, featureToggleService);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected boolean isApplicantEvent(CallbackParams callbackParams) {
        return NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED.name()
            .equals(callbackParams.getRequest().getEventId());
    }

    @Override
    protected String getApplicantTaskId() {
        return TASK_ID_APPLICANT;
    }

    @Override
    protected String getRespondentTaskId() {
        return TASK_ID_RESPONDENT;
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected String getApplicantTemplateId() {
        return notificationsProperties.getNotifyLiPApplicantBreathingSpaceLifted();
    }

    @Override
    protected String getApplicantWelshTemplateId() {
        return notificationsProperties.getNotifyLiPApplicantBreathingSpaceLiftedWelsh();
    }

    @Override
    protected String getRespondentTemplateId() {
        return notificationsProperties.getNotifyLiPRespondentBreathingSpaceLifted();
    }

    @Override
    protected String getRespondentWelshTemplateId() {
        return notificationsProperties.getNotifyLiPRespondentBreathingSpaceLiftedWelsh();
    }
}
