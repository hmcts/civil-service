package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.applicant;

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
public class GaProcessedApplicantNotification extends BaseIntegrationTest {
    public static final String SCENARIO_GA_RESPONSE_SUBMITTED = "Scenario.AAA6.GeneralApps.RespondentResponseSubmitted.Applicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    void should_create_scenario_for_general_application_processed_applicant() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_RESPONSE_SUBMITTED, caseId
        )
            .andExpect(status().isOk());

        //Verify Dashboard Notification is created

        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "APPLICANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Application is being processed"),
                jsonPath("$[0].titleCy").value("Cais yn cael ei brosesu"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\"> A judge will consider the application. </p>" +
                        "<p class=\"govuk-body\"> You’ll receive an update with information about next steps.</p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\"> Bydd barnwr yn ystyried y cais. </p>" +
                        "<p class=\"govuk-body\">Fe gewch diweddariad gyda gwybodaeth am y camau nesaf.</p>")
            );
    }
}
