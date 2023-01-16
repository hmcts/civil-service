package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY;

@Service
@RequiredArgsConstructor
public class TrialReadyNotifyOthersHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY
    );

    public static final String TASK_ID_APPLICANT = "OtherTrialReadyNotifyApplicantSolicitor1";
    public static final String TASK_ID_RESPONDENT_ONE = "OtherTrialReadyNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT_TWO = "OtherTrialReadyNotifyRespondentSolicitor2";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return null;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return null;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return null;
    }
}
