package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DASHBOARD_NOTIFICATION_EVENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(DASHBOARD_NOTIFICATION_EVENT);

    private final DashboardNotificationRegistry registry;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::dispatchNotifications);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return callbackParams.getCaseData().getBusinessProcess().getActivityId();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse dispatchNotifications(CallbackParams callbackParams) {
        String activityId = camundaActivityId(callbackParams);
        List<DashboardWorkflowTask> workflows = registry.workflowsFor(activityId);

        if (workflows.isEmpty()) {
            log.warn("No dashboard notification handlers registered for activity {}", activityId);
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        DashboardTaskContext context = DashboardTaskContext.from(callbackParams);
        workflows.forEach(task -> task.execute(context));
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
