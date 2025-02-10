package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.DefendantMediationSuccessfulDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimIntentMediationSuccessfulDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantMediationSuccessfulDashboardNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_mediation_successful_scenario() throws Exception {

        String caseId = String.valueOf(System.currentTimeMillis());
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .respondent1Represented(YesOrNo.NO)
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You settled the claim through mediation"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">You made an agreement which means the claim is now ended and sets out the terms of how you must repay Mr. John Rambo.</p> <p class=\"govuk-body\"><a href=\"{MEDIATION_SUCCESSFUL_URL}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">Download the agreement (opens in a new tab)</a></p> <p class=\"govuk-body\"><a href=\"{CITIZEN_CONTACT_THEM_URL}\" rel=\"noopener noreferrer\" class=\"govuk-link\">Contact Mr. John Rambo</a> if you need their payment details. Make sure you get receipts for any payments.</p>"),
                jsonPath("$[0].titleCy").value("Rydych wedi setlo’r hawliad drwy gyfryngu"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Mi wnaethoch gytundeb sy’n golygu bod yr hawliad nawr ar ben. Mae’r " +
                            "cytundeb yn nodi’r telerau ar gyfer sut mae rhaid i chi ad-dalu Mr. John Rambo.</p> " +
                            "<p class=\"govuk-body\"><a href=\"{MEDIATION_SUCCESSFUL_URL}\" target=\"_blank\" rel=\"noopener noreferrer\" " +
                            "class=\"govuk-link\">Lawrlwytho’r cytundeb (yn agor mewn tab newydd)</a></p> <p class=\"govuk-body\">" +
                            "<a href=\"{CITIZEN_CONTACT_THEM_URL}\" rel=\"noopener noreferrer\" class=\"govuk-link\">Cysylltwch â " +
                            "Mr. John Rambo</a> ios oes arnoch angen eu manylion talu. Gwnewch yn siŵr eich bod yn cael derbynebau am unrhyw daliadau.</p>")
            );
    }
}
