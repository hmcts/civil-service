package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantRequestedJudgeToMakeNewPlanScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantResponseNotificationHandler handler;

    @Test
    void should_create_claimant_requested_judgment_by_admission() throws Exception {
        String caseId = "456567";

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder().applicant1ChoosesHowToProceed(
                                 ChooseHowToProceed.REQUEST_A_CCJ).claimantResponseOnCourtDecision(
                                 ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE).build()).build())
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).individualTitle("Mr")
                            .individualFirstName("Claimant").build())
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
                "<p class=\"govuk-body\">You rejected the <a href=\"{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}\" class=\"govuk-link\">repayment plan</a>.</p><p class=\"govuk-body\">When a judge has made a decision, we’ll post a copy of the judgment to you.</p>"),
            jsonPath("$[0].titleCy").value(
                "Rydych wedi gwneud cais am Ddyfarniad Llys Sirol yn erbyn Mr Defendant Guy"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Rydych wedi gwrthod y <a href=\"{VIEW_CCJ_REPAYMENT_PLAN_CLAIMANT}\" class=\"govuk-link\">cynllun ad-dalu</a>.</p><p class=\"govuk-body\">Pan fydd barnwr wedi gwneud penderfyniad, byddwn yn anfon copi o’r dyfarniad drwy’r post atoch chi.</p>")
        );
    }
}


