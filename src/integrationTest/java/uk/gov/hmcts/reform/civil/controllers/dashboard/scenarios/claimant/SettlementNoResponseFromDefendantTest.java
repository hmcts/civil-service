package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.SettlementNoResponseFromDefendantEvent;
import uk.gov.hmcts.reform.civil.handler.event.SettlementNoResponseFromDefendantEventHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SettlementNoResponseFromDefendantTest extends DashboardBaseIntegrationTest {

    @Autowired
    private SettlementNoResponseFromDefendantEventHandler handler;
    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Test
    void should_create_settlement_no_response_from_defendant() throws Exception {

        String caseId = "10002349";
        LocalDate firstPaymentDate = LocalDate.now();
        PaymentFrequencyLRspec frequency = PaymentFrequencyLRspec.ONCE_FOUR_WEEKS;
        BigDecimal installmentAmount = new BigDecimal("100");
        BigDecimal totalAmount = new BigDecimal("10000");
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("Defendant")
                             .individualTitle("Mr").individualLastName("person").build())
            .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                                          .setRepaymentFrequency(frequency)
                                          .setPaymentAmount(installmentAmount)
                                          .setFirstRepaymentDate(firstPaymentDate)
                                          )
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .totalClaimAmount(totalAmount)
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).id(Long.valueOf(caseId)).build();

        when(coreCaseDataService.getCase(Long.valueOf(caseId))).thenReturn(caseDetails);
        when(userService.getAccessToken(any(), any())).thenReturn(BEARER_TOKEN);

        handler.createClaimantDashboardScenario(new SettlementNoResponseFromDefendantEvent(Long.valueOf(caseId)));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "Mr Defendant person has not signed your settlement agreement"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You can <a href=\"{COUNTY_COURT_JUDGEMENT_URL}\" rel=\"noopener noreferrer\" class=\"govuk-link\">request a County Court Judgment (CCJ)</a>, based on the repayment plan shown in the agreement.</p> <p class=\"govuk-body\">The court will make an order requiring them to pay the money. The order does not guarantee that they will pay it.</p> <p class=\"govuk-body\">Mr Defendant person can still sign the settlement agreement until you request a CCJ.</p>"),
            jsonPath("$[0].titleCy").value(
                "Nid yw Mr Defendant person wedi llofnodi eich cytundeb setlo"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Gallwch <a href=\"{COUNTY_COURT_JUDGEMENT_URL}\" rel=\"noopener noreferrer\" class=\"govuk-link\">wneud cais am Ddyfarniad Llys Sirol (CCJ)</a>, yn seiliedig ar y cynllun ad-dalu sydd wedi’i nodi yn y cytundeb.</p> <p class=\"govuk-body\">Bydd y llys yn gwneud gorchymyn yn gofyn iddynt dalu’r arian. Ni fydd y gorchymyn yn gwarantu y byddant yn ei dalu.</p> <p class=\"govuk-body\">Gall Mr Defendant person dal llofnodi’r cytundeb setlo hyd nes y byddwch yn gwneud cais am CCJ.</p>")

        );
    }
}


