package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DefendantSignSettlementAgreementDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantAcceptsSettlementClaimantNotificationTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantSignSettlementAgreementDashboardNotificationHandler handler;

    @Test
    void should_create_defendant_accepts_settlement_agreement() throws Exception {

        String caseId = "123467891212";
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.YES).build())
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1(Party.builder().individualFirstName("Dave").individualLastName("Indent").type(Party.Type.INDIVIDUAL).build())
            .applicant1(Party.builder().individualFirstName("Dave").individualLastName("Indent").type(Party.Type.INDIVIDUAL).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Settlement agreement"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">Dave Indent has accepted the settlement agreement. You cannot <a href=\"{COUNTY_COURT_JUDGEMENT_URL}\" rel=\"noopener noreferrer\" class=\"govuk-link\">request a County Court Judgment(CCJ)</a>,  unless they break the terms of the agreement.</p> <p class=\"govuk-body\">You can <a href=\"{DOWNLOAD_SETTLEMENT_AGREEMENT}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">view the settlement agreement (opens in a new tab)</a> or <a href=\"{TELL_US_IT_IS_SETTLED}\" rel=\"noopener noreferrer\" class=\"govuk-link\">tell us it's settled</a>.</p>"),
            jsonPath("$[0].titleCy").value(
                "Cytundeb setlo"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Mae Dave Indent wedi derbyn y cytundeb setlo. Ni allwch <a href=\"{COUNTY_COURT_JUDGEMENT_URL}\" rel=\"noopener noreferrer\" class=\"govuk-link\">wneud cais am Ddyfarniad Llys Sirol(CCJ)</a>,  oni bai eu bod yn torri telerau’r cytundeb.</p> <p class=\"govuk-body\">Gallwch <a href=\"{DOWNLOAD_SETTLEMENT_AGREEMENT}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\"> weld y cytundeb setlo (yn agor mewn tab newydd)</a> neu <a href=\"{TELL_US_IT_IS_SETTLED}\" rel=\"noopener noreferrer\" class=\"govuk-link\">ddweud wrthym ei fod wedi’i setlo</a>.</p>")

        );
    }

}
