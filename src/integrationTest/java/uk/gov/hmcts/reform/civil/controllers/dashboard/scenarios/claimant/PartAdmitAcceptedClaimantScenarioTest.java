package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

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
public class PartAdmitAcceptedClaimantScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_PART_ADMIT_ACCEPTED_CLAIMANT = "Scenario.AAA6.ClaimantIntent.PartAdmit.Claimant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";
    private static final String GET_NOTIFICATIONS_URL
        = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";

    @Test
    void should_create_scenario_for_part_admit_accepted_claimant() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(Map.of("respondent1PartyName", "John Doe",
                                  "defendantAdmittedAmount", "£500",
                                  "respondent1AdmittedAmountPaymentDeadlineEn", "19th March 2024",
                                  "respondent1AdmittedAmountPaymentDeadlineCy", "19th March 2024"
                   )).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_PART_ADMIT_ACCEPTED_CLAIMANT, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Immediate payment"),
                jsonPath("$[0].descriptionEn").value("<p class=\"govuk-body\">John Doe said they will pay you £500 immediately. Funds must clear your account by 19th March 2024.</p> <p class=\"govuk-body\">If you don´t receive the money by then, you can <a href={COUNTY_COURT_JUDGEMENT_URL} class=\"govuk-link\">request a County Court Judgment</a>.</p>")
            );
    }
}
