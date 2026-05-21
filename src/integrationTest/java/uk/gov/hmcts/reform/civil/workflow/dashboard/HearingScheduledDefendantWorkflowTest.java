package uk.gov.hmcts.reform.civil.workflow.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.workflow.dashboard.fixture.HearingScheduledDefendantFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@SuppressWarnings("java:S5960")
class HearingScheduledDefendantWorkflowTest extends DashboardWorkflowIntegrationTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private LocationReferenceDataService locationReferenceDataService;

    @BeforeEach
    void setup() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(locationReferenceDataService.getHearingCourtLocations(any()))
            .thenReturn(HearingScheduledDefendantFixtures.locations());
    }

    @Test
    void shouldCreateHearingScheduledDefendantNotificationsAndTasks() throws Exception {
        startWorkflow(HearingScheduledDefendantFixtures.caseData())
            .eventId(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());

        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, HearingScheduledDefendantFixtures.caseReference(), "DEFENDANT")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[1].titleEn").value("A hearing has been scheduled"));

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, HearingScheduledDefendantFixtures.caseReference(), "DEFENDANT")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reference").value(HearingScheduledDefendantFixtures.caseReference()))
            .andExpect(jsonPath("$[0].taskNameEn").value(
                "<a href={VIEW_HEARINGS}  rel=\"noopener noreferrer\" class=\"govuk-link\">View the hearing</a>"
            ));
    }
}
