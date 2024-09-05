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
public class OtherPartyUploadedDocumentTest extends BaseIntegrationTest {

    public static final String SCENARIO_OTHER_PARTY_UPLOADED_DOC_RESPONDENT = "Scenario.AAA6.GeneralApps.OtherPartyUploadedDocuments.Respondent";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    void should_create_scenario_for_respondent_when_other_party_uploaded_document() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_OTHER_PARTY_UPLOADED_DOC_RESPONDENT, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "RESPONDENT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The other parties have uploaded documents to the application"),
                jsonPath("$[0].titleCy").value("Mae’r partïon eraill wedi uwchlwytho dogfennau i’r cais"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\"><a href={GA_RESPONSE_VIEW_APPLICATION_URL} rel=\"noopener noreferrer\" class=\"govuk-link\">Review the uploaded documents.</a></p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\"><a href={GA_RESPONSE_VIEW_APPLICATION_URL} rel=\"noopener noreferrer\" class=\"govuk-link\">Adolygu’r dogfennau a uwchlwythwyd.</a></p>")
            );
    }

}
