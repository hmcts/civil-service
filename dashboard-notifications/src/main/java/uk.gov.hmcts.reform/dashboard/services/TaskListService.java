package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TaskListService {

    private final TaskListRepository taskListRepository;

    @Autowired
    public TaskListService(TaskListRepository taskListRepository) {
        this.taskListRepository = taskListRepository;
    }

    public Optional<List<TaskListEntity>> getTaskList(String ccdCaseIdentifier, String roleType) {
        return taskListRepository.findByReferenceAndTaskItemTemplateRole(ccdCaseIdentifier, roleType);
    }

    public TaskListEntity saveOrUpdate(TaskListEntity taskListEntity, String templateId) {
        Optional<TaskListEntity> existingEntity = taskListRepository.findByReferenceAndRoleAndTaskItemTemplateAndId(
            taskListEntity.getReference(), taskListEntity.getRole(), templateId);

        TaskListEntity beingUpdated = taskListEntity;
        if (existingEntity.isPresent()) {
            beingUpdated = taskListEntity.toBuilder().id(existingEntity.get().getId()).build();
        }

        return taskListRepository.save(beingUpdated);
    }
}
