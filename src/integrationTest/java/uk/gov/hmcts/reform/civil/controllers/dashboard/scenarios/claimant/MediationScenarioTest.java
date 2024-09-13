package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MediationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseNotificationHandler handler;

    @Test
    void should_create_mediation_scenario() throws Exception {

        String caseId = "4123456789";
        CaseData caseData = CaseDataBuilder.builder().atStateBeforeTakenOfflineSDONotDrawn().build();
        caseData = caseData.toBuilder().ccdState(CaseState.IN_MEDIATION).ccdCaseReference(Long.valueOf(caseId)).applicant1Represented(YesOrNo.NO).build();

        // When
        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You have said you wish continue with your claim"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">Your case will be referred for mediation. Your mediation appointment will be arranged within 28 days.</p>" +
                            "<p class=\"govuk-body\"><a href=\"https://www.gov.uk/guidance/small-claims-mediation-service\"  rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\"> " +
                            "Find out more about how mediation works (opens in new tab)</a>.</p>"),
                jsonPath("$[0].titleCy").value("Rydych wedi dweud eich bod yn dymuno parhau â’ch hawliad"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Bydd eich achos yn cael ei gyfeirio at y gwasanaeth cyfryngu. " +
                            "Bydd eich apwyntiad cyfryngu yn cael ei drefnu o fewn 28 diwrnod.</p>" +
                            "<p class=\"govuk-body\"><a href=\"https://www.gov.uk/guidance/small-claims-mediation-service\"  " +
                            "rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\"> " +
                            "Rhagor o wybodaeth am sut mae cyfryngu yn gweithio (yn agor mewn tab newydd)</a>.</p>")
            );
    }
}
