package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;

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

    @SuppressWarnings("unchecked")
    public <T extends CaseReference> Optional<MigrationTask<T>> getMigrationTask(String taskName) {
        if (taskName == null || taskName.isEmpty()) {
            return Optional.empty();
        }
        MigrationTask<?> task = taskNameToMigrationTaskMapping.get(taskName);
        if (task == null) {
            return Optional.empty();
        }
        return Optional.of((MigrationTask<T>) task); // unsafe cast, but controlled
    }
}
