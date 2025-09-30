package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantRejectRepaymentPlanOrgLtdCoDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseNotificationHandler handler;

    @Test
    void should_create_part_admit_pay_by_setDate_judgement_online_scenario() throws Exception {

        String caseId = "50399";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder()
                             .companyName("Company one")
                             .type(Party.Type.COMPANY).build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000))
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The court will review the details and issue a judgment"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You have rejected the defendant's payment plan, the court will issue a County Court Judgment (CCJ)."
                        + " If you do not agree with the judgment, you can send in the defendant's financial details and ask for this to be redetermined."
                        + "</p><p class=\"govuk-body\">Email the details and your claim number"
                        + " reference to {cmcCourtEmailId} or send by post to: </p><br>{cmcCourtAddress}"),
                jsonPath("$[0].titleCy").value("Bydd y llys yn adolygu’r manylion ac yn cyhoeddi dyfarniad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych wedi gwrthod cynllun talu’r diffynnydd a bydd y llys yn cyhoeddi Dyfarniad Llys Sirol (CCJ). " +
                        "Os nad ydych yn cytuno â’r dyfarniad, gallwch anfon manylion ariannol y diffynnydd i’r llys a gofyn am ailbenderfyniad." +
                        "</p><p class=\"govuk-body\">Anfonwch y manylion a rhif eich hawliad reference ar e-bost i {cmcCourtEmailId} neu postiwch yr wybodaeth i: </p>" +
                        "<br>{cmcCourtAddress}")
            );
    }

}
