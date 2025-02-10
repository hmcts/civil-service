package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DefendantSignSettlementAgreementDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantRejectedSettlementAgreementClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantSignSettlementAgreementDashboardNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_defendant_rejected_settlement_agreement_scenario() throws Exception {

        String caseId = "123467891213";
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO).build())
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1(Party.builder().individualFirstName("Dave").individualLastName("Indent").type(Party.Type.INDIVIDUAL).build())
            .applicant1(Party.builder().individualFirstName("Dave").individualLastName("Indent").type(Party.Type.INDIVIDUAL).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value("Settlement agreement"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">Dave Indent has rejected the settlement agreement."
                    + " You can  <a href={REQUEST_CCJ_URL} class=\"govuk-link\">request a"
                    + " County Court Judgment (CCJ)</a>.</p>"),
            jsonPath("$[0].titleCy").value("Cytundeb setlo"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Mae Dave Indent wedi gwrthod y cytundeb setlo. Gallwch " +
                    "<a href={REQUEST_CCJ_URL} class=\"govuk-link\">wneud cais am Ddyfarniad Llys Sirol (CCJ)</a>.</p>")

        );
    }

}
