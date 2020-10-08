package uk.gov.hmcts.reform.unspec.callback;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
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
            handledEvent -> handlers.put(handledEvent.name(), this));
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

    /**
     * To be used to return empty callback response, will be used in overriding classes.
     * @param callbackParams This parameter is required as this is passed as reference for execute method in CallBack
     * @return empty callback response
     */
    protected CallbackResponse emptyCallbackResponse(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
