package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.generalapplication.applicant;

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

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class GeneralApplicationHwfPartialRemittanceScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_HEF_REJECTED = "Scenario.AAA6.GeneralApps.HwF.PartRemission.Applicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    void should_create_scenario_for_general_application_hwf_partialRemittance() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_HEF_REJECTED, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "APPLICANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Your help with fees application has been reviewed"),
                jsonPath("$[0].titleCy").value("Mae eich cais am help i dalu ffioedd wedi cael ei adolygu"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You’ll get help with the ${applicationFeeTypeEn} fee. ${remissionAmount} will be covered by fee remission.</p>" +
                        "<p class=\"govuk-body\">To progress your application, you must  still pay the remaining fee of ${outstandingFeeInPounds}.</p>" +
                        "<p class=\"govuk-body\">You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Byddwch yn cael help i dalu’r ${applicationFeeTypeCy}. Bydd ${remissionAmount} o’r ffi yn cael ei ddileu.</p>" +
                        "<p class=\"govuk-body\">I symud eich cais yn ei flaen, rhaid i chi dalu’r ffi sy’ weddill sef ${outstandingFeeInPounds}.</p>" +
                        "<p class=\"govuk-body\">Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>")
            );
    }
}
