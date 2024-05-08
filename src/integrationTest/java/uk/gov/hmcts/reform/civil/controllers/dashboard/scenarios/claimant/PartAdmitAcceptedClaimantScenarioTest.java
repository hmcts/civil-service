package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PartAdmitAcceptedClaimantScenarioTest extends DashboardBaseIntegrationTest {

    private static final String GET_NOTIFICATIONS_URL
        = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";

    @Autowired
    private ClaimantResponseNotificationHandler handler;

    @Test
    void should_create_scenario_for_part_admit_accepted_claimant() throws Exception {

        String caseId = "712345678";

        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .applicant1AcceptPartAdmitPaymentPlanSpec(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(500))
            .build().toBuilder()
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                               .whenWillThisAmountBePaid(LocalDate.of(2024, 3, 19))
                                               .build())
            .ccdCaseReference(Long.valueOf(caseId)).ccdState(CaseState.CASE_SETTLED).build();

        // When
        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Immediate payment"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr. Sole Trader has said that they will pay you £500 immediately in full and final settlement of your claim and you have accepted this offer. Funds must be received in your account by 19 March 2024.</p> <p class=\"govuk-body\">If you don´t receive the money by then, you can <a href={COUNTY_COURT_JUDGEMENT_URL} class=\"govuk-link\">request a County Court Judgment(CCJ)</a>.</p>")
            );
    }
}
