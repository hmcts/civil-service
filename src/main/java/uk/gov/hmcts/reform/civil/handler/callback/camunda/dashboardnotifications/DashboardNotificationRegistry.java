package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class DashboardNotificationRegistry {

    private final Map<String, List<DashboardWorkflowTask>> workflowTasks;

    public DashboardNotificationRegistry(List<DashboardTaskContribution> contributions) {
        Map<String, List<DashboardWorkflowTask>> registry = new HashMap<>();
        Optional.ofNullable(contributions).orElse(List.of())
            .forEach(contribution -> registry.merge(
                contribution.taskId(),
                contribution.dashboardTasks(),
                (existing, additional) -> {
                    var merged = new java.util.ArrayList<>(existing);
                    merged.addAll(additional);
                    return merged;
                }
            ));
        this.workflowTasks = Collections.unmodifiableMap(registry);
    }

    public List<DashboardWorkflowTask> workflowsFor(String taskId) {
        return workflowTasks.getOrDefault(taskId, List.of());
    }
}
