package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardNotificationDispatcher {

    private final DashboardNotificationRegistry registry;

    public void dispatch(String activityId, DashboardTaskContext context) {
        List<DashboardWorkflowTask> workflows = registry.workflowsFor(activityId, context.caseType());

        if (workflows.isEmpty()) {
            log.warn("No dashboard notification handlers registered for activity {}", activityId);
            return;
        }

        workflows.forEach(task -> task.execute(context));
    }
}
