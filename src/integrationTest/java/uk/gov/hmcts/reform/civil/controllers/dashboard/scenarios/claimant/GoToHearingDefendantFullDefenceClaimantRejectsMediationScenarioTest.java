package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;

public class GoToHearingDefendantFullDefenceClaimantRejectsMediationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_go_to_hearing_scenario() throws Exception {

        String caseId = "5123456789";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .respondent1Represented(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(CaseDataLiP.builder().applicant1ClaimMediationSpecRequiredLip(
                ClaimantMediationLip.builder().hasAgreedFreeMediation(MediationDecision.No).build()).build())
            .build();

        // When
        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">Mr. John Rambo wants to proceed with the claim.</p> <p class=\"govuk-body\">They said no to mediation.</p> " +
                            "<p class=\"govuk-body\">The case will be referred to a judge who will decide what should happen next.</p> <p class=\"govuk-body\">You can <a href={VIEW_DEFENDANT_RESPONSE} class=\"govuk-link\">view your" +
                            " response</a> or <a target=\"_blank\" href={VIEW_CLAIMANT_HEARING_REQS} rel=\"noopener noreferrer\" class=\"govuk-link\">view the claimant's hearing requirements</a>.</p>"),
                jsonPath("$[0].titleCy").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Mr. John Rambo wants to proceed with the claim.</p> <p class=\"govuk-body\">They said no to mediation.</p>" +
                            " <p class=\"govuk-body\">The case will be referred to a judge who will decide what should happen next.</p> <p class=\"govuk-body\">You can" +
                            " <a href={VIEW_DEFENDANT_RESPONSE} class=\"govuk-link\">view your response</a> or <a target=\"_blank\" href={VIEW_CLAIMANT_HEARING_REQS} rel=\"noopener noreferrer\" class=\"govuk-link\">view the claimant's hearing requirements</a>.</p>")
            );
    }
}
