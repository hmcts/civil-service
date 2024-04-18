package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantMediationSuccessfulDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantMediationSuccessfulDashboardNotificationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantMediationSuccessfulDashboardNotificationHandler handler;

    @Test
    void should_create_mediation_scenario_for_carm() throws Exception {

        String caseId = String.valueOf(System.currentTimeMillis());
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mediation appointment successful"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">Both parties attended mediation and an agreement was reached.</p> "
                            + "<p class=\"govuk-body\">This case is now settled and no further action is needed.</p> "
                            + "<p class=\"govuk-body\">You can view your mediation agreement <a href=\"{MEDIATION_SUCCESSFUL_URL}\" "
                            + "rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">here</a>.</p>"),
                jsonPath("$[0].titleCy").value("Mediation appointment successful"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Both parties attended mediation and an agreement was reached.</p> "
                            + "<p class=\"govuk-body\">This case is now settled and no further action is needed.</p> "
                            + "<p class=\"govuk-body\">You can view your mediation agreement <a href=\"{MEDIATION_SUCCESSFUL_URL}\" "
                            + "rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">here</a>.</p>")
            );
    }

    @Test
    void should_create_mediation_scenario() throws Exception {

        String caseId = String.valueOf(System.currentTimeMillis());
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("You settled the claim through mediation"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">You made an agreement which means the claim is now ended and sets out the terms of how Mr. Sole Trader must repay you.</p><p class=\"govuk-body\"><a href=\"{MEDIATION_SUCCESSFUL_URL}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">Download the agreement</a></p>"),
                jsonPath("$[0].titleCy").value("You settled the claim through mediation"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">You made an agreement which means the claim is now ended and sets out the terms of how Mr. Sole Trader must repay you.</p><p class=\"govuk-body\"><a href=\"{MEDIATION_SUCCESSFUL_URL}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">Download the agreement</a></p>")
            );
    }
}
