package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.generalapplication.applicant;

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
public class GeneralApplicationHwfInvalidReferenceScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_HWF_INVALID_REF_REQ = "Scenario.AAA6.GeneralApps.HwF.InvalidRef.Applicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    void should_create_scenario_for_general_application_more_info_required() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_HWF_INVALID_REF_REQ, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "APPLICANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You've provided an invalid help with fees reference number"),
                jsonPath("$[0].titleCy").value("Rydych wedi darparu cyfeirnod help i dalu ffioedd annilys"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You've applied for help with the ${applicationFeeTypeEn} fee, but the reference number is invalid.</p>" +
                        "<p class=\"govuk-body\">You've been sent an email with instructions on what to do next. " +
                        "If you've already read the email and taken action, you can disregard this message.</p>" +
                        "<p class=\"govuk-body\">You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych wedi gwneud cais am help i dalu'r ffi gwneud ${applicationFeeTypeCy}, ond mae'r cyfeirnod yn annilys.</p>" +
                        "<p class=\"govuk-body\">Anfonwyd e-bost atoch gyda chyfarwyddiadau ar beth i'w wneud nesaf. " +
                        "Os ydych wedi darllen yr e-bost yn barod ac wedi gweithredu, gallwch anwybyddu'r neges hon.</p>" +
                        "<p class=\"govuk-body\">Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>")
            );
    }

}
