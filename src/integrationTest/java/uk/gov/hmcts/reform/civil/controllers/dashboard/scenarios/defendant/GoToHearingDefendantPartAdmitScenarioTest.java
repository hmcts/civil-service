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
                    .value("<p class=\"govuk-body\">Mr. John Rambo wants to proceed to court.</p>" +
                               "<p class=\"govuk-body\">They rejected your admission of £700.</p>" +
                               "<p class=\"govuk-body\">If the case goes to a hearing we will contact you with further details.</p>" +
                               "<p class=\"govuk-body\"><a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View your response</a>" +
                               "<br><a href={VIEW_CLAIMANT_HEARING_REQS} target=\"_blank\" class=\"govuk-link\">" +
                               "View the claimant's hearing requirements</a></p>"),
                jsonPath("$[0].titleCy").value("Wait for the court to review the case"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">Mr. John Rambo wants to proceed to court.</p>" +
                               "<p class=\"govuk-body\">They rejected your admission of £700.</p>" +
                               "<p class=\"govuk-body\">If the case goes to a hearing we will contact you with further details.</p>" +
                               "<p class=\"govuk-body\"><a href={VIEW_RESPONSE_TO_CLAIM} class=\"govuk-link\">View your response</a>" +
                               "<br><a href={VIEW_CLAIMANT_HEARING_REQS} target=\"_blank\" class=\"govuk-link\">" +
                               "View the claimant's hearing requirements</a></p>")
            );
    }
}
