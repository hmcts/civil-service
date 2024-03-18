package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_DEFRESPONSE_MORETIMEREQUESTED_DEFENDANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class MoreTimeRequestedScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_ccj_requested_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        String claimantName = "Dave Indent";
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(
                       Map.of(
                           "applicant1PartyName", claimantName
                       )
                   )
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA7_DEFRESPONSE_MORETIMEREQUESTED_DEFENDANT.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("More time requested"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The response deadline for you is now ${time} on {deadline} ({days} days remaining).<a href=\" \" rel=\"noopener noreferrer\" class=\"govuk-link\"> Respond to claim</a></p>"));

    }

}
