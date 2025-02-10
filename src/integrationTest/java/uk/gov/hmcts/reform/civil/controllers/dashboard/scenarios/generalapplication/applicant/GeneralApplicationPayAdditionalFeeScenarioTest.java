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
public class GeneralApplicationPayAdditionalFeeScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_MORE_INFO_REQ = "Scenario.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    @DirtiesContext
    void should_create_scenario_for_general_application_more_info_required() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_MORE_INFO_REQ, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "APPLICANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You must pay an additional application fee"),
                jsonPath("$[0].titleCy").value("Rhaid i chi dalu ffi ychwanegol i wneud cais"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The court requires you to pay an additional fee before your application can progress further. " +
                        "<a href=\"{GA_VIEW_APPLICATION_URL}\" class=\"govuk-link\">Pay the additional application fee</a></p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae'r llys angen i chi dalu ffi ychwanegol cyn y gall eich cais gael ei brosesu ymhellach. " +
                        "<a href=\"{GA_VIEW_APPLICATION_URL}\" class=\"govuk-link\">Talu ffi’r gwneud cais ychwanegol</a></p>")
            );
    }

}
