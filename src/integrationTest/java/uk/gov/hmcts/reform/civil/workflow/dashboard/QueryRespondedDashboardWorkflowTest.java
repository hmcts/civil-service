package uk.gov.hmcts.reform.civil.workflow.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.workflow.dashboard.fixture.QueryManagementDashboardFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@SuppressWarnings("java:S5960")
class QueryRespondedDashboardWorkflowTest extends DashboardWorkflowIntegrationTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
    }

    @Test
    void shouldCreateDashboardNotificationForLipClaimantWhenQueryResponseReceived() throws Exception {
        startWorkflow(QueryManagementDashboardFixtures.lipClaimantCaseWithQueryResponse())
            .eventId(CaseEvent.UPDATE_DASHBOARD_NOTIFICATIONS_RESPONSE_TO_QUERY)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, QueryManagementDashboardFixtures.caseReference(), "CLAIMANT")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].titleEn").isNotEmpty());
    }

    @Test
    void shouldCreateDashboardNotificationForLipDefendantWhenQueryResponseReceived() throws Exception {
        startWorkflow(QueryManagementDashboardFixtures.lipDefendantCaseWithQueryResponse())
            .eventId(CaseEvent.UPDATE_DASHBOARD_NOTIFICATIONS_RESPONSE_TO_QUERY)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, QueryManagementDashboardFixtures.caseReference(), "DEFENDANT")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].titleEn").isNotEmpty());
    }
}
