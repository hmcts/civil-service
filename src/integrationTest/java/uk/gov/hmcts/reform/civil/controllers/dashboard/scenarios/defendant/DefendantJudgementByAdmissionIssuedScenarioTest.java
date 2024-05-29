package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.JudgementByAdmissionIssuedDefendantDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantJudgementByAdmissionIssuedScenarioTest extends DashboardBaseIntegrationTest {

    public static final String DEFENDANT = "DEFENDANT";

    @Autowired
    private JudgementByAdmissionIssuedDefendantDashboardNotificationHandler handler;

    @Test
    void should_create_scenario_for_judgement_by_admission_issued() throws Exception {

        String caseId = "720111";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .respondent1Represented(YesOrNo.NO)
                .build();

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, DEFENDANT)
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("A judgment has been made against you"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">The judgment formalises the payment plan you’ve agreed with the claimant.<br>" +
                               "You’ve agreed to pay the claim amount of £10000000 bla bla bla.<br>" +
                               "The claimant’s details for payment and the full payment plan can be found on the judgment.<br>" +
                               "If you can no longer afford the repayments you’ve agreed with the claimant, you can <u>make an application to vary the judgment</u>.</p>"),
                jsonPath("$[0].titleCy").value("A judgment has been made against you"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">The judgment formalises the payment plan you’ve agreed with the claimant.<br>" +
                               "You’ve agreed to pay the claim amount of £10000000 bla bla bla.<br>" +
                               "The claimant’s details for payment and the full payment plan can be found on the judgment.<br>" +
                               "If you can no longer afford the repayments you’ve agreed with the claimant, you can <u>make an application to vary the judgment</u>.</p>")
            );
    }
}
