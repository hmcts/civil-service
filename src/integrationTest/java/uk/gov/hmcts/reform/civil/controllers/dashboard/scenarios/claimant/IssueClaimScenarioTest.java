package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_AWAIT;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class IssueClaimScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_claimIssue_response_await_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(Map.of())
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA7_CLAIM_ISSUE_RESPONSE_AWAIT.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for defendant to respond"),
                jsonPath("$[0].descriptionEn").value(
                    "${claimantName} has until <Date> to respond. They can request an extra 28 days if they need it.")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a href={VIEW_CLAIM_URL}>View the claim</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[1].taskNameEn")
                    .value("<a href={VIEW_INFO_ABOUT_CLAIMANT}>View information about the claimant</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[2].taskNameEn")
                    .value("<a href={VIEW_INFO_ABOUT_DEFENDANT}>View information about the defendant</a>"),
                jsonPath("$[2].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[3].taskNameEn")
                    .value("<a href={VIEW_ORDERS_AND_NOTICES}>View orders and notices</a>"),
                jsonPath("$[3].currentStatusEn").value(TaskStatus.AVAILABLE.getName())

            );

    }

}
