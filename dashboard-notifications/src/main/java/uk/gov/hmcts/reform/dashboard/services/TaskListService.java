package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;
import uk.gov.hmcts.reform.dashboard.utilities.StringUtility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Service
@Slf4j
public class TaskListService {

    private static final List<Integer> EXCLUDED_TASKS = List.of(
            TaskStatus.AVAILABLE.getPlaceValue(),
            TaskStatus.DONE.getPlaceValue(),
            TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
    );

    private final TaskListRepository taskListRepository;
    private final TaskItemTemplateRepository taskItemTemplateRepository;

    @Autowired
    public TaskListService(TaskListRepository taskListRepository, TaskItemTemplateRepository taskItemTemplateRepository) {
        this.taskListRepository = taskListRepository;
        this.taskItemTemplateRepository = taskItemTemplateRepository;
    }

    public List<TaskList> getTaskList(String ccdCaseIdentifier, String roleType, boolean returnAsNonProgressable) {

        List<TaskListEntity> taskListEntityList = taskListRepository.findByReferenceAndTaskItemTemplateRole(
            ccdCaseIdentifier,
            roleType
        );

        return taskListEntityList.stream()
           .sorted(Comparator.comparingInt(t -> t.getTaskItemTemplate().getTaskOrder()))
           .map(t -> nonNull(returnAsNonProgressable) && returnAsNonProgressable ? createAsNonProgressable(t) : t)
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

    public void makeProgressAbleTasksInactiveForCaseIdentifierAndRole(String caseIdentifier, String role, String excludedCategory) {
        List<TaskListEntity> tasks = new ArrayList<>();
        if (nonNull(excludedCategory)) {
            List<TaskItemTemplateEntity> categories = taskItemTemplateRepository.findByCategoryEnAndRole(excludedCategory, role);
            if (nonNull(categories)) {
                List<Long> catIds = categories.stream().map(TaskItemTemplateEntity::getId).toList();
                tasks = taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
                    caseIdentifier, role, EXCLUDED_TASKS, catIds
                );
            }
        } else {
            tasks = taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
                caseIdentifier, role, EXCLUDED_TASKS);
        }
        tasks.forEach(t -> {
            TaskListEntity task = buildNonProgressableTask(t);
            taskListRepository.save(task);
        });
        log.info("{} tasks made inactive for claim = {}", tasks.size(), caseIdentifier);
    }

    TaskListEntity createAsNonProgressable(TaskListEntity task) {
        return !EXCLUDED_TASKS.contains(task) ? buildNonProgressableTask(task) : task;
    }

    TaskListEntity buildNonProgressableTask(TaskListEntity task) {
        return task.toBuilder()
                .currentStatus(TaskStatus.INACTIVE.getPlaceValue())
                .nextStatus(TaskStatus.INACTIVE.getPlaceValue())
                .hintTextCy("")
                .hintTextEn("")
                .taskNameEn(StringUtility.removeAnchor(task.getTaskNameEn()))
                .taskNameCy(StringUtility.removeAnchor(task.getTaskNameCy()))
                .build();
    }
}
