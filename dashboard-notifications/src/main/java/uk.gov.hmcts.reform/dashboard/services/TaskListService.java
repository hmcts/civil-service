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

        // TODO can we use any external Library like MapStruct for entity to DTO conversion ???
        //ModelMapper modelMapper = new ModelMapper();
        //TypeMap<TaskListEntity, TaskList> propertyMapper = modelMapper.createTypeMap(TaskListEntity.class, TaskList.class);
        //propertyMapper.addMapping(TaskItemTemplateEntity::getCategoryEn, TaskList::getCategoryEn);

        //List<TaskList> taskList = modelMapper.map(taskListEntityList, new TypeToken<List<TaskList>>() {}.getType());

        List<TaskList> taskList = taskListEntityList.stream()
            .map(p -> new TaskList(p.getId(), p.getReference(), p.getCurrentStatus(), p.getNextStatus(),
                                   p.getTaskNameEn(), p.getHintTextEn(), p.getTaskNameCy(), p.getHintTextCy(),
                                   p.getCreatedAt(), p.getUpdatedAt(), p.getUpdatedBy(), p.getMessageParams(),
                                   p.getTaskItemTemplate().getCategoryEn(), p.getTaskItemTemplate().getCategoryCy(),
                                   p.getTaskItemTemplate().getRole(), p.getTaskItemTemplate().getTaskOrder()
            ))
            .collect(Collectors.toList());

        return taskList;
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
}
