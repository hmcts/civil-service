package uk.gov.hmcts.reform.civil.service.taskmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.community.rest.exception.RemoteProcessEngineException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.WaTaskManagementApiClient;
import uk.gov.hmcts.reform.civil.exceptions.InternalServerErrorException;
import uk.gov.hmcts.reform.civil.model.taskmanagement.GetTasksResponse;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchOperator;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchParameterKey;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchParameterList;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchTaskRequest;
import uk.gov.hmcts.reform.civil.model.taskmanagement.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaTaskManagementService {

    private final AuthTokenGenerator authTokenGenerator;

    private final WaTaskManagementApiClient taskManagementClient;

    @Value("${task-management.enabled:true}")
    private boolean taskManagementEnabled;

    public List<Task> getAllTasks(String caseId, String userAuth) {
        if (!taskManagementEnabled) {
            log.info("Get all tasks - WA integration is disabled returning empty list");
            return new ArrayList<>();
        }

        SearchTaskRequest request = SearchTaskRequest.builder()
            .searchParameters(List.of(
                SearchParameterList.builder()
                    .key(SearchParameterKey.CASE_ID)
                    .operator(SearchOperator.IN)
                    .values(List.of(caseId))
                    .build()))
            .build();

        log.info("wa task search request: {}", request);

        GetTasksResponse response;
        try {
            response = taskManagementClient.searchWithCriteria(
                authTokenGenerator.generate(),
                userAuth,
                request
            );
        } catch (RemoteProcessEngineException e) {
            log.error("There was an issue retrieving tasks from task management api: {}", e.getMessage());
            throw new InternalServerErrorException(e.getMessage());
        }

        return response.getTasks();
    }

    public Task getTaskToComplete(String caseId, String userAuth, Predicate<Task> filterPredicate) {
        if (!taskManagementEnabled) {
            log.info("Get task to complete - WA integration is disabled returning null");
            return null;
        }

        List<Task> availableTasks = getAllTasks(caseId, userAuth);

        if (!availableTasks.isEmpty()) {
            return availableTasks.stream().filter(filterPredicate).findFirst().orElse(null);
        }
        return null;
    }

    public void claimTask(String authorization, String taskId) {
        if (taskManagementEnabled) {
            try {
                taskManagementClient.claimTask(authTokenGenerator.generate(), authorization, taskId);
            } catch (RemoteProcessEngineException e) {
                log.error("There was an issue claiming the task [{}] from task management api: {}", taskId, e.getMessage());
                throw new InternalServerErrorException(e.getMessage());
            }

        } else {
            log.info("Claim task - WA integration is disabled");
        }
    }
}
