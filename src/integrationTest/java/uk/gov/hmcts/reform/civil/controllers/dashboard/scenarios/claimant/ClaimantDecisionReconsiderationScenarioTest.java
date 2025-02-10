package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DecisionOnRequestForReconsiderationClaimantHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantDecisionReconsiderationScenarioTest extends DashboardBaseIntegrationTest {

    public static final String CLAIMANT = "CLAIMANT";

    @Autowired
    private DecisionOnRequestForReconsiderationClaimantHandler decisionOnRequestForReconsiderationClaimantHandler;

    @Test
    @DirtiesContext
    void should_create_scenario_for_decision_reconsideration_claimant() throws Exception {

        String caseId = "720111";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        decisionOnRequestForReconsiderationClaimantHandler.handle(callbackParams(caseData));

        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, CLAIMANT)
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Request has been reviewed"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">A judge has made a decision on the order. Carefully " +
                               "<a href=\"{VIEW_DECISION_RECONSIDERATION}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\"> " +
                               "read and review the decision</a>."),
                jsonPath("$[0].titleCy").value("Adolygwyd y cais"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">Mae barnwr wedi gwneud penderfyniad ar y gorchymyn. " +
                               "<a href=\"{VIEW_DECISION_RECONSIDERATION}\" rel=\"noopener noreferrer\" target=\"_blank\" class=\"govuk-link\"> " +
                               "Darllenwch ac adolygwch y penderfyniad</a> yn ofalus.")
            );
    }
}
