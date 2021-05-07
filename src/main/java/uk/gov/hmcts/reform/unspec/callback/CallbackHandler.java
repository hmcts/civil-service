package uk.gov.hmcts.reform.unspec.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

public abstract class CallbackHandler {

    private static final String DEFAULT = "default";
    private static final Logger LOG = LoggerFactory.getLogger(CallbackHandler.class);

    protected abstract Map<String, Callback> callbacks();

    public abstract List<CaseEvent> handledEvents();

    protected String callbackKey(CallbackType type) {
        return type.getValue();
    }

    protected String callbackKey(CallbackType type, String pageId) {
        return callbackKey(null, type, pageId);
    }

    protected String callbackKey(CallbackVersion version, CallbackType type) {
        return callbackKey(version, type, null);
    }

    protected String callbackKey(CallbackVersion version, CallbackType type, String pageId) {
        String formattedVersion = ofNullable(version).map(v -> v.toString() + "-").orElse("");
        String formattedPageId = ofNullable(pageId).map(id -> "-" + id).orElse("");
        return String.format("%s%s%s", formattedVersion, type.getValue(), formattedPageId);
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
        String callbackKey;

        callbackKey = callbackKey(callbackParams.getVersion(), callbackParams.getType(), callbackParams.getPageId());

        if (ofNullable(callbacks().get(callbackKey)).isEmpty()) {
            LOG.info(String.format("No implementation found for %s, falling back to default", callbackKey));
            callbackKey = callbackKey(callbackParams.getType(), callbackParams.getPageId());
        }

        return ofNullable(callbacks().get(callbackKey))
            .map(callback -> callback.execute(callbackParams))
            .orElseThrow(() -> new CallbackException(
                String.format(
                    "Callback for event %s, version %s, type %s and page id %s not implemented",
                    callbackParams.getRequest().getEventId(),
                    callbackParams.getVersion(),
                    callbackParams.getType(),
                    callbackParams.getPageId()
                )));
    }

    /**
     * To be used to return empty callback response, will be used in overriding classes.
     *
     * @param callbackParams This parameter is required as this is passed as reference for execute method in CallBack
     * @return empty callback response
     */
    protected CallbackResponse emptyCallbackResponse(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    /**
     * Returns empty submitted callback response. Used by events that set business process to ready, but doesn't have
     * any submitted callback logic (making callback is still required to trigger EventEmitterAspect)
     *
     * @param callbackParams This parameter is required as this is passed as reference for execute method in CallBack
     * @return empty submitted callback response
     */
    protected CallbackResponse emptySubmittedCallbackResponse(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder().build();
    }
}
