package uk.gov.hmcts.reform.civil.controllers.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
//@Sql("/scripts/dashboardNotifications/create_dashboard_scenarios.sql")
public class CreateDashboardScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_HEARING_FEE_PAYMENT_REQUIRED = "scenario.hearing.fee.payment.required";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";
    private static final String GET_NOTIFICATIONS_URL
        = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";
    private static final String GET_TASKS_ITEMS_URL = "/dashboard/taskList/{ccd-case-identifier}/role/{role-type}";

    @Test
    void should_create_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        String hearingFeeByTime = "4 pm";
        LocalDate hearingFeeByDate = OffsetDateTime.now().toLocalDate();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(Map.of("hearingFeePayByTime", hearingFeeByTime,
                                  "hearingFeePayByDate", hearingFeeByDate
                   ))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_HEARING_FEE_PAYMENT_REQUIRED, caseId
        )
            .andExpect(status().isOk());

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "claimant")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameCy").value("<a href=#>Pay the hearing fee</a>"),
                jsonPath("$[0].hintTextCy")
                    .value("pay by "
                               + hearingFeeByTime
                               + " on "
                               + hearingFeeByDate
                               + ". you have (noOfDays) to pay.")
            );

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "claimant")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Pay the hearing fee"),
                jsonPath("$[0].descriptionEn").value("Pay the hearing fee. <a href=#>Click here</a>")
            );
    }



    @Test
    void should_create_scenario_for_claim_issue() throws Exception {

        UUID caseId = UUID.randomUUID();
        String hearingFeeByTime = "4 pm";
        OffsetDateTime hearingFeeByDate = OffsetDateTime.now();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(Map.of("defaultRespondTime", hearingFeeByTime,
                                  "responseDeadline", hearingFeeByDate,
                                  "daysLeftToRespond", 28,
                                  "ccdCaseReference", caseId
                   ))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, "Scenario.AAA7.ClaimIssue.Response.Required", caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You haven´t responded to the claim"),
                jsonPath("$[0].descriptionEn")
                    .value("You need to respond before 4 pm on " + hearingFeeByDate + ". There are 28 days remaining. <a href=\"/case/" + caseId + "/response/task-list\">Respond to the claim.</a>.")
            );
    }

}
