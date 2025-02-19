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
import uk.gov.hmcts.reform.civil.notification.handlers.BreathingSpaceLiftedNotifier;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_BREATHING_SPACE_EVENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class BreathingSpaceLiftedNotificationsHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_BREATHING_SPACE_EVENT);
    public static final String TASK_ID = "BreathingSpaceLiftedNotifier";
    private final BreathingSpaceLiftedNotifier breathingSpaceLiftedNotifier;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyForBreathingSpaceLifted
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    public CallbackResponse notifyForBreathingSpaceLifted(CallbackParams callbackParams) {
        breathingSpaceLiftedNotifier.notifyParties(callbackParams.getCaseData());
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
