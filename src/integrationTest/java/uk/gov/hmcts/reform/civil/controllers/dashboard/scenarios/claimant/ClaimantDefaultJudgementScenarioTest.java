package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefaultJudgementIssuedClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantDefaultJudgementScenarioTest extends DashboardBaseIntegrationTest {

    public static final String CLAIMANT = "CLAIMANT";

    @Autowired
    private DefaultJudgementIssuedClaimantNotificationHandler handler;

    @Test
    void should_create_scenario_for_default_judgement() throws Exception {

        String caseId = "720111";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .build();

        when(featureToggleService.isGeneralApplicationsEnabled()).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, CLAIMANT)
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("A judgment against the defendant has been made"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">The exact details of what you need to pay, and by when, are stated on the judgment. <br> " +
                            "If you want to dispute the judgment, or ask to change how and when you pay back the claim amount, you can " +
                            "<a href=\"{SERVICE_REQUEST_UPDATE}\" class=\"govuk-link\">make an application to set aside (remove) or vary the judgment</a>.</p>"),
                jsonPath("$[0].titleCy").value("A judgment against the defendant has been made"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">The exact details of what you need to pay, and by when, are stated on the judgment. <br> " +
                            "If you want to dispute the judgment, or ask to change how and when you pay back the claim amount, you can " +
                            "<a href=\"{SERVICE_REQUEST_UPDATE}\" class=\"govuk-link\">make an application to set aside (remove) or vary the judgment</a>.</p>")
            );
    }
}
