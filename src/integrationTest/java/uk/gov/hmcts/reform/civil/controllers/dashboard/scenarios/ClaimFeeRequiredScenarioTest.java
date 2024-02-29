package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ClaimFeeRequiredScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_CLAIM_FEE_REQUIRED = "Scenario.AAA7.ClaimIssue.ClaimFee.Required";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";
    private static final String GET_NOTIFICATIONS_URL
        = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";
    private static final String GET_TASKS_ITEMS_URL = "/dashboard/taskList/{ccd-case-identifier}/role/{role-type}";

    @Test
    void should_create_scenario_for_claim_fee_required() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(Map.of("claimFee", "£70"
                   )).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_CLAIM_FEE_REQUIRED, caseId
        )
            .andExpect(status().isOk());

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a href={VIEW_CLAIM_URL}  rel=\"noopener noreferrer\" class=\"govuk-link\">View the claim</a>"),
                jsonPath("$[0].currentStatusEn").value("Available"),
                jsonPath("$[1].taskNameEn").value("<a href={VIEW_INFO_ABOUT_CLAIMANT_URL}  rel=\"noopener noreferrer\" class=\"govuk-link\">View information about the claimant</a>"),
                jsonPath("$[1].currentStatusEn").value("Available"),
                jsonPath("$[2].taskNameEn").value("<a>View the response to the claim</a>"),
                jsonPath("$[2].currentStatusEn").value("Not available yet"),
                jsonPath("$[3].taskNameEn").value("<a>View information about the defendant</a>"),
                jsonPath("$[3].currentStatusEn").value("Not available yet"),
                jsonPath("$[4].taskNameEn").value("<a>View hearings</a>"),
                jsonPath("$[4].currentStatusEn").value("Not available yet"),
                jsonPath("$[5].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[5].currentStatusEn").value("Not available yet"),
                jsonPath("$[6].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[6].currentStatusEn").value("Not available yet"),
                jsonPath("$[7].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[7].currentStatusEn").value("Not available yet"),
                jsonPath("$[8].taskNameEn").value("<a>View the bundle</a>"),
                jsonPath("$[8].currentStatusEn").value("Not available yet"),
                jsonPath("$[9].taskNameEn").value("<a href={VIEW_ORDERS_AND_NOTICES_URL}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[9].currentStatusEn").value("Available"),
                jsonPath("$[10].taskNameEn").value("<a>View the judgment</a>"),
                jsonPath("$[10].currentStatusEn").value("Not available yet"),
                jsonPath("$[11].taskNameEn").value("<a>View applications</a>"),
                jsonPath("$[11].currentStatusEn").value("Not available yet")

            );

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You need to pay your claim fee"),
                jsonPath("$[0].descriptionEn").value("Your claim has not yet been issued, in order to proceed you must pay the claim fee of £70. <a href={CLAIM_FEE_URL}  rel=\"noopener noreferrer\" class=\"govuk-link\">Pay the claim fee</a>.")
            );
    }

}
