package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_DEFENDANT;

public class HearingScheduledDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Test
    void should_create_hearing_scheduled_scenario() throws Exception {
        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>(Map.of("hearingDateEn", "1 April 2024", "hearingCourtEn", "Court Name",
                                                "hearingDateCy", "1 April 2024", "hearingCourtCy", "Court Name")))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA6_CP_HEARING_SCHEDULED_DEFENDANT.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("A hearing has been scheduled"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Your hearing has been scheduled for 1 April 2024 at "
                        + "Court Name. Please keep your contact details and anyone you wish to rely on in court up" +
                        " to date. You can update contact details by telephoning the court at 0300 123 7050." +
                        " <a href=\"{VIEW_HEARING_NOTICE_CLICK}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">View the hearing notice</a>.</p>"),
                jsonPath("$[0].titleCy").value("Mae gwrandawiad wedi'i drefnu"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae eich gwrandawiad wedi'i drefnu ar gyfer 1 April 2024 yn "
                        + "Court Name. Cadwch eich manylion cyswllt chi a manylion cyswllt unrhyw un yr hoffech ddibynnu arnynt yn y llys yn gyfredol." +
                        " Gallwch ddiweddaru manylion cyswllt drwy ffonio'r llys ar 0300 303 5174." +
                        " <a href=\"{VIEW_HEARING_NOTICE_CLICK}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">Gweld yr hysbysiad o wrandawiad</a>.</p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_HEARINGS}  rel=\"noopener noreferrer\" class=\"govuk-link\">View hearings</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={VIEW_HEARINGS}  rel=\"noopener noreferrer\" class=\"govuk-link\">View hearings</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[1].reference").value(caseId.toString()),
                jsonPath("$[1].taskNameEn").value(
                    "<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[1].taskNameCy").value(
                    "<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[1].currentStatusCy").value(TaskStatus.AVAILABLE.getName())

            );
    }
}
