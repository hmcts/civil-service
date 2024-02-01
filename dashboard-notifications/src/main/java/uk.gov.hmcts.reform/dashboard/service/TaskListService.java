package uk.gov.hmcts.reform.dashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dashboard.data.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.data.TaskListRepository;

import javax.inject.Inject;
import java.util.List;

@Service
@Slf4j
public class TaskListService {

    private final TaskListRepository taskListRepository;

    @Autowired
    public TaskListService(TaskListRepository taskListRepository) {
        this.taskListRepository = taskListRepository;
    }


    public List<TaskListEntity> getTaskList(Long caseId) {

        return taskListRepository.findTaskListByCaseReference(String.valueOf(caseId));


    }
}
