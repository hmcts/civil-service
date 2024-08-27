package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskListService {

    /**
     * tasks that do not allow progress.
     */
    private static final Set<String> READ_ONLY_TASK_IDS = Set.of("Application.View", "Claim.Claimant.Info",
                                                                 "Claim.View", "Hearing.Bundle.View",
                                                                 "Hearing.Document.View", "Hearing.View",
                                                                 "Judgment.View", "Order.View",
                                                                 "Response.Defendant.Info", "Response.View",
                                                                 "View.Mediation.Documents"
    );
    private final TaskListRepository taskListRepository;

    @Autowired
    public TaskListService(TaskListRepository taskListRepository) {
        this.taskListRepository = taskListRepository;
    }

    public List<TaskList> getTaskList(String ccdCaseIdentifier, String roleType) {

        List<TaskListEntity> taskListEntityList = taskListRepository.findByReferenceAndTaskItemTemplateRole(
            ccdCaseIdentifier,
            roleType
        );

        return taskListEntityList.stream()
            .sorted(Comparator.comparingInt(t -> t.getTaskItemTemplate().getTaskOrder()))
            .map(TaskList::from)
            .toList();
    }

    public TaskListEntity saveOrUpdate(TaskListEntity taskList) {

        TaskItemTemplateEntity taskItemTemplate = taskList.getTaskItemTemplate();
        Optional<TaskListEntity> existingEntity = taskListRepository
            .findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateTemplateName(
                taskList.getReference(),
                taskItemTemplate.getRole(),
                taskItemTemplate.getTemplateName()
            );

        TaskListEntity beingUpdated = taskList;
        if (existingEntity.isPresent()) {
            beingUpdated = taskList.toBuilder().id(existingEntity.get().getId()).build();
        }

        return taskListRepository.save(beingUpdated);
    }

    public TaskListEntity updateTaskListItem(UUID taskItemIdentifier) {

        Optional<TaskListEntity> existingEntity = taskListRepository.findById(taskItemIdentifier);

        return existingEntity.map(taskListEntity -> {
            TaskListEntity updated = taskListEntity.toBuilder().currentStatus(taskListEntity.getNextStatus()).build();
            return taskListRepository.save(updated);
        }).orElseThrow(() -> new IllegalArgumentException("Invalid task item identifier " + taskItemIdentifier));
    }

    public List<TaskListEntity> blockTaskProgress(String ccdCaseIdentifier, String roleType) {

        List<TaskListEntity> taskListEntityList = taskListRepository
            .findByReferenceAndTaskItemTemplateRoleAndCurrentStatusIn(
                ccdCaseIdentifier,
                roleType,
                Set.of(TaskStatus.IN_PROGRESS.getPlaceValue(), TaskStatus.AVAILABLE.getPlaceValue(),
                       TaskStatus.OPTIONAL.getPlaceValue(), TaskStatus.ACTION_NEEDED.getPlaceValue()
                )
            );

        return taskListEntityList.stream().filter(t -> !READ_ONLY_TASK_IDS
                .contains(t.getTaskItemTemplate().getTemplateName()))
            .map(t -> {
                TaskListEntity updated = t.toBuilder().currentStatus(TaskStatus.INACTIVE.getPlaceValue())
                    .nextStatus(TaskStatus.INACTIVE.getPlaceValue())
                    .hintTextCy("").hintTextEn("")
                    .build();
                return taskListRepository.save(updated);
            })
            .collect(Collectors.toList());
    }
}
