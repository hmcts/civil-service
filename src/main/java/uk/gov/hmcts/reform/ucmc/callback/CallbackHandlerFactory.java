package uk.gov.hmcts.reform.ucmc.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.HashMap;
import java.util.List;

import static java.util.Optional.ofNullable;

@Service
public class CallbackHandlerFactory {

    private final HashMap<String, CallbackHandler> eventHandlers = new HashMap<>();

    @Autowired(required = false)
    public CallbackHandlerFactory(List<CallbackHandler> beans) {
        beans.forEach(bean -> bean.register(eventHandlers));
    }

    public CallbackResponse dispatch(CallbackParams callbackParams) {
        String eventId = callbackParams.getRequest().getEventId();
        return ofNullable(eventHandlers.get(eventId))
            .map(h -> h.handle(callbackParams))
            .orElseThrow(() -> new CallbackException("Could not handle callback for event " + eventId));
    }
}
