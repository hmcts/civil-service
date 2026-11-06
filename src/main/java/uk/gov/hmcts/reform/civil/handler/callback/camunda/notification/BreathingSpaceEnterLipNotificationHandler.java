package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_APPLICANT_BREATHING_SPACE_ENTER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_ENTER;

@Service
public class BreathingSpaceEnterLipNotificationHandler extends AbstractBreathingSpaceLipNotificationHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_LIP_APPLICANT_BREATHING_SPACE_ENTER,
        NOTIFY_LIP_RESPONDENT1_BREATHING_SPACE_ENTER
    );

    private static final String REFERENCE_TEMPLATE = "breathing-space-enter-lip-notification-%s";
    public static final String TASK_ID_APPLICANT = "BreathingSpaceEnterNotifyLipApplicant";
    public static final String TASK_ID_RESPONDENT = "BreathingSpaceEnterNotifyLipRespondent1";

    public BreathingSpaceEnterLipNotificationHandler(NotificationService notificationService,
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
        return NOTIFY_LIP_APPLICANT_BREATHING_SPACE_ENTER.name()
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
        return notificationsProperties.getNotifyApplicant1EnteredBreathingSpaceLip();
    }

    @Override
    protected String getApplicantWelshTemplateId() {
        return notificationsProperties.getNotifyApplicant1EnteredBreathingSpaceLipWelsh();
    }

    @Override
    protected String getRespondentTemplateId() {
        return notificationsProperties.getNotifyEnteredBreathingSpaceForDefendantLip();
    }

    @Override
    protected String getRespondentWelshTemplateId() {
        return notificationsProperties.getNotifyEnteredBreathingSpaceForDefendantLipWelsh();
    }
}
