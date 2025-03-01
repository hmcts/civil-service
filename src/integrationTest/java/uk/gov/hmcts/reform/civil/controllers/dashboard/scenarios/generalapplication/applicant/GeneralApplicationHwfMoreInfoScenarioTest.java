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
public class GeneralApplicationHwfMoreInfoScenarioTest extends BaseIntegrationTest {

    public static final String SCENARIO_GA_HWF_MoreInfo = "Scenario.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant";
    private static final String DASHBOARD_CREATE_SCENARIO_URL
        = "/dashboard/scenarios/{scenario_ref}/{unique_case_identifier}";

    @Test
    void should_create_scenario_for_general_application_hef_rejected() throws Exception {

        UUID caseId = UUID.randomUUID();
        doPost(BEARER_TOKEN,
               ScenarioRequestParams.builder()
                   .params(new HashMap<>()).build(),
               DASHBOARD_CREATE_SCENARIO_URL, SCENARIO_GA_HWF_MoreInfo, caseId
        )
            .andExpect(status().isOk());

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "APPLICANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Your help with fees application needs more information"),
                jsonPath("$[0].titleCy").value("Mae angen i chi ddarparu mwy o wybodaeth ar gyfer eich cais am help i dalu ffioedd"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">We need more information on your application for help with the ${applicationFeeTypeEn} fee.</p>" +
                        "<p class=\"govuk-body\">You’ve been sent an email with further details. " +
                        "If you’ve already read the email and taken action, you can disregard this message.</p>" +
                        "<p class=\"govuk-body\">You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae arnom angen mwy o wybodaeth ar gyfer eich cais am help i dalu'r ffi ${applicationFeeTypeCy}.</p>" +
                        "<p class=\"govuk-body\">Anfonwyd e-bost atoch gyda rhagor o fanylion. " +
                        "Os ydych wedi darllen yr e-bost yn barod ac wedi gweithredu, gallwch anwybyddu'r neges hon.</p>" +
                        "<p class=\"govuk-body\">Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>")
            );
    }
}
