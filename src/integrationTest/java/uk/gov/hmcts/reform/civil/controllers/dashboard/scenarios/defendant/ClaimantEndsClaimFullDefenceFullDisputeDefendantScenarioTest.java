package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ClaimantResponseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class ClaimantEndsClaimFullDefenceFullDisputeDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_claimant_settled_claim_scenario() throws Exception {

        String caseId = "12346789121";

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder()
            .applicant1(Party.builder().companyName("Test Company").type(Party.Type.COMPANY).build())
            .respondent1(Party.builder().companyName("Respondent Company").type(Party.Type.COMPANY).build())
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(NO)
            .applicant1ProceedWithClaim(NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM)
            .ccdState(CaseState.CASE_STAYED)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The claim has now ended"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Test Company has decided not to proceed with the claim.</p>"),
                jsonPath("$[0].titleCy").value("Mae’r hawliad wedi dod i ben"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae Test Company wedi penderfynu peidio â bwrw ymlaen gyda’r hawliad.</p>")
            );
    }
}
