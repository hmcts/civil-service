package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.camunda.community.rest.client.model.IncidentDto;
import org.camunda.community.rest.client.model.ProcessInstanceDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeApi;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;

import java.util.List;
import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArray;
import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@PactTestFor(providerName = "camunda_rest_engine")
@MockServerConfig(hostInterface = "localhost", port = "6687")
@TestPropertySource(properties = "feign.client.config.processInstance.url=http://localhost:6687")
public class CamundaRuntimeApiConsumerTest extends BaseContractTest {

    private static final String PROCESS_INSTANCE_ID = "process-instance-id";
    private static final String INCIDENT_ID = "incident-id";
    private static final String JOB_ID = "job-id";

    @Autowired
    private CamundaRuntimeClient camundaRuntimeClient;

    @Autowired
    private CamundaRuntimeApi camundaRuntimeApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getProcessVariables(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a Camunda process variables request")
            .path("/process-instance/" + PROCESS_INSTANCE_ID + "/variables")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildProcessVariablesResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact queryProcessInstances(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a Camunda history process instance query request")
            .path("/history/process-instance")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .matchQuery("maxResults", "\\d+", "50")
            .matchQuery("sortBy", "startTime", "startTime")
            .matchQuery("sortOrder", "desc|asc", "desc")
            .method(HttpMethod.POST.toString())
            .body("""
                {
                  "processDefinitionKey": "civil"
                }
                """)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildProcessInstanceResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact fetchExternalTaskErrorDetails(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a Camunda external task error details request")
            .path("/history/external-task-log/" + INCIDENT_ID + "/error-details")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(newJsonBody(root -> root.stringValue("message", "External task failed")).build())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getLatestIncident(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a Camunda latest open incident request")
            .path("/incident")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .matchQuery("open", "true|false", "true")
            .matchQuery("processInstanceId", PROCESS_INSTANCE_ID, PROCESS_INSTANCE_ID)
            .matchQuery("sortBy", "incidentTimestamp", "incidentTimestamp")
            .matchQuery("sortOrder", "desc|asc", "desc")
            .matchQuery("maxResults", "\\d+", "1")
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildIncidentResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact setJobRetries(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a Camunda set job retries request")
            .path("/job/" + JOB_ID + "/retries")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .method(HttpMethod.PUT.toString())
            .body("""
                {
                  "retries": 1
                }
                """)
            .willRespondWith()
            .status(HttpStatus.SC_NO_CONTENT)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getProcessVariables")
    public void verifyGetProcessVariables() {
        Map<String, Object> response = camundaRuntimeClient.getProcessVariables(PROCESS_INSTANCE_ID);

        assertThat(response.get("caseId"), is(equalTo("1712345678901234")));
    }

    @Test
    @PactTestFor(pactMethod = "queryProcessInstances")
    public void verifyQueryProcessInstances() {
        List<ProcessInstanceDto> response = camundaRuntimeApi.queryProcessInstances(
            SERVICE_AUTH_TOKEN,
            null,
            50,
            "startTime",
            "desc",
            Map.of("processDefinitionKey", "civil")
        );

        assertThat(response.size(), is(equalTo(1)));
    }

    @Test
    @PactTestFor(pactMethod = "fetchExternalTaskErrorDetails")
    public void verifyFetchExternalTaskErrorDetails() {
        Map<String, Object> response = camundaRuntimeApi.fetchErrorDetails(INCIDENT_ID, SERVICE_AUTH_TOKEN);

        assertThat(response.get("message"), is(equalTo("External task failed")));
    }

    @Test
    @PactTestFor(pactMethod = "getLatestIncident")
    public void verifyGetLatestIncident() {
        List<IncidentDto> response = camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            SERVICE_AUTH_TOKEN,
            true,
            PROCESS_INSTANCE_ID,
            "incidentTimestamp",
            "desc",
            1
        );

        assertThat(response.size(), is(equalTo(1)));
    }

    @Test
    @PactTestFor(pactMethod = "setJobRetries")
    public void verifySetJobRetries() {
        camundaRuntimeApi.setJobRetries(SERVICE_AUTH_TOKEN, JOB_ID, Map.of("retries", 1));
    }

    private DslPart buildProcessVariablesResponse() {
        return newJsonBody(root -> root.object("caseId", caseId -> caseId
            .stringValue("type", "String")
            .stringValue("value", "1712345678901234")
            .object("valueInfo", valueInfo -> {
            }))).build();
    }

    private DslPart buildProcessInstanceResponse() {
        return newJsonArray(root -> root.object(processInstance -> processInstance
            .stringValue("id", PROCESS_INSTANCE_ID)
            .stringValue("definitionId", "civil:1:definition")
            .stringValue("businessKey", "1712345678901234")
            .booleanValue("ended", false)
            .booleanValue("suspended", false))).build();
    }

    private DslPart buildIncidentResponse() {
        return newJsonArray(root -> root.object(incident -> incident
            .stringValue("id", INCIDENT_ID)
            .stringValue("processInstanceId", PROCESS_INSTANCE_ID)
            .stringValue("incidentType", "failedExternalTask")
            .stringValue("incidentMessage", "External task failed")
            .stringValue("configuration", JOB_ID))).build();
    }
}
