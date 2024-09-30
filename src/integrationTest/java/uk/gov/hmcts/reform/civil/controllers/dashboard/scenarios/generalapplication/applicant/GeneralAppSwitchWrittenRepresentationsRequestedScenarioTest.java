package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.generalapplication.applicant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class GeneralAppSwitchWrittenRepresentationsRequestedScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_WRITTEN_REPS_REQUESTED = "Scenario.AAA6.GeneralApps.SwitchWrittenRepresentationRequired.RespondentApplicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    void should_create_scenario_for_written_representations_requested() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_WRITTEN_REPS_REQUESTED, caseId
        )
            .andExpect(status().isOk());

        //Verify Dashboard Notification is created

        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "APPLICANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You need to provide written representation"),
                jsonPath("$[0].titleCy").value("Mae angen i chi ddarparu cynrychiolaeth ysgrifenedig"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The court has requested that you must <a href={GA_VIEW_APPLICATION_URL}"
                        + " rel=\"noopener noreferrer\" class=\"govuk-link\">provide written representation</a>. You must do this"
                        + " by 4pm on ${writtenRepApplicantDeadlineDateEn}.</p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae'r llys wedi gofyn i chi <a href={GA_VIEW_APPLICATION_URL}"
                        + " rel=\"noopener noreferrer\" class=\"govuk-link\">ddarparu cynrychiolaeth ysgrifenedig</a>."
                        + " Rhaid i chi wneud hyn erbyn 4pm ar ${writtenRepApplicantDeadlineDateCy}.</p>")
            );
    }
}
