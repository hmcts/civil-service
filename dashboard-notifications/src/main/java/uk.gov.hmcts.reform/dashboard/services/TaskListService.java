package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    public TaskListEntity saveOrUpdate(TaskListEntity taskListEntity, String templateName) {
        Optional<TaskListEntity> existingEntity = taskListRepository
            .findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateName(
                taskListEntity.getReference(), taskListEntity.getTaskItemTemplate().getRole(), templateName);

        TaskListEntity beingUpdated = taskListEntity;
        if (existingEntity.isPresent()) {
            beingUpdated = taskListEntity.toBuilder().id(existingEntity.get().getId()).build();
        }

        return taskListRepository.save(beingUpdated);
    }
    public TaskListEntity updateTaskList(UUID id , String reference, String role ) {
        Optional<TaskListEntity> existingEntity = taskListRepository
            .findByTaskItemTemplateIdAndTaskItemTemplateRole(
                id,role);

        TaskListEntity beingUpdated = new TaskListEntity();
        if (existingEntity.isPresent()) {
            beingUpdated.toBuilder().id(existingEntity.get().getId()).reference(reference).currentStatus(existingEntity.get().getNextStatus()).build();
        }

        return taskListRepository.save(beingUpdated);
    }
}
