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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantResponseNotificationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseNotificationHandler handler;

    @Test
    void shouldCreateNotification_forClaimantWhenClaimantProceedsCarm() throws Exception {
        String caseId = "901234567891";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.IN_MEDIATION)
            .build();

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Your claim is now going to mediation"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">Your claim is now going to mediation."
                            + " You will be contacted within 28 days with details of your appointment. "
                            + "<br> If you do not attend your mediation appointment, the judge may issue a penalty.</p>"),
                jsonPath("$[0].titleCy").value("Mae eich hawliad nawr yn mynd i gyfryngu"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Mae eich hawliad nawr yn mynd i gyfryngu. Byddwn yn " +
                            "cysylltu â chi o fewn 28 diwrnod gyda manylion am eich apwyntiad. "
                            + "<br> Os na fyddwch yn mynychu’ch apwyntiad cyfryngu, efallai y bydd y barnwr yn eich cosbi.</p>"));
    }

    @Test
    void shouldCreateNotification_forClaimantWhenClaimantProceeds() throws Exception {
        String caseId = "901234567892";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.IN_MEDIATION)
            .build();

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You have said you wish continue with your claim"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">Your case will be referred for mediation. "
                               + "Your mediation appointment will be arranged within 28 days.</p><p class=\"govuk-body\">"
                               + "<a href=\"https://www.gov.uk/guidance/small-claims-mediation-service\" "
                               + " rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\"> Find out more about how mediation works (opens in new tab)</a>.</p>"),
                jsonPath("$[0].titleCy").value("Rydych wedi dweud eich bod yn dymuno parhau â’ch hawliad"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">Bydd eich achos yn cael ei gyfeirio at y gwasanaeth cyfryngu." +
                               " Bydd eich apwyntiad cyfryngu yn cael ei drefnu o fewn 28 diwrnod.</p><p class=\"govuk-body\">" +
                               "<a href=\"https://www.gov.uk/guidance/small-claims-mediation-service\"  rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">" +
                               " Rhagor o wybodaeth am sut mae cyfryngu yn gweithio (yn agor mewn tab newydd)</a>.</p>"));
    }
}
