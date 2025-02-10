package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimSettledDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantSettledTheClaimScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimSettledDashboardNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_claimant_settled_claim_scenario() throws Exception {

        String caseId = "55551111";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1ClaimSettledDate(LocalDate.of(2024, 03, 19))
                                                        .build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The claim is settled"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You have confirmed that the claim against Mr. Sole Trader was settled on 19 March 2024.</p>"
                        + "<p class=\"govuk-body\">The defendant has 19 days from the date of settlement to notify the court of any objection.</p>"),
                jsonPath("$[0].titleCy").value("Mae’r hawliad wedi’i setlo"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">You have confirmed that the claim against Mr. Sole Trader was settled on 19 March 2024.</p>"
                        + "<p class=\"govuk-body\">The defendant has 19 days from the date of settlement to notify the court of any objection.</p>")
            );
    }
}
