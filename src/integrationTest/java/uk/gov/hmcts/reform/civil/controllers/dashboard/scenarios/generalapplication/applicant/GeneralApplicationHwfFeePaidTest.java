package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.generalapplication.applicant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class GeneralApplicationHwfFeePaidTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_HWF_FEE_PAID = "Scenario.AAA6.GeneralApps.HwF.FeePaid.Applicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    @DirtiesContext
    void should_create_scenario_for_general_application_fee_paid() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_HWF_FEE_PAID, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "APPLICANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The ${applicationFeeTypeEn} fee has been paid"),
                jsonPath("$[0].titleCy").value("Mae’r ffi gwneud ${applicationFeeTypeCy} wedi cael ei thalu"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The ${applicationFeeTypeEn} fee has been paid in full.</p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae‘r ffi gwneud ${applicationFeeTypeCy} wedi cael ei thalu’n llawn.</p>")
            );
    }
}
