package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimIssueNotificationsHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimIssuedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimIssueNotificationsHandler handler;

    @Test
    void should_create_scenario_for_claim_issue() throws Exception {

        String caseId = "12348991015";

        LocalDateTime dateTime = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1ResponseDeadline(dateTime)
            .issueDate(dateTime.toLocalDate())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
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
                jsonPath("$[4].taskNameEn").value("<a>View mediation settlement agreement</a>"),
                jsonPath("$[4].currentStatusEn").value("Not available yet"),
                jsonPath("$[5].taskNameEn").value("<a>Upload mediation documents</a>"),
                jsonPath("$[5].currentStatusEn").value("Not available yet"),
                jsonPath("$[6].taskNameEn").value("<a>View mediation documents</a>"),
                jsonPath("$[6].currentStatusEn").value("Not available yet"),
                jsonPath("$[7].taskNameEn").value("<a>View hearings</a>"),
                jsonPath("$[7].currentStatusEn").value("Not available yet"),
                jsonPath("$[8].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[8].currentStatusEn").value("Not available yet"),
                jsonPath("$[9].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[9].currentStatusEn").value("Not available yet"),
                jsonPath("$[10].taskNameEn").value("<a>View the bundle</a>"),
                jsonPath("$[10].currentStatusEn").value("Not available yet"),
                jsonPath("$[11].taskNameEn").value(
                    "<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[11].currentStatusEn").value("Available"),
                jsonPath("$[12].taskNameEn").value("<a>View the judgment</a>"),
                jsonPath("$[12].currentStatusEn").value("Not available yet"),
                jsonPath("$[13].taskNameEn").value("<a>View applications</a>"),
                jsonPath("$[13].currentStatusEn").value("Not available yet")

            );

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You haven´t responded to the claim"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">You need to respond before 4pm on "
                               + DateUtils.formatDate(dateTime)
                               + ". There are {daysLeftToRespond} days remaining. <a href=\"{RESPONSE_TASK_LIST_URL}\""
                               + "  rel=\"noopener noreferrer\" class=\"govuk-link\">Respond to the claim</a>.</p>")
            );
    }
}
