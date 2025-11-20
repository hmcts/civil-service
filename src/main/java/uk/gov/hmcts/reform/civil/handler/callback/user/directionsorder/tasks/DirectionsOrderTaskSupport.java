package uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

public final class DirectionsOrderTaskSupport {

    private DirectionsOrderTaskSupport() {
    }

    public static boolean supportsEvent(DirectionsOrderTaskContext context, CaseEvent event) {
        if (context == null) {
            return true;
        }
        CallbackParams params = context.callbackParams();
        if (params == null || params.getRequest() == null || params.getRequest().getEventId() == null) {
            return true;
        }
        return event.name().equals(params.getRequest().getEventId());
    }
}
