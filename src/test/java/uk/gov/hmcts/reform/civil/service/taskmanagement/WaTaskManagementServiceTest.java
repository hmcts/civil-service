package uk.gov.hmcts.reform.civil.service.taskmanagement;

import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.WaTaskManagementApiClient;
import uk.gov.hmcts.reform.civil.model.wa.GetTasksResponse;
import uk.gov.hmcts.reform.civil.model.wa.SearchParameter;
import uk.gov.hmcts.reform.civil.model.wa.SearchTaskRequest;
import uk.gov.hmcts.reform.civil.model.wa.Task;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class WaTaskManagementServiceTest {

    static String S2S_TOKEN = "s2s";
    static String USER_TOKEN = "s2s";
    static String CASE_ID = "1111222233334444";

    @Mock
    private WaTaskManagementApiClient taskManagementClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private WaTaskManagementService taskManagementService;

    @Before
    public void setupTests() {
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
    }

    @Nested
    class GetAllTasks {

        @Test
        void getAllTasks() {
            List<Task> expectedTasks = List.of();

            when(taskManagementClient.searchWithCriteria(S2S_TOKEN, USER_TOKEN, any(SearchTaskRequest.class))).thenReturn(
               GetTasksResponse.builder().tasks(expectedTasks).build());

            List<Task> actual = taskManagementService.getAllTasks(CASE_ID, USER_TOKEN);

            assertEquals(expectedTasks, actual);
        }
    }

    @Nested
    class GetTaskToComplete {

        @Test
        void getTaskToComplete() {
        }
    }

    @Nested
    class ClaimTask {

        @Test
        void claimTask() {
        }
    }

    private SearchTaskRequest buildSearchRequest(String caseId) {
        return SearchTaskRequest.builder().build();
    }

}
