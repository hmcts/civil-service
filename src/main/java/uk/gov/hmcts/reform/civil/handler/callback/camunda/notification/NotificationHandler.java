package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierFactory;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;

@Service
@RequiredArgsConstructor
public class NotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_EVENT); //Change back to generic NOTIFY_EVENT

    private final NotifierFactory  notifierFactory;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::sendNotifications
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return callbackParams.getCaseData().getBusinessProcess().getActivityId();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse sendNotifications(CallbackParams callbackParams) {
        final String taskId = callbackParams.getCaseData().getBusinessProcess().getActivityId();
        final Notifier notifier = notifierFactory.getNotifier(taskId);
        notifier.notifyParties(callbackParams.getCaseData(), NOTIFY_EVENT.toString(), taskId);
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
