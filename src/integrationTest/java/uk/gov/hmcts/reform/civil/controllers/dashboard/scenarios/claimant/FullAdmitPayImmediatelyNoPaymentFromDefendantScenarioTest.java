package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEvent;
import uk.gov.hmcts.reform.civil.handler.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEventHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FullAdmitPayImmediatelyNoPaymentFromDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private FullAdmitPayImmediatelyNoPaymentFromDefendantEventHandler handler;
    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Test
    void should_create_full_admit_pay_immediately_no_payment_scenario() throws Exception {
        String caseId = "1234678912136";
        LocalDate whenWillThisAmountBePaid = LocalDate.now().plusDays(5);
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build().toBuilder()
            .ccdCaseReference(Long.valueOf(caseId))
            .totalClaimAmount(BigDecimal.valueOf(124.67))
            .applicant1(Party.builder().individualFirstName("Dave").individualLastName("Indent").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualFirstName("Dave").individualLastName("Indent").type(Party.Type.INDIVIDUAL).build())
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                               .whenWillThisAmountBePaid(whenWillThisAmountBePaid)
                                               .build())
            .build();

        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).id(Long.valueOf(caseId)).build();

        when(coreCaseDataService.getCase(Long.valueOf(caseId))).thenReturn(caseDetails);
        when(userService.getAccessToken(any(), any())).thenReturn(BEARER_TOKEN);

        handler.createClaimantDashboardScenario(new FullAdmitPayImmediatelyNoPaymentFromDefendantEvent(Long.valueOf(
            caseId)));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value("Immediate payment"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You have accepted Dave Indent's plan to pay £{fullAdmitPayImmediatelyPaymentAmount} "
                    + "immediately. Funds must clear your account by " + DateUtils.formatDate(whenWillThisAmountBePaid) + ".</p><p class=\"govuk-body\">If you don't receive"
                    + " the money by then, you can <a href=\"{COUNTY_COURT_JUDGEMENT_URL}\"  "
                    + "rel=\"noopener noreferrer\" class=\"govuk-link\">request a County Court Judgment</a>.</p>"),
            jsonPath("$[0].titleCy").value("Immediate payment"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">You have accepted Dave Indent's plan to pay £{fullAdmitPayImmediatelyPaymentAmount} "
                    + "immediately. Funds must clear your account by " + DateUtils.formatDateInWelsh(
                    whenWillThisAmountBePaid) + ".</p><p class=\"govuk-body\">If you don't receive "
                    + "the money by then, you can <a href=\"{COUNTY_COURT_JUDGEMENT_URL}\" "
                    + " rel=\"noopener noreferrer\" class=\"govuk-link\">request a County Court Judgment</a>.</p>")

        );
    }

}
