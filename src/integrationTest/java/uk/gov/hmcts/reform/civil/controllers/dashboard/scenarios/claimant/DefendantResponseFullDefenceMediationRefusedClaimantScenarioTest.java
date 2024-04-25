package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Random;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class DefendantResponseFullDefenceMediationRefusedClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseClaimantNotificationHandler handler;

    @Test
    void should_create_full_defence_mediation_refused_response_scenario() throws Exception {

        String caseId = "12345678901";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1ResponseDeadline(responseDeadline.atStartOfDay())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ResponseDeadline(LocalDateTime.of(2024, 3, 21, 16, 0))
            .responseClaimMediationSpecRequired(NO)
            .applicant1Represented(NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The defendant has rejected the claim and refused mediation.<br>You need to respond by 21 March 2024.<br><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a></p>"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">The defendant has rejected the claim and refused mediation.<br>You need to respond by 21 Mawrth 2024.<br><a href=\"{CLAIMANT_RESPONSE_TASK_LIST}\" class=\"govuk-link\">View and respond</a></p>")
            );
    }
}
