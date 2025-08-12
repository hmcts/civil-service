package uk.gov.hmcts.reform.civil.service.taskmanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.WaTaskManagementApiClient;
import uk.gov.hmcts.reform.civil.model.taskmanagement.GetTasksResponse;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchOperator;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchParameterKey;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchParameterList;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchTaskRequest;
import uk.gov.hmcts.reform.civil.model.taskmanagement.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class WaTaskManagementServiceTest {

    static String S2S_TOKEN = "s2s";
    static String USER_TOKEN = "user-token";
    static String CASE_ID = "1111222233334444";

    @Mock
    private WaTaskManagementApiClient taskManagementClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private WaTaskManagementService taskManagementService;

    @BeforeEach
    public void setupTests() {
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
    }

    @Nested
    class WhenTAskManagementIsEnabled {

        @BeforeEach
        void setupDisabledTests() {
            ReflectionTestUtils.setField(taskManagementService, "taskManagementEnabled", true);
        }

        @Test
        void getAllTasks_shouldCallTaskManagementClientWithExpectedRequest_andReturnExpectedTasks() {
            List<Task> expectedTasks = List.of();

            when(taskManagementClient.searchWithCriteria(
                S2S_TOKEN, USER_TOKEN, buildSearchRequest(CASE_ID)))
                .thenReturn(GetTasksResponse.builder().tasks(expectedTasks).build());

            List<Task> actual = taskManagementService.getAllTasks(CASE_ID, USER_TOKEN);

            assertEquals(expectedTasks, actual);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void getTaskToComplete_shouldReturnExpectedTask(boolean makeMatch) {
            Task expectedTask = Task.builder().name("TestTask").build();
            Predicate<Task> testPredicate = task -> makeMatch;
            when(taskManagementClient.searchWithCriteria(
                S2S_TOKEN, USER_TOKEN, buildSearchRequest(CASE_ID)))
                .thenReturn(GetTasksResponse.builder().tasks(List.of(expectedTask)).build());

            Task actual = taskManagementService.getTaskToComplete(CASE_ID, USER_TOKEN, testPredicate);

            if (makeMatch) {
                assertEquals(expectedTask, actual);
            } else {
                assertNull(actual);
            }
        }

        @Test
        void getTaskToComplete_shouldReturnNull_whenNoTasks() {
            Predicate<Task> testPredicate = task -> true;
            when(taskManagementClient.searchWithCriteria(
                S2S_TOKEN, USER_TOKEN, buildSearchRequest(CASE_ID)))
                .thenReturn(GetTasksResponse.builder().tasks(List.of()).build());

            Task actual = taskManagementService.getTaskToComplete(CASE_ID, USER_TOKEN, testPredicate);

            assertNull(actual);
        }

        @Test
        void claimTask_shouldCallTaskClient_withExpectedTaskId() {
            String taskId = "TaskId";
            taskManagementService.claimTask(USER_TOKEN, taskId);

            verify(taskManagementClient).claimTask(S2S_TOKEN, USER_TOKEN, taskId);
        }
    }

    @Nested
    class WhenTaskManagementIsDisabled {

        @BeforeEach
        void setupDisabledTests() {
            ReflectionTestUtils.setField(taskManagementService, "taskManagementEnabled", false);
        }

        @Test
        void getAllTasks_shouldReturnEmptyList_whenDisabled() {
            List<Task> actual = taskManagementService.getAllTasks(CASE_ID, USER_TOKEN);

            assertEquals(new ArrayList<>(), actual);
            verify(taskManagementClient, never()).searchWithCriteria(S2S_TOKEN, USER_TOKEN, buildSearchRequest(CASE_ID));
        }

        @Test
        void getTaskToComplete_shouldReturnNull_whenDisabled() {
            Predicate<Task> testPredicate = task -> true;
            Task actual = taskManagementService.getTaskToComplete(CASE_ID, USER_TOKEN, testPredicate);

            assertNull(actual);
            verify(taskManagementClient, never()).searchWithCriteria(S2S_TOKEN, USER_TOKEN, buildSearchRequest(CASE_ID));
        }

        @Test
        void claimTask_shouldNotCallClient_whenDisabled() {
            String taskId = "TaskId";
            taskManagementService.claimTask(USER_TOKEN, taskId);

            verify(taskManagementClient, never()).claimTask(S2S_TOKEN, USER_TOKEN, taskId);
        }
    }

    private SearchTaskRequest buildSearchRequest(String caseId) {
        return SearchTaskRequest.builder()
            .searchParameters(List.of(
                SearchParameterList.builder()
                    .key(SearchParameterKey.CASE_ID)
                    .operator(SearchOperator.IN)
                    .values(List.of(caseId))
                    .build()))
            .sortingParameters(null)
            .requestContext(null)
            .build();
    }
}
