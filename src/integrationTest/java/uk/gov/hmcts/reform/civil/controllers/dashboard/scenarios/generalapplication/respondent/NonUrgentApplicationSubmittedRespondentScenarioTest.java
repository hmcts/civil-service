package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.generalapplication.respondent;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class NonUrgentApplicationSubmittedRespondentScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_NON_URGENT_APPLICATION_SUBMITTED_RESPONDENT = "Scenario.AAA6.GeneralApps.NonUrgentApplicationMade.Respondent";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    @DirtiesContext
    void should_create_scenario_for_respondent_when_non_urgent_application_submitted() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_NON_URGENT_APPLICATION_SUBMITTED_RESPONDENT, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "RESPONDENT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The other parties have requested a change to the case"),
                jsonPath("$[0].titleCy").value("Mae’r partïon eraill wedi gofyn am newid yr achos"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Review their request and respond to it by 4pm on"
                        + " ${generalAppNotificationDeadlineDateEn}. After this date, the application will go to a judge"
                        + " who’ll decide what the next steps will be. <a href={GA_RESPONDENT_INFORMATION_URL}"
                        + " rel=\"noopener noreferrer\" class=\"govuk-link\">Review and respond to the request</a></p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Dylech adolygu eu cais ac ymateb iddo erbyn 4pm ar"
                        + " ${generalAppNotificationDeadlineDateCy}. Ar ôl y dyddiad hwn, bydd y cais yn mynd at farnwr a"
                        + " fydd yn penderfynu beth fydd y camau nesaf. <a href={GA_RESPONDENT_INFORMATION_URL}"
                        + " rel=\"noopener noreferrer\" class=\"govuk-link\">Adolygu ac ymateb i’r cais</a></p>")
            );
    }

}
