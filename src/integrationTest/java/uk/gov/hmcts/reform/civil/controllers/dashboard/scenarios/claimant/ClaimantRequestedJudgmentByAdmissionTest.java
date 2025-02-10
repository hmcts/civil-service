package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantCCJResponseNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantRequestedJudgmentByAdmissionTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantCCJResponseNotificationHandler handler;

    @Test
    @DirtiesContext
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
}


