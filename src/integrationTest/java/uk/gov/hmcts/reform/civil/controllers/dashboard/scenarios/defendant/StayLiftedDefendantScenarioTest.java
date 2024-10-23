package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.StayLiftedClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.StayLiftedDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StayLiftedDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private StayLiftedDefendantNotificationHandler handler;

    @Test
    void should_create_stay_lifted_claimant_scenario() throws Exception {

        String caseId = "7123454456455465";

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .respondent1Represented(YesOrNo.NO)
            .build();

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The stay has been lifted"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The stay of these proceedings has been lifted.</p>"),
                jsonPath("$[0].titleCy").value("Mae'r ataliad wedi'i godi"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae'r ataliad ar gyfer yr achos hwn wedi'i godi.</p>")
            );
    }
}
