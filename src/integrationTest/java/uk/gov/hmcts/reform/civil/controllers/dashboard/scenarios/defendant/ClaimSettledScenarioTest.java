package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

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
public class ClaimSettledScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_scenario_for_claim_settled() throws Exception {

        UUID caseId = UUID.randomUUID();

        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(Map.of("claimSettledAmount", "£3000",
                                  "claimSettledDateEn", "16th March 2024",
                                  "claimSettledDateCy", "16th March 2024"
                   ))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, "Scenario.AAA6.ClaimantIntent.ClaimSettled.Defendant", caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The claim is settled"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">The claimant has confirmed that you paid £3000 on 16th March 2024.</p>")
            );
    }
}
