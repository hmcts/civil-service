package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class CaseEventToCamundaActivityIdMapper {

    private final Map<CaseEvent, String> eventToActivity;

    public CaseEventToCamundaActivityIdMapper(Map<CaseEvent, String> eventToActivity) {
        this.eventToActivity = Collections.unmodifiableMap(eventToActivity);
    }

    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = Optional.ofNullable(callbackParams.getRequest().getEventId())
            .map(CaseEvent::valueOf)
            .orElseThrow(() -> illegalEvent(null));
        String activityId = eventToActivity.get(caseEvent);
        if (StringUtils.isBlank(activityId)) {
            throw illegalEvent(caseEvent);
        } else {
            return activityId;
        }
    }

    private CallbackException illegalEvent(CaseEvent caseEvent) {
        return new CallbackException(
            String.format("Callback handler received illegal event: %s", caseEvent));
    }
}
