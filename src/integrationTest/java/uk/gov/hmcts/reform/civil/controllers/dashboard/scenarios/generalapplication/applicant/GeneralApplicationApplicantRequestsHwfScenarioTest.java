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
public class GeneralApplicationApplicantRequestsHwfScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_HWF_REQUESTED_APPLICANT = "Scenario.AAA6.GeneralApps.HwFRequested.Applicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    @DirtiesContext
    void should_create_scenario_for_general_application_hwf_requested_applicant() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_HWF_REQUESTED_APPLICANT, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "APPLICANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("We’re reviewing your help with fees application"),
                jsonPath("$[0].titleCy").value("Rydym yn adolygu eich cais am help i dalu ffioedd"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You’ve applied for help with the ${applicationFeeTypeEn} fee. "
                    + "You’ll receive an update in 5 to 10 working days.</p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Fe wnaethoch gais am help i dalu’r ffi sef "
                    + "${applicationFeeTypeCy}. Byddwch yn cael diweddariad mewn 5 i 10 diwrnod gwaith.</p>")
            );
    }
}
