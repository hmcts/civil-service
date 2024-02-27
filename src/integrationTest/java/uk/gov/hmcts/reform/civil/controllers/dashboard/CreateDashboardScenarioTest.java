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
@Sql("/scripts/dashboardNotifications/create_dashboard_scenarios.sql")
public class CreateDashboardScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_HEARING_FEE_PAYMENT_REQUIRED = "scenario.hearing.fee.payment.required";

    public static final String SCENARIO_CLAIMANT_AFTER_PAY_FEE_SCENARIO = "scenario.claimissue.claimfee.required";
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
    void should_create_scenario_for_after_pay_fee_required() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(Map.of("claimantName", "testName"
                   ))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_CLAIMANT_AFTER_PAY_FEE_SCENARIO, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for defendant to respond"),
                jsonPath("$[0].descriptionEn")
                    .value("${claimantName} has until <Date> to respond. They can request an extra 28 days if they need it.")
            );
    }
}
