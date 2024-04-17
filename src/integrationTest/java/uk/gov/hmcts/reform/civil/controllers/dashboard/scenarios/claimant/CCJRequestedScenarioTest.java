package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.CCJRequestedDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CCJRequestedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CCJRequestedDashboardNotificationHandler handler;

    @Test
    void should_create_ccj_requested_scenario() throws Exception {

        String caseId = "12345678";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        String defendantName = "Mr. Sole Trader";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("County Court Judgment (CCJ) requested"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">We'll process your request and post a copy of the judgment to you and "
                        + defendantName
                        + ". We aim to do this as soon as possible.</p>"
                        + "<p class=\"govuk-body\">Your online account will not be updated, and "
                        + defendantName
                        + " will no longer be able to respond to your claim online. Any further updates will be by post.</p>"
                        + "<p class=\"govuk-body\">If a postal response is received before the judgment is issued, your request will be rejected.</p>"
                        + "<p class=\"govuk-body\"><a href=\"{enforceJudgementUrl}\" rel=\"noopener noreferrer\" class=\"govuk-link\">Find out about actions you can take once a CCJ is issued (opens in a new tab)</a>.</p>")
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
