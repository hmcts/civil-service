package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.CreateClaimIssueNotificationsHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IssueClaimScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CreateClaimIssueNotificationsHandler handler;

    @Test
    void should_create_claimIssue_response_await_scenario() throws Exception {

        String caseId = "4123456789111";
        LocalDateTime responseDeadline = LocalDateTime.now();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .caseReference(Long.valueOf(caseId))
            .respondent1ResponseDeadline(responseDeadline)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for defendant to respond"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr. Sole Trader has until "
                        + DateUtils.formatDate(responseDeadline) + " to respond. They can request an extra 28 days if they need it.</p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_CLAIM_URL} rel=\"noopener noreferrer\" class=\"govuk-link\">View the claim</a>"),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={VIEW_CLAIM_URL} rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld yr hawliad</a>"),
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
