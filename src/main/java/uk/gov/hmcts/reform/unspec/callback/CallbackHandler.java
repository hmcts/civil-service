package uk.gov.hmcts.reform.unspec.callback;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

public abstract class CallbackHandler {

    private static final String DEFAULT = "default";

    protected abstract Map<String, Callback> callbacks();

    public abstract List<CaseEvent> handledEvents();

    protected String callbackKey(CallbackType type) {
        return type.getValue();
    }

    protected String callbackKey(CallbackType type, String pageId) {
        return pageId == null ? type.getValue() : type.getValue() + "-" + pageId;
    }

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
        String callbackKey = callbackKey(callbackParams.getType(), callbackParams.getPageId());
        return ofNullable(callbacks().get(callbackKey))
            .map(callback -> callback.execute(callbackParams))
            .orElseThrow(() -> new CallbackException(
                String.format(
                    "Callback for event %s, type %s and page id %s not implemented",
                    callbackParams.getRequest().getEventId(),
                    callbackParams.getType(), callbackParams.getPageId()
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
