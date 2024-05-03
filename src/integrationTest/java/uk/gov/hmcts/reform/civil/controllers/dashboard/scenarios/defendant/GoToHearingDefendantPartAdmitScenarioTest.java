package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GoToHearingDefendantPartAdmitScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_go_to_hearing_scenario_defendant_part_admit() throws Exception {

        String caseId = "90123456783";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(700))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">Mr.Claimant wants to proceed with the claim.</p>" +
                               "<p class=\"govuk-body\">They rejected your admission of £700.</p>" +
                               "<p class=\"govuk-body\">The case will be referred to a judge who will decide what should happen next.</p>" +
                               "<p class=\"govuk-body\">You can <a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">view your response</a>" +
                               " or <a href={VIEW_CLAIMANT_HEARING_REQS} target=\"_blank\" class=\"govuk-link\">" +
                               "view the claimant's hearing requirements</a>.</p>"),
                jsonPath("$[0].titleCy").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">Mr.Claimant wants to proceed with the claim.</p>" +
                               "<p class=\"govuk-body\">They rejected your admission of £700.</p>" +
                               "<p class=\"govuk-body\">The case will be referred to a judge who will decide what should happen next.</p>" +
                               "<p class=\"govuk-body\">You can <a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">view your response</a>" +
                               " or <a href={VIEW_CLAIMANT_HEARING_REQS} target=\"_blank\" class=\"govuk-link\">" +
                               "view the claimant's hearing requirements</a>.</p>")
            );
    }
}
