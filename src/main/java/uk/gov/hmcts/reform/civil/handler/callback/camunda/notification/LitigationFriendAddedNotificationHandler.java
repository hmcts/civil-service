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
import uk.gov.hmcts.reform.civil.notification.handlers.AddDefendantLitigationFriendNotifier;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT_FOR_LITIGATION_FRIEND_ADDED;

@Service
@RequiredArgsConstructor
@Slf4j
public class LitigationFriendAddedNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_EVENT_FOR_LITIGATION_FRIEND_ADDED);

    public static final String TASK_ID = "LitigationFriendAddedNotifier";

    private final AddDefendantLitigationFriendNotifier addDefendantLitigationFriendNotifier;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyForLitigationFriendAdded
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyForLitigationFriendAdded(CallbackParams callbackParams) {
        addDefendantLitigationFriendNotifier.notifyParties(callbackParams.getCaseData(), NOTIFY_EVENT_FOR_LITIGATION_FRIEND_ADDED.toString(), TASK_ID);

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
