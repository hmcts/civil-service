package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MigrationTaskFactory {

    private final Map<String, MigrationTask> taskNameToMigrationTaskMapping;

    public MigrationTaskFactory(MigrationTask... migrationTasks) {
        this.taskNameToMigrationTaskMapping = Arrays.stream(migrationTasks)
            .collect(Collectors.toMap(MigrationTask::getTaskName, migrationTask -> migrationTask));
    }

    public Optional<MigrationTask> getMigrationTask(String taskName) {
        if (taskName == null || taskName.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(taskNameToMigrationTaskMapping.get(taskName));
    }
}
