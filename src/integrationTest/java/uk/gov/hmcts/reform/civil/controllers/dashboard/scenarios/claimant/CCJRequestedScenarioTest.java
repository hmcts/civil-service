package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class CCJRequestedScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_ccj_requested_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        String defendantName = "Dave Indent";
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(
                       Map.of(
                           "respondent1ResponseDeadline", responseDeadline,
                           "respondent1PartyName", defendantName
                       )
                   )
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA7_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("County Court Judgment (CCJ) requested"),
                jsonPath("$[0].descriptionEn").value(
                    "We’ll process your request and post a copy of the judgment to you and "
                        + defendantName
                        + ". We aim to do this as soon as possible.<br><br>"
                        + "Your online account will not be updated, and "
                        + defendantName
                        + " will no longer be able to respond to your claim online. Any further updates will be by post.<br><br>"
                        + "If a postal response is received before the judgment is issued, your request will be rejected.<br><br>"
                        + "<a href=\"{enforceJudgementUrl}\" rel=\"noopener noreferrer\" class=\"govuk-link\">Find out about actions you can take once a CCJ is issued (opens in a new tab).</a>.")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_CLAIM_URL} rel=\"noopener noreferrer\" class=\"govuk-link\">View the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[1].taskNameEn")
                    .value(
                        "<a href={VIEW_INFO_ABOUT_CLAIMANT} rel=\"noopener noreferrer\" class=\"govuk-link\">View information about the claimant</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[2].taskNameEn")
                    .value(
                        "<a href={VIEW_INFO_ABOUT_DEFENDANT} rel=\"noopener noreferrer\" class=\"govuk-link\">View information about the defendant</a>"),
                jsonPath("$[2].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[3].taskNameEn")
                    .value(
                        "<a href={VIEW_ORDERS_AND_NOTICES} rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[3].currentStatusEn").value(TaskStatus.AVAILABLE.getName())

            );

    }

}
