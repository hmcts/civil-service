package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.generalapplication.respondent;

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
public class UrgentApplicationSubmittedRespondentScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_URGENT_APPLICATION_SUBMITTED_RESPONDENT = "Scenario.AAA6.GeneralApps.UrgentApplicationMade.Respondent";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    void should_create_scenario_for_respondent_when_urgent_application_submitted() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_URGENT_APPLICATION_SUBMITTED_RESPONDENT, caseId
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
                    "<p class=\"govuk-body\">Their request is being treated urgently as there’s a hearing date in the next 10 days. A judge will decide what the next steps"
                        + " should be.</p><p class=\"govuk-body\">You can still <a href=\"{GA_RESPONDENT_INFORMATION_URL}\" class=\"govuk-link\">review the request and respond</a>"
                        + " by 4pm on ${generalAppNotificationDeadlineDateEn}, but a judge may have decided on the next steps before you do so.</p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae eu cais yn cael ei drin ar frys gan fod dyddiad gwrandawiad o fewn y 10 diwrnod nesaf. Bydd barnwr yn penderfynu beth"
                        + " ddylai'r camau nesaf fod.</p><p class=\"govuk-body\">Gallwch barhau i <a href=\"{GA_RESPONDENT_INFORMATION_URL}\" class=\"govuk-link\">adolygu’r cais ac ymateb</a>"
                        + " erbyn 4pm ar ${generalAppNotificationDeadlineDateCy}, ond efallai y bydd barnwr wedi penderfynu ar y camau nesaf cyn i chi wneud hynny.</p>")
            );
    }

}
