package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class ClaimantEndsClaimFullDefenceFullDisputeDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    void should_create_claimant_settled_claim_scenario() throws Exception {

        String caseId = "1234678912";
        LocalDateTime respondent1SettlementDeadline = LocalDateTime.now().plusDays(7);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimantFullDefence().build()
            .toBuilder()
            .applicant1(Party.builder().partyName("Mr Claimant person").build())
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1ProceedWithClaim(NO)
            .respondent1ClaimResponseTestForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired("DISPUTES_THE_CLAIM")
            .ccdState(CaseState.CASE_DISMISSED)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The claim has now ended"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr Claimant person has decided not to proceed with the claim.</p>"),
                jsonPath("$[0].titleCy").value("Mae’r hawliad wedi dod i ben"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae Mr Claimant person wedi penderfynu peidio â bwrw ymlaen gyda’r hawliad.</p>")
            );
    }
}
