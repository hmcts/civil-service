package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantCCJResponseNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantRequestedJudgmentByAdmissionTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantCCJResponseNotificationHandler handler;

    @Test
    void should_create_claimant_requested_judgment_by_admission() throws Exception {
        String caseId = "4567";

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).individualTitle("Mr").individualFirstName("Claimant").build())
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).individualTitle("Mr").individualFirstName(
                "Defendant").individualLastName("Guy").build())
            .build();

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "You requested a County Court Judgment against Mr Defendant Guy"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You accepted the <a href=\"{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}\" class=\"govuk-link\">repayment plan</a>. When we've processed the request, we'll post a copy of the judgment to you.</p>"),
            jsonPath("$[0].titleCy").value(
                "Rydych wedi gwneud cais am Ddyfarniad Llys Sirol (CCJ) yn erbyn Mr Defendant Guy"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Rydych wedi derbyn y <a href=\"{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}\" class=\"govuk-link\">cynllun ad-dalu</a>." +
                    " Pan fyddwn wedi prosesu’r cais, byddwn yn anfon copi o’r dyfarniad drwy’r post atoch chi.</p>")
        );
    }

    @Test
    void should_create_claimant_requested_judgment_by_admission_judgement_online() throws Exception {
        String caseId = "4567";

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).individualTitle("Mr").individualFirstName("Claimant").build())
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).individualTitle("Mr").individualFirstName(
                "Defendant").individualLastName("Guy").build())
            .build();

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "You requested a County Court Judgment against Mr Defendant Guy"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You accepted the <a href=\"{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}\" class=\"govuk-link\">repayment plan</a>. When we've processed the request, we'll send you an update by email.</p>"
                + "<p class=\"govuk-body\"><a href=\"{TELL_US_IT_IS_SETTLED}\" rel=\"noopener noreferrer\" class=\"govuk-link\">Tell us it's paid</a></p>"),
            jsonPath("$[0].titleCy").value(
                "Rydych wedi gwneud cais am Ddyfarniad Llys Sirol (CCJ) yn erbyn Mr Defendant Guy"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Rydych wedi derbyn y <a href=\"{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}\" class=\"govuk-link\">cynllun ad-dalu</a>." +
                    " Pan fyddwn wedi prosesu’r cais, byddwn yn anfon diweddariad atoch trwy e-bost.</p>"
                    + "<p class=\"govuk-body\"><a href=\"{TELL_US_IT_IS_SETTLED}\" rel=\"noopener noreferrer\" class=\"govuk-link\">Dywedwch wrthym ei fod wedi’i dalu</a></p>")
        );
    }
}


