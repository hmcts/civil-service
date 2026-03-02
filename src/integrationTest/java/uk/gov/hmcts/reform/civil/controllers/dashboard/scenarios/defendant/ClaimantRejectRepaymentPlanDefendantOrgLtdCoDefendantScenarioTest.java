package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantRejectRepaymentPlanDefendantOrgLtdCoDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_scenario_for_partl_admit_reject_repaymentPlan_scenario() throws Exception {

        String caseId = "50211";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .respondent1(Party.builder()
                        .companyName("Company one")
                        .type(Party.Type.COMPANY).build())
            .applicant1(Party.builder()
                        .companyName("Company one")
                        .type(Party.Type.COMPANY).build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000))
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The court will review the details and issue a judgment"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Company one has rejected your payment plan, the court will issue a County Court Judgment (CCJ)."
                          +  " If you do not agree with the judgment, you can send in your financial details and ask for this to be redetermined. "
                          +  "Your online account will not be updated - any further updates will be by post.</p><p class=\"govuk-body\">Email the details and your claim number"
                          +  " reference to {cmcCourtEmailId} or send by post to: </p><br>{cmcCourtAddress}"),
                jsonPath("$[0].titleCy").value("Bydd y llys yn adolygu’r manylion ac yn cyhoeddi dyfarniad"),
                jsonPath("$[0].descriptionCy").value(
                        "<p class=\"govuk-body\">Mae Company one wedi gwrthod eich cynllun talu, a bydd y llys yn " +
                            "cyhoeddi Dyfarniad Llys Sirol (CCJ). Os nad ydych yn cytuno â’r dyfarniad, " +
                            "gallwch anfon eich manylion ariannol i’r llys a gofyn am ailbenderfyniad." +
                            " Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru - bydd unrhyw ddiweddariadau pellach yn cael eu hanfon drwy’r post." +
                            "</p><p class=\"govuk-body\">Anfonwch y manylion a rhif eich hawliad reference ar e-bost i {cmcCourtEmailId} neu postiwch yr wybodaeth i: </p>" +
                            "<br>{cmcCourtAddress}"));
    }

    @Test
    void should_create_scenario_for_full_admit_reject_repaymentPlan_scenario() throws Exception {

        String caseId = "50125";
        LocalDate firstRepaymentDate = OffsetDateTime.now().toLocalDate();

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .respondent1Represented(YesOrNo.NO)
                .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                        .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                        .setPaymentAmount(new BigDecimal(1000))
                        .setFirstRepaymentDate(LocalDate.now())
                        )
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000))
                .applicant1(Party.builder()
                        .organisationName("Applicant Org")
                        .type(Party.Type.ORGANISATION).build())
                .respondent1(Party.builder()
                        .organisationName("Org one")
                        .type(Party.Type.ORGANISATION).build())
                .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
                .totalClaimAmount(new BigDecimal(5000))
                .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
                .andExpect(status().isOk())
                .andExpectAll(
                        status().is(HttpStatus.OK.value()),
                        jsonPath("$[0].titleEn").value("The court will review the details and issue a judgment"),
                        jsonPath("$[0].descriptionEn").value(
                                "<p class=\"govuk-body\">Applicant Org has rejected your payment plan, the court will issue a County Court Judgment (CCJ)."
                                        +  " If you do not agree with the judgment, you can send in your financial details and ask for this to be redetermined. "
                                        +  "Your online account will not be updated - any further updates will be by post.</p>" +
                                    "<p class=\"govuk-body\">Email the details and your claim number"
                                        +  " reference to {cmcCourtEmailId} or send by post to: </p><br>{cmcCourtAddress}"),
                        jsonPath("$[0].titleCy").value("Bydd y llys yn adolygu’r manylion ac yn cyhoeddi dyfarniad"),
                        jsonPath("$[0].descriptionCy").value(
                                "<p class=\"govuk-body\">Mae Applicant Org wedi gwrthod eich cynllun talu, a bydd y llys yn cyhoeddi Dyfarniad " +
                                    "Llys Sirol (CCJ). Os nad ydych yn cytuno â’r dyfarniad, gallwch anfon eich manylion ariannol i’r llys a gofyn am ailbenderfyniad." +
                                    " Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru - bydd unrhyw ddiweddariadau pellach yn cael eu hanfon drwy’r " +
                                    "post.</p>" +
                                    "<p class=\"govuk-body\">Anfonwch y manylion a rhif eich hawliad reference ar e-bost i {cmcCourtEmailId} neu postiwch yr wybodaeth i: </p>" +
                                    "<br>{cmcCourtAddress}"));
    }
}
