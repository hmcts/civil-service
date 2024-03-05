package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_NOTICE_CLAIM_ISSUE_HWF_NO_REMISSION;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class NoRemissionHwFScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_no_remission_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        LocalDate paymentDueDate = OffsetDateTime.now().toLocalDate();
        String claimFee = "100";
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(
                       Map.of(
                           "paymentDueDate", paymentDueDate,
                           "claimFee", claimFee
                       )
                   )
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA7_NOTICE_CLAIM_ISSUE_HWF_NO_REMISSION.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn")
                    .value("Your help with fees application has been rejected"),
                jsonPath("$[0].descriptionEn").value(
                    "We've rejected your application for help with the claim fee. See the email for "
                        + "further details. You'll need to pay the full fee of "
                        + claimFee + " by " + paymentDueDate + " . You can pay by phone by calling 0300 123 7050")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_CLAIM_URL}  rel=\"noopener noreferrer\" class=\"govuk-link\">View the claim</a>"),
                jsonPath("$[0].currentStatusEn").value("Available"),
                jsonPath("$[1].taskNameEn").value(
                    "<a href={VIEW_INFO_ABOUT_CLAIMANT}  rel=\"noopener noreferrer\" class=\"govuk-link\">View information about the claimant</a>"),
                jsonPath("$[1].currentStatusEn").value("Available"),
                jsonPath("$[2].taskNameEn").value("<a>View the response to the claim</a>"),
                jsonPath("$[2].currentStatusEn").value("Not available yet"),
                jsonPath("$[3].taskNameEn").value(
                    "<a href={VIEW_INFO_ABOUT_DEFENDANT}  rel=\"noopener noreferrer\" class=\"govuk-link\">View information about the defendant</a>"),
                jsonPath("$[3].currentStatusEn").value("Available"),
                jsonPath("$[4].taskNameEn").value("<a>View hearings</a>"),
                jsonPath("$[4].currentStatusEn").value("Not available yet"),
                jsonPath("$[5].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[5].currentStatusEn").value("Not available yet"),
                jsonPath("$[6].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[6].currentStatusEn").value("Not available yet"),
                jsonPath("$[7].taskNameEn").value("<a>View the bundle</a>"),
                jsonPath("$[7].currentStatusEn").value("Not available yet"),
                jsonPath("$[8].taskNameEn").value(
                    "<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[8].currentStatusEn").value("Available"),
                jsonPath("$[9].taskNameEn").value("<a>View the judgment</a>"),
                jsonPath("$[9].currentStatusEn").value("Not available yet"),
                jsonPath("$[10].taskNameEn").value("<a>View applications</a>"),
                jsonPath("$[10].currentStatusEn").value("Not available yet")
            );

    }

}
