package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.TrailReadyNotifier;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICANT_NOTIFY_OTHERS_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT1_NOTIFY_OTHERS_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT2_NOTIFY_OTHERS_TRIAL_READY;

@Service
@RequiredArgsConstructor
public class TrialReadyNotificationHandler extends CallbackHandler {

    public static final String TASK_ID_APPLICANT = "ApplicantTrialReadyNotifierOthers";
    public static final String TASK_ID_RESPONDENT_ONE = "RespondentSolicitor1TrialReadyNotifierOthers";
    public static final String TASK_ID_RESPONDENT_TWO = "RespondentSolicitor2TrialReadyNotifierOthers";

    private final TrailReadyNotifier trailReadyNotifier;

    private static final List<CaseEvent> EVENTS = List.of(
        APPLICANT_NOTIFY_OTHERS_TRIAL_READY,
        RESPONDENT1_NOTIFY_OTHERS_TRIAL_READY,
        RESPONDENT2_NOTIFY_OTHERS_TRIAL_READY
    );

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        String eventId = callbackParams.getRequest().getEventId();
        if (eventId.equals(APPLICANT_NOTIFY_OTHERS_TRIAL_READY.name())) {
            return TASK_ID_APPLICANT;
        } else if (eventId.equals(RESPONDENT1_NOTIFY_OTHERS_TRIAL_READY.name())) {
            return TASK_ID_RESPONDENT_ONE;
        } else {
            return TASK_ID_RESPONDENT_TWO;
        }
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyForTrialReady
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyForTrialReady(CallbackParams callbackParams) {
        String eventId = callbackParams.getRequest().getEventId();
        String taskId = callbackParams.getCaseData().getBusinessProcess().getActivityId();
        CaseData caseData = callbackParams.getCaseData();

        trailReadyNotifier.setEventId(eventId);
        trailReadyNotifier.notifyParties(caseData, eventId, taskId);

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
