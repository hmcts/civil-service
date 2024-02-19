package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskListService {

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
            .map(TaskList::from)
            .collect(Collectors.toList());
    }

    public TaskListEntity saveOrUpdate(TaskListEntity taskList) {
        Optional<TaskListEntity> existingEntity = taskListRepository
            .findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateName(
                taskList.getReference(),
                taskList.getTaskItemTemplate().getRole(),
                taskList.getTaskItemTemplate().getName()
            );

        TaskListEntity beingUpdated = taskList;
        if (existingEntity.isPresent()) {
            beingUpdated = taskList.toBuilder().id(existingEntity.get().getId()).build();
        }

        return taskListRepository.save(beingUpdated);
    }

    public TaskListEntity updateTaskList(String reference, String role, String name) {

        Optional<TaskListEntity> existingEntity = taskListRepository
            .findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateName(
                reference, role, name);

        existingEntity.ifPresent(taskListEntity -> {
            taskListEntity.setCurrentStatus(taskListEntity.getNextStatus());
            taskListRepository.save(taskListEntity);
        });

        return existingEntity.isPresent() ? existingEntity.get() : new TaskListEntity();
    }
}
