package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;

public class GoToHearingScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_go_to_hearing_scenario() throws Exception {

        String caseId = "51234567891";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
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
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">You have rejected Mr. Sole Trader's response. The case will be referred to a judge who will decide what should happen next.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">View the defendant's response</a></p>"),
                jsonPath("$[0].titleCy").value("Aros i’r llys adolygu’r achos"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Rydych wedi gwrthod ymateb Mr. Sole Trader. Bydd yr achos yn cael ei gyfeirio at farnwr a fydd yn penderfynu beth ddylai ddigwydd nesaf.</p><p class=\"govuk-body\"><a href=\"{VIEW_RESPONSE_TO_CLAIM}\"  rel=\"noopener noreferrer\" class=\"govuk-link\">Gweld ymateb y diffynnydd</a></p>")
            );
    }
}
