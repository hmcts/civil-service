package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.applicant;

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
public class GeneralAppSubmittedScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_SUBMITTED = "Scenario.AAA6.GeneralApps.ApplicationSubmitted.Applicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";
    private static final String UPDATE_DASHBOARD_NOTIFICATION = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";

    @Test
    @DirtiesContext

    void should_create_scenario_for_general_application_submitted() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_SUBMITTED, caseId
        )
            .andExpect(status().isOk());

        //Verify Dashboard Notification is created

        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "APPLICANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Application is being processed"),
                jsonPath("$[0].titleCy").value("Mae’r cais yn cael ei brosesu"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\"> A judge will consider the application. </p><p class=\"govuk-body\"> The other parties can respond within 5 working days after " +
                        "the application is submitted," +
                        " unless you've chosen not to inform them." +
                        " If you have a hearing in the next 10 days, your application will be treated urgently." +
                        " <a href=\"{GA_VIEW_APPLICATION_URL}\" class=\"govuk-link\">View application documents</a></p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\"> Bydd barnwr yn ystyried y cais. </p>" +
                        "<p class=\"govuk-body\"> Gall y partïon eraill ymateb o fewn 5 diwrnod gwaith ar ôl i’r cais gael ei gyflwyno," +
                        " oni bai eich bod wedi dewis peidio â rhoi gwybod iddynt." +
                        " Os oes gennych wrandawiad o fewn y 10 diwrnod nesaf, bydd eich cais yn cael ei drin ar frys." +
                        " <a href=\"{GA_VIEW_APPLICATION_URL}\" class=\"govuk-link\">Gweld dogfennau’r cais</a></p>")
            );
    }
}
