package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class DashboardNotificationRegistry {

    private final Map<DashboardCaseType, Map<String, List<DashboardWorkflowTask>>> workflowTasks;

    public DashboardNotificationRegistry(List<DashboardTaskContribution> contributions) {
        Map<DashboardCaseType, Map<String, List<DashboardWorkflowTask>>> registry = new EnumMap<>(DashboardCaseType.class);
        Optional.ofNullable(contributions).orElse(List.of())
            .forEach(contribution -> {
                DashboardCaseType caseType = contribution.caseType();
                Map<String, List<DashboardWorkflowTask>> caseRegistry =
                    registry.computeIfAbsent(caseType, key -> new HashMap<>());
                caseRegistry.merge(
                    contribution.taskId(),
                    contribution.dashboardTasks(),
                    (existing, additional) -> {
                        var merged = new java.util.ArrayList<>(existing);
                        merged.addAll(additional);
                        return merged;
                    }
                );
            });
        this.workflowTasks = Collections.unmodifiableMap(registry.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> Collections.unmodifiableMap(entry.getValue())
            )));
    }

    public List<DashboardWorkflowTask> workflowsFor(String taskId, DashboardCaseType caseType) {
        return workflowTasks
            .getOrDefault(caseType, Map.of())
            .getOrDefault(taskId, List.of());
    }
}
