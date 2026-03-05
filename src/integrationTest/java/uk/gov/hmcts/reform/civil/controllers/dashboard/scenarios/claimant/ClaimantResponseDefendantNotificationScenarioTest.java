package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class ClaimantResponseDefendantNotificationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void shouldCreateNotification_forDefendantWhenClaimantProceedsCarm() throws Exception {
        String caseId = String.valueOf(System.currentTimeMillis());
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(NO)
            .ccdState(CaseState.IN_MEDIATION)
            .build();

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
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
                        "<p class=\"govuk-body\">Mae eich hawliad nawr yn mynd i gyfryngu."
                            + " Byddwn yn cysylltu â chi o fewn 28 diwrnod gyda manylion am eich apwyntiad. "
                            + "<br> Os na fyddwch yn mynychu’ch apwyntiad cyfryngu, efallai y bydd y barnwr yn eich cosbi.</p>"));
    }

    @Test
    void shouldCreateNotification_forDefendantWhenClaimantProceeds() throws Exception {
        String caseId = String.valueOf(System.currentTimeMillis());
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(NO)
            .ccdState(CaseState.IN_MEDIATION)
            .build();

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mr. John Rambo rejected your response"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">Your case will be referred for mediation. Your mediation appointment will be arranged within 28 days.</p>" +
                            "<p class=\"govuk-body\"><a href=\"{MEDIATION}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">Find out more about how mediation works (opens in a new tab)</a>.<p/>" +
                            "<p class=\"govuk-body\">They've also sent us their hearing requirements. " +
                            "<a href=\"{VIEW_CLAIMANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">You can view their hearing requirements here (opens in new tab)</a>.</p>"),
                jsonPath("$[0].titleCy").value("Mae Mr. John Rambo wedi gwrthod eich ymateb"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Bydd eich achos yn cael ei gyfeirio at y gwasanaeth cyfryngu." +
                            " Bydd eich apwyntiad cyfryngu yn cael ei drefnu o fewn 28 diwrnod.</p><p class=\"govuk-body\">" +
                            "<a href=\"{MEDIATION}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">Rhagor " +
                            "o wybodaeth am sut mae cyfryngu yn gweithio (yn agor mewn tab newydd)</a>.<p/>" +
                            "<p class=\"govuk-body\">Maent hefyd wedi anfon atom eu gofynion ar gyfer y gwrandawiad. " +
                            "<a href=\"{VIEW_CLAIMANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\" " +
                            "target=\"_blank\">Gallwch weld eu gofynion ar gyfer y gwrandawiad yma (yn agor mewn tab newydd)</a>.</p>")
            );
    }

    @Test
    void shouldCreateNotification_forDefendantWhenLRvLipFullAdmitPayImmediately() throws Exception {
        String caseId = String.valueOf(System.currentTimeMillis());
        LocalDate whenWillThisAmountBePaid = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .totalClaimAmount(BigDecimal.valueOf(9000))
            .respondent1Represented(NO)
            .applicant1Represented(YES)
            .applicant1ProceedWithClaim(null)
            .applicant1(Party.builder().individualFirstName("John").individualLastName("White").type(Party.Type.INDIVIDUAL).build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(whenWillThisAmountBePaid)
                                               )
            .build();

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Immediate payment"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">John White has accepted your offer to pay £9001, plus the claim fee and any fixed costs claimed, immediately" +
                        " in full and final settlement of the claim. Funds must be received in <a href={VIEW_INFO_ABOUT_CLAIMANT} class=\"govuk-link\">their account</a>" +
                        " by " + DateUtils.formatDate(whenWillThisAmountBePaid) + ".</p><p class=\"govuk-body\">If they don't receive the money by then," +
                        " they can request a County Court Judgment (CCJ).</p>"),
                jsonPath("$[0].titleCy").value("Talu ar unwaith"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae John White wedi derbyn eich cynnig i dalu £9001, ynghyd â ffi’r hawliad ac unrhyw gostau sefydlog a hawlir, " +
                        "yn llawn ar unwaith fel setliad llawn a therfynol o’r hawliad.  Rhaid i’r arian fod yn <a href={VIEW_INFO_ABOUT_CLAIMANT} class=\"govuk-link\">ei g/chyfrif</a> erbyn "
                        + DateUtils.formatDateInWelsh(whenWillThisAmountBePaid, false) + ".</p><p class=\"govuk-body\"> Os na fydd yr arian wedi cyrraedd erbyn hynny," +
                        " gallant ofyn am Ddyfarniad Llys Sirol (CCJ).</p>"));
    }
}
