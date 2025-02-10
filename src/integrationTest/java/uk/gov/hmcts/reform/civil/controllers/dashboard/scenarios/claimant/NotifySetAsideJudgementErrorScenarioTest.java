package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.NotifySetAsideJudgementDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotifySetAsideJudgementErrorScenarioTest extends DashboardBaseIntegrationTest {

    public static final String CLAIMANT = "CLAIMANT";

    @Autowired
    private NotifySetAsideJudgementDashboardNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_scenario_for_nofity_set_aside_judgement() throws Exception {

        String caseId = "720111";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .joSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR)
            .build();

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(featureToggleService.isGeneralApplicationsEnabled()).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, CLAIMANT)
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The judgment against the defendant has been set aside (removed)"),
                jsonPath("$[0].descriptionEn").value("<p class=\"govuk-body\">You’ll receive an update with information about next steps.</p>"),
                jsonPath("$[0].titleCy").value("Mae’r dyfarniad yn erbyn y diffynnydd wedi cael ei roi o’r naill du (wedi’i ddileu)"),
                jsonPath("$[0].descriptionCy").value("<p class=\"govuk-body\">Byddwch yn cael diweddariad gyda gwybodaeth am y camau nesaf.</p>")
            );
    }
}
