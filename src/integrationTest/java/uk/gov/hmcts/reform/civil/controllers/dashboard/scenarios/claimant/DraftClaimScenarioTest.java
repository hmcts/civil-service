package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_SUBMIT_REQUIRED;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DraftClaimScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_draft_claim_scenario() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>())
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_AAA6_CLAIM_ISSUE_CLAIM_SUBMIT_REQUIRED.getScenario(), caseId
        )
            .andExpect(status().isOk());

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>View the claim</a>"),
                jsonPath("$[0].currentStatusEn").value("Not available yet"),
                jsonPath("$[1].taskNameEn").value("<a>View information about the claimant</a>"),
                jsonPath("$[1].currentStatusEn").value("Not available yet"),
                jsonPath("$[2].taskNameEn").value("<a>View the response to the claim</a>"),
                jsonPath("$[2].currentStatusEn").value("Not available yet"),
                jsonPath("$[3].taskNameEn").value("<a>View information about the defendant</a>"),
                jsonPath("$[3].currentStatusEn").value("Not available yet"),
                jsonPath("$[4].taskNameEn").value("<a>View mediation settlement agreement</a>"),
                jsonPath("$[4].currentStatusEn").value("Not available yet"),
                jsonPath("$[5].taskNameEn").value("<a>Upload mediation documents</a>"),
                jsonPath("$[5].currentStatusEn").value("Not available yet"),
                jsonPath("$[6].taskNameEn").value("<a>View mediation documents</a>"),
                jsonPath("$[6].currentStatusEn").value("Not available yet"),
                jsonPath("$[7].taskNameEn").value("<a>View the hearing</a>"),
                jsonPath("$[7].currentStatusEn").value("Not available yet"),
                jsonPath("$[8].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[8].currentStatusEn").value("Not available yet"),
                jsonPath("$[9].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[9].currentStatusEn").value("Not available yet"),
                jsonPath("$[10].taskNameEn").value("<a>View documents</a>"),
                jsonPath("$[10].currentStatusEn").value("Not available yet"),
                jsonPath("$[11].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[11].currentStatusEn").value("Not available yet"),
                jsonPath("$[12].taskNameEn").value("<a>View the bundle</a>"),
                jsonPath("$[12].currentStatusEn").value("Not available yet"),
                jsonPath("$[13].taskNameEn").value("<a>View orders and notices</a>"),
                jsonPath("$[13].currentStatusEn").value("Not available yet"),
                jsonPath("$[14].taskNameEn").value("<a>View the judgment</a>"),
                jsonPath("$[14].currentStatusEn").value("Not available yet"),
                jsonPath("$[15].taskNameEn").value("<a>Contact the court to request a change to my case</a>"),
                jsonPath("$[15].currentStatusEn").value("Not available yet"),
                jsonPath("$[16].taskNameEn").value("<a>View applications</a>"),
                jsonPath("$[16].currentStatusEn").value("Not available yet")

            );

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("This claim has not been submitted"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">Your claim is saved as a draft. <a href=\"{DRAFT_CLAIM_TASK_LIST}\" "
                               + "rel=\"noopener noreferrer\" class=\"govuk-link\">Continue with claim</a></p>")
            );
    }

}
