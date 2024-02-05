package uk.gov.hmcts.reform.dashboard.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repository.TaskListRepository;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TaskListService {

    private final TaskListRepository taskListRepository;

    @Autowired
    public TaskListService(TaskListRepository taskListRepository) {
        this.taskListRepository = taskListRepository;
    }


    public Optional<TaskListEntity> getTaskList(UUID uuid) {
        return taskListRepository.findById(uuid);
    }
}
