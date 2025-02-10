package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.generalapplication.applicant;

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
public class GeneralApplicationTranslatedDocUploadedScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_MORE_INFO_REQ = "Scenario.AAA6.GeneralApps.TranslatedDocumentUploaded.Applicant";
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
                jsonPath("$[0].titleEn").value("Translated document for the application is now available"),
                jsonPath("$[0].titleCy").value("Mae’r ddogfen a gyfieithwyd ar gyfer y cais bellach ar gael"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\"><a href=\"{GA_RESPONSE_VIEW_APPLICATION_URL}\" class=\"govuk-link\">View translated application documents.</a></p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\"><a href=\"{GA_RESPONSE_VIEW_APPLICATION_URL}\" class=\"govuk-link\">Gweld dogfennau’r cais a gyfieithwyd.</a></p>")
            );
    }

}
