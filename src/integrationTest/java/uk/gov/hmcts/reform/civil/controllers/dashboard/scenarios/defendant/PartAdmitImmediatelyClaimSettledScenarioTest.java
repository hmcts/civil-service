package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class PartAdmitImmediatelyClaimSettledScenarioTest extends BaseIntegrationTest {

    @Test
    void should_create_scenario_for_part_admit_immediate_accepted() throws Exception {

        UUID caseId = UUID.randomUUID();

        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>(Map.of("defendantAdmittedAmount", "£100",
                                                "respondent1AdmittedAmountPaymentDeadlineEn", "1 January 2024",
                                                "respondent1AdmittedAmountPaymentDeadlineCy", "1 January 2024"
                   )))
                   .build(),
               DASHBOARD_CREATE_SCENARIO_URL, "Scenario.AAA6.ClaimantIntent.PartAdmit.Defendant", caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Immediate payment"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">The claimant has accepted your plan to pay £100 "
                               + "immediately. Funds must clear <a href={VIEW_INFO_ABOUT_CLAIMANT} "
                               + "class=\"govuk-link\">their account</a> by 1 January 2024.</p><p class=\"govuk-body\">If they don´t receive the "
                               + "money by then, they can request a County Court Judgment.</p>")
            );
    }
}
