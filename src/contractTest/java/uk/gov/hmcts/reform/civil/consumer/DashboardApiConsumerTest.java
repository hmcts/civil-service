package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;

@PactTestFor(providerName = "dashboard_api")
@MockServerConfig(hostInterface = "localhost", port = "6686")
@TestPropertySource(properties = "dashboard.api.url=http://localhost:6686")
public class DashboardApiConsumerTest extends BaseContractTest {

    private static final String CASE_ID = "1712345678901234";
    private static final String ROLE_TYPE = "APPLICANT";
    private static final String TEMPLATE_NAME = "application-submitted";
    private static final String SCENARIO_REF = "Scenario.AAA6.GeneralApps.ApplicationSubmitted.Applicant";

    @Autowired
    private DashboardApiClient dashboardApiClient;

    @Pact(consumer = "civil_service")
    public RequestResponsePact recordScenario(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a dashboard record scenario request")
            .path("/dashboard/scenarios/" + SCENARIO_REF + "/" + CASE_ID)
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .method(HttpMethod.POST.toString())
            .body("""
                {
                  "params": {
                    "ccdCaseReference": "%s",
                    "partyName": "Jane Smith"
                  }
                }
                """.formatted(CASE_ID))
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact deleteNotificationsForRole(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a dashboard delete notifications for role request")
            .path("/dashboard/notifications/" + CASE_ID + "/role/" + ROLE_TYPE)
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.DELETE.toString())
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact deleteTemplateNotificationsForRole(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a dashboard delete template notifications for role request")
            .path("/dashboard/notifications/" + CASE_ID + "/role/" + ROLE_TYPE + "/" + TEMPLATE_NAME)
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.DELETE.toString())
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "recordScenario")
    public void verifyRecordScenario() {
        dashboardApiClient.recordScenario(CASE_ID, SCENARIO_REF, AUTHORIZATION_TOKEN, scenarioRequestParams());
    }

    @Test
    @PactTestFor(pactMethod = "deleteNotificationsForRole")
    public void verifyDeleteNotificationsForRole() {
        dashboardApiClient.deleteNotificationsForCaseIdentifierAndRole(CASE_ID, ROLE_TYPE, AUTHORIZATION_TOKEN);
    }

    @Test
    @PactTestFor(pactMethod = "deleteTemplateNotificationsForRole")
    public void verifyDeleteTemplateNotificationsForRole() {
        dashboardApiClient.deleteTemplateNotificationsForCaseIdentifierAndRole(
            CASE_ID,
            ROLE_TYPE,
            TEMPLATE_NAME,
            AUTHORIZATION_TOKEN
        );
    }

    private ScenarioRequestParams scenarioRequestParams() {
        return new ScenarioRequestParams(new HashMap<>(Map.of(
            "ccdCaseReference", CASE_ID,
            "partyName", "Jane Smith"
        )));
    }
}
