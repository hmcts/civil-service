package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.civil.client.WaTaskManagementApiClient;
import uk.gov.hmcts.reform.civil.model.taskmanagement.GetTasksResponse;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchOperator;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchParameterKey;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchParameterList;
import uk.gov.hmcts.reform.civil.model.taskmanagement.SearchTaskRequest;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "task_management")
@MockServerConfig(hostInterface = "localhost", port = "6667")
@TestPropertySource(properties = "task-management.api.url=http://localhost:6667")
public class TaskManagementApiConsumerTest extends BaseContractTest {

    public static final String TASK_ID = "task-id";
    public static final String TASK_TITLE = "task-title";
    public static final String ENDPOINT = "/task";
    public static final String CLAIM_ENDPOINT = ENDPOINT + "/" + TASK_ID + "/claim";

    @Autowired
    private WaTaskManagementApiClient taskManagementClient;

    @Pact(consumer = "civil_service")
    public RequestResponsePact postSearchTaskServiceRequest(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildSearchTaskResponsePact(builder);
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact postClaimTaskServiceRequest(PactDslWithProvider builder)
        throws JSONException, IOException {
        return buildClaimTaskResponsePact(builder);
    }

    @Test
    @PactTestFor(pactMethod = "postSearchTaskServiceRequest")
    public void verifySearchTaskSearch() {
        GetTasksResponse response = taskManagementClient.searchWithCriteria(
            SERVICE_AUTH_TOKEN,
            AUTHORIZATION_TOKEN,
            buildSearchTaskRequest()
        );

        assertThat(response.getTasks().size(), is(equalTo(1)));
        assertThat(response.getTasks().get(0).getId(), is(equalTo(TASK_ID)));
        assertThat(response.getTasks().get(0).getTaskTitle(), is(equalTo(TASK_TITLE)));
    }

    @Test
    @PactTestFor(pactMethod = "postClaimTaskServiceRequest")
    public void verifyClaimTask() {
        taskManagementClient.claimTask(
            SERVICE_AUTH_TOKEN,
            AUTHORIZATION_TOKEN,
            TASK_ID
        );
    }

    private SearchTaskRequest buildSearchTaskRequest() {
        return SearchTaskRequest.builder()
            .searchParameters(List.of(
                SearchParameterList.builder()
                    .key(SearchParameterKey.CASE_ID)
                    .operator(SearchOperator.IN)
                    .values(List.of("1111222233334444"))
                    .build()))
            .sortingParameters(null)
            .requestContext(null)
            .build();
    }

    private RequestResponsePact buildSearchTaskResponsePact(PactDslWithProvider builder) throws IOException {
        return builder
            .uponReceiving("a new task search request")
            .path(ENDPOINT)
            .method(HttpMethod.POST.toString())
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .body(createJsonObject(buildSearchTaskRequest()))
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(buildSearchTasksResponse())
            .toPact();
    }

    private RequestResponsePact buildClaimTaskResponsePact(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a new claim task request")
            .path(CLAIM_ENDPOINT)
            .method(HttpMethod.POST.toString())
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private DslPart buildSearchTasksResponse() {
        return LambdaDsl.newJsonBody((root) -> {
            root
                .array("tasks", tasksArray -> {
                    tasksArray.object(taskObject -> {
                        taskObject.stringType("id", TASK_ID);
                        taskObject.stringType("task_title", TASK_TITLE);
                    });
                });
        }).build();
    }

}
