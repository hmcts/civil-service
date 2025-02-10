package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DefendantSignSettlementAgreementDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantRejectedSettlementAgreementDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantSignSettlementAgreementDashboardNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_defendant_rejected_settlement_agreement_scenario() throws Exception {

        String caseId = "90123456784";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .caseDataLiP(
                CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO
                ).build()
            )
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value("Settlement agreement"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">Mr. John Rambo can request a County Court Judgment (CCJ), " +
                    "which would order you to repay the money in line with the agreement. " +
                    "The court believes you can afford this.</p> " +
                    "<p class=\"govuk-body\">If the claimant requests a CCJ then you can ask a judge " +
                    "to consider changing the plan, based on your financial details.</p>"),
            jsonPath("$[0].titleCy").value("Cytundeb setlo"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Gall Mr. John Rambo wneud cais am Ddyfarniad Llys Sirol (CCJ), " +
                    "a fyddai’n gorchymyn eich bod yn ad-dalu’r arian yn unol â’r cytundeb. Mae’r llys yn credu " +
                    "y gallwch fforddio hyn.</p>" +
                    " <p class=\"govuk-body\"> Os bydd yr hawlydd yn gwneud cais am CCJ yna gallwch ofyn i " +
                    "farnwr ystyried newid y cynllun, yn seiliedig ar eich manylion ariannol.</p>")
        );
    }

}
