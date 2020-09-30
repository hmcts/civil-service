package uk.gov.hmcts.reform.unspec.callback;

import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

public abstract class CallbackHandler {

    private static final String DEFAULT = "default";

    protected abstract Map<CallbackType, Callback> callbacks();

    public abstract List<CaseEvent> handledEvents();

    public String camundaActivityId() {
        return DEFAULT;
    }

    public boolean isEventAlreadyProcessed(BusinessProcess businessProcess) {
        if (camundaActivityId().equals(DEFAULT)) {

            return false;
        }

        return businessProcess != null && camundaActivityId().equals(businessProcess.getActivityId());
    }

    public void register(Map<String, CallbackHandler> handlers) {
        handledEvents().forEach(
            handledEvent -> handlers.put(handledEvent.getValue(), this));
    }

    public CallbackResponse handle(CallbackParams callbackParams) {
        return ofNullable(callbacks().get(callbackParams.getType()))
            .map(callback -> callback.execute(callbackParams))
            .orElseThrow(() -> new CallbackException(
                String.format(
                    "Callback for event %s, type %s not implemented",
                    callbackParams.getRequest().getEventId(),
                    callbackParams.getType()
                )));
    }
}
