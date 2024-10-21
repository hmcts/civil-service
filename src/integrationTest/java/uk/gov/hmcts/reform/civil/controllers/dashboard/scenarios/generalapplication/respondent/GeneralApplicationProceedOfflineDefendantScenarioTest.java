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
public class GeneralApplicationProceedOfflineDefendantScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_PROCEED_OFFLINE = "Scenario.AAA6.GeneralApps.ApplicationProceedsOffline.Respondent";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    void should_create_scenario_for_general_application_hef_rejected() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_PROCEED_OFFLINE, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("This application continues offline"),
                jsonPath("$[0].titleCy").value("Mae'r cais hwn yn parhau all-lein"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The application will continue offline. There will not be any further updates here. All updates will be by post.</p>" +
                        "<p class=\"govuk-body\">If the application has not yet been paid for, you will need to submit it to the court using " +
                        "<a href=\"{MAKE_APPLICATION_TO_COURT_URL}\" class=\"govuk-link\">form N244</a> together with your Help with Fees number if appropriate.</p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Bydd y cais yn parhau all-lein. Ni fydd unrhyw ddiweddariadau pellach yma. Bydd yr holl ddiweddariadau yn digwydd drwy'r post.</p>" +
                        "<p class=\"govuk-body\">Os nad ydych eisoes wedi talu am y cais, bydd angen i chi ei gyflwyno i'r llys gan ddefnyddio " +
                        "<a href=\"{MAKE_APPLICATION_TO_COURT_URL}\" class=\"govuk-link\">ffurflen N244</a> ynghyd â'ch rhif Help i Dalu Ffioedd os yw'n briodol.</p>")
            );
    }
}
