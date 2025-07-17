package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TaskListService {

    private final TaskListRepository taskListRepository;
    private final TaskItemTemplateRepository taskItemTemplateRepository;

    @Autowired
    public TaskListService(TaskListRepository taskListRepository, TaskItemTemplateRepository taskItemTemplateRepository) {
        this.taskListRepository = taskListRepository;
        this.taskItemTemplateRepository = taskItemTemplateRepository;
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

    @Transactional
    public TaskListEntity saveOrUpdate(TaskListEntity taskList) {

        TaskItemTemplateEntity taskItemTemplate = taskList.getTaskItemTemplate();
        List<TaskListEntity> entities = taskListRepository
            .findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateTemplateName(
                taskList.getReference(),
                taskItemTemplate.getRole(),
                taskItemTemplate.getTemplateName()
            );

        Optional<TaskListEntity> latestEntity = entities.stream()
            .max(Comparator.comparing(TaskListEntity::getCreatedAt));

        TaskListEntity beingUpdated = taskList;
        if (latestEntity.isPresent()) {
            beingUpdated = taskList.toBuilder().id(latestEntity.get().getId()).build();
            entities.stream()
                .filter(e -> !e.equals(latestEntity.get()))
                .forEach(duplicateEntity -> taskListRepository.deleteById(duplicateEntity.getId()));
        }

        return taskListRepository.save(beingUpdated);
    }

    @Transactional
    public TaskListEntity updateTaskListItem(UUID taskItemIdentifier) {

        Optional<TaskListEntity> existingEntity = taskListRepository.findById(taskItemIdentifier);

        return existingEntity.map(taskListEntity -> {
            TaskListEntity updated = taskListEntity.toBuilder().currentStatus(taskListEntity.getNextStatus()).build();
            return taskListRepository.save(updated);
        }).orElseThrow(() -> new IllegalArgumentException("Invalid task item identifier " + taskItemIdentifier));
    }

    private void makeProgressAbleTasksInactiveForCaseIdentifierAndRole(String caseIdentifier, String role, String excludedCategory, String excludedTemplate) {
        List<TaskListEntity> tasks = new ArrayList<>();
        if (Objects.nonNull(excludedCategory)) {
            List<TaskItemTemplateEntity> categories = taskItemTemplateRepository.findByCategoryEnAndRole(excludedCategory, role);
            if (Objects.nonNull(categories)) {
                List<Long> catIds = categories.stream().map(TaskItemTemplateEntity::getId).toList();
                tasks = taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
                    caseIdentifier, role, List.of(TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                                                  TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()), catIds
                );
            }
        } else if (Objects.nonNull(excludedTemplate)) {
            tasks = taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplateTemplateNameNot(
                caseIdentifier, role, List.of(TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                                              TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
                ), excludedTemplate);
        } else {
            tasks = taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
                caseIdentifier, role, List.of(TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                                              TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
                ));
        }
        tasks.forEach(t -> {
            TaskListEntity task = t.toBuilder()
                .currentStatus(TaskStatus.INACTIVE.getPlaceValue())
                .nextStatus(TaskStatus.INACTIVE.getPlaceValue())
                .hintTextCy("")
                .hintTextEn("")
                .taskNameEn(StringUtility.removeAnchor(t.getTaskNameEn()))
                .taskNameCy(StringUtility.removeAnchor(t.getTaskNameCy()))
                .build();
            taskListRepository.save(task);
        });
        log.info("{} tasks made inactive for claim = {}", tasks.size(), caseIdentifier);
    }

    @Transactional
    public void makeProgressAbleTasksInactiveForCaseIdentifierAndRole(String caseIdentifier, String role) {
        makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseIdentifier, role, null, null);
    }

    @Transactional
    public void makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(String caseIdentifier, String role, String excludedCategory) {
        makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseIdentifier, role, excludedCategory, null);
    }

    @Transactional
    public void makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(String caseIdentifier, String role, String excludedTemplate) {
        makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseIdentifier, role, null, excludedTemplate);
    }
}
