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
public class GeneralAppIssuePaymentRequiredTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_ISSUE_FEE_REQUIRED = "Scenario.AAA6.GeneralApps.ApplicationFeeRequired.Applicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    @DirtiesContext
    void should_create_scenario_for_general_application_created() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_ISSUE_FEE_REQUIRED, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "APPLICANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Pay application fee"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">To finish making your application, you must pay the application fee of ${applicationFee} as soon as possible. Your application will be paused and will not be sent to the other parties or considered by a judge until you’ve paid the fee. <a href={GA_VIEW_APPLICATION_URL} rel=\"noopener noreferrer\" class=\"govuk-link\">Pay application fee</a></p>"),
                jsonPath("$[0].titleCy").value("Talu ffi gwneud cais"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">I orffen gwneud eich cais, rhaid i chi dalu’r ffi gwneud cais o ${applicationFee} cyn gynted â phosib. Bydd eich cais yn cael ei oedi ac ni chaiff ei anfon at y partïon eraill na’i ystyried gan farnwr nes i chi dalu’r ffi. <a href={GA_VIEW_APPLICATION_URL} rel=\"noopener noreferrer\" class=\"govuk-link\">Talu’r ffi gwneud cais</a></p>")

            );
    }

}
