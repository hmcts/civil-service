package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;

public class PartAdmitImmediatelyClaimSettledScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_scenario_for_part_admit_immediate_accepted() throws Exception {

        String caseId = "90123456781";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .applicant1AcceptPartAdmitPaymentPlanSpec(null)
            .respondent1Represented(YesOrNo.NO)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(100))
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(LocalDate.of(2024, 1, 1))
                                               )
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Immediate payment"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">Mr. John Rambo has accepted your offer to pay £100, plus the claim fee and any fixed costs claimed, " +
                            "immediately in full and final settlement of the claim. " +
                            "Funds must be received in <a href={VIEW_INFO_ABOUT_CLAIMANT} class=\"govuk-link\">their account</a> by 1 January 2024.</p><p class=\"govuk-body\">" +
                            "If they don't receive the money by then, they can request a County Court Judgment (CCJ).</p>"),
                jsonPath("$[0].titleCy").value("Talu ar unwaith"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Mae Mr. John Rambo wedi derbyn eich cynnig i dalu £100, ynghyd â ffi’r hawliad ac unrhyw gostau sefydlog a hawlir, yn " +
                            "llawn ar unwaith fel setliad llawn a therfynol o’r hawliad.  Rhaid i’r arian fod yn " +
                            "<a href={VIEW_INFO_ABOUT_CLAIMANT} class=\"govuk-link\">ei g/chyfrif</a> erbyn 1 Ionawr 2024.</p>" +
                            "<p class=\"govuk-body\"> Os na fydd yr arian wedi cyrraedd erbyn hynny, " +
                            "gallant ofyn am Ddyfarniad Llys Sirol (CCJ).</p>")
            );
    }
}
