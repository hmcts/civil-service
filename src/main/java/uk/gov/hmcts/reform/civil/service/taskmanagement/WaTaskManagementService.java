package uk.gov.hmcts.reform.civil.service.taskmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.WaTaskManagementApiClient;
import uk.gov.hmcts.reform.civil.model.wa.GetTasksResponse;
import uk.gov.hmcts.reform.civil.model.wa.RequestContext;
import uk.gov.hmcts.reform.civil.model.wa.SearchOperator;
import uk.gov.hmcts.reform.civil.model.wa.SearchParameterKey;
import uk.gov.hmcts.reform.civil.model.wa.SearchParameterList;
import uk.gov.hmcts.reform.civil.model.wa.SearchTaskRequest;
import uk.gov.hmcts.reform.civil.model.wa.Task;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaTaskManagementService {

    private final AuthTokenGenerator authTokenGenerator;

    private final WaTaskManagementApiClient taskManagementClient;

    public List<Task> getAllTasks(String caseId, String userAuth) {
        SearchTaskRequest request = SearchTaskRequest.builder()
            .searchParameters(List.of(
                SearchParameterList.builder()
                    .key(SearchParameterKey.CASE_ID)
                    .operator(SearchOperator.IN)
                    .values(List.of(caseId.toString()))
                    .build()))
            .build();

        log.info("wa task search request: {}", request);
        GetTasksResponse response = taskManagementClient.searchWithCriteria(
            authTokenGenerator.generate(),
            userAuth,
            request
        );
        log.info("response from wa api: {}", response);
        response.getTasks().forEach(task -> log.info("TASK: {}", task.getTaskTitle()));

        return response.getTasks();
    }

    public Task getTaskToComplete(String caseId, String userAuth, Predicate<Task> filterPredicate) {
        List<Task> availableTasks = getAllTasks(caseId, userAuth);
        if (!availableTasks.isEmpty()) {
            return availableTasks.stream().filter(filterPredicate).findFirst().orElse(null);
        }
        return null;
    }

    public void claimTask(String authorization, String taskId) {
        taskManagementClient.claimTask(authTokenGenerator.generate(), authorization, taskId);
    }
}
