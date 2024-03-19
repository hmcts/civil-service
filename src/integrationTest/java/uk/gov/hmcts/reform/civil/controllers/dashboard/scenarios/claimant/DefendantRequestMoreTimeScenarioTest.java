package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_DEFENDANT_RESPONSE_MORE_TIME_REQUESTED_CLAIMANT;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ResponseDeadlineExtendedDashboardNotificationHandler;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

public class DefendantRequestMoreTimeScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ResponseDeadlineExtendedDashboardNotificationHandler handler;

    @Test
    void should_create_more_time_requested_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(
            BEARER_TOKEN,
            ScenarioRequestParams.builder().params(Map.of("defaultRespondTime", "4pm",
                                                          "respondent1ResponseDeadlineEn", "1 April 2024",
                                                          "respondent1ResponseDeadlineCy", "1 April 2024")).build(),
            DASHBOARD_CREATE_SCENARIO_URL,
            SCENARIO_AAA7_DEFENDANT_RESPONSE_MORE_TIME_REQUESTED_CLAIMANT.getScenario(),
            caseId
        ).andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andDo(handler -> {
                String response = handler.getResponse().getContentAsString();
                System.out.println(response);
            })
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("More time requested"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The response deadline for the defendant is now 4pm on 1 April 2024 ({daysLeftToRespond} days remaining).</p>"),
                jsonPath("$[0].titleCy").value("More time requested"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">The response deadline for the defendant is now 4pm on 1 April 2024 ({daysLeftToRespond} days remaining).</p>")
            );
    }
}
