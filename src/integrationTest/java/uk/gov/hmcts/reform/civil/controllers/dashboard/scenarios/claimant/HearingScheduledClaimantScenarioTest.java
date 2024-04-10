package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class HearingScheduledClaimantScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_hearing_scheduled_scenario() throws Exception {
        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>(Map.of("hearingDateEn", "1 April 2024", "hearingCourtEn", "Court Name",
                                                "hearingDateCy", "1 April 2024", "hearingCourtCy", "Court Name")))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("A hearing has been scheduled"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Your hearing has been scheduled for 1 April 2024 at "
                        + "Court Name. Please keep your contact details and anyone you wish to rely on in court up" +
                        " to date. You can update contact details by telephoning the court at 0300 123 7050." +
                        " <a href=\"{VIEW_HEARING_NOTICE_CLICK}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">View the hearing notice</a>.</p>"),
                jsonPath("$[0].titleCy").value("A hearing has been scheduled"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Your hearing has been scheduled for 1 April 2024 at "
                        + "Court Name. Please keep your contact details and anyone you wish to rely on in court up" +
                        " to date. You can update contact details by telephoning the court at 0300 123 7050." +
                        " <a href=\"{VIEW_HEARING_NOTICE_CLICK}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\">View the hearing notice</a>.</p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
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

    @Test
    void should_create_hearing_fee_required_scenario() throws Exception {
        String caseId = "503206541654";

        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>(Map.of("hearingDueDateEn", "1 April 2024", "hearingDueDateCy", "1 April 2024",
                                                "hearingFee", "£200")))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(), caseId
        ).andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),

                jsonPath("$[0].titleEn").value("You must pay the hearing fee"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You must either <a href=\"{PAY_HEARING_FEE_URL_REDIRECT}\" class=\"govuk-link\">pay the hearing fee</a> of £200 "
                        + "or <a href=\"{APPLY_HELP_WITH_FEES_START_REDIRECT}\" class=\"govuk-link\"> apply for help with fees</a>. " +
                        "You must do this by 1 April 2024. If you do not take one of these actions, your claim will be struck out."),
                jsonPath("$[0].titleCy").value("You must pay the hearing fee"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">You must either <a href=\"{PAY_HEARING_FEE_URL_REDIRECT}\" class=\"govuk-link\">pay the hearing fee</a> of £200 "
                        + "or <a href=\"{APPLY_HELP_WITH_FEES_START_REDIRECT}\" class=\"govuk-link\"> apply for help with fees</a>. " +
                        "You must do this by 1 April 2024. If you do not take one of these actions, your claim will be struck out.")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={PAY_HEARING_FEE} class=\"govuk-link\">Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={PAY_HEARING_FEE} class=\"govuk-link\">Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.AVAILABLE.getName())

            );
    }
}
