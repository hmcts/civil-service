package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DefendantSignSettlementAgreementDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest.callbackParams;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DefendantAcceptSettlementAgreementDefendantScenarioTest extends BaseIntegrationTest {
    @Autowired
    private DefendantSignSettlementAgreementDashboardNotificationHandler handler;

    @Test
    void should_create_scenario_for_defendant_accept_defendant_plan_settlement_agreement() throws Exception {
        UUID caseId = UUID.randomUUID();
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.YES
                ).build()
            )
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Settlement agreement"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">You have accepted the  "
                            + "<a href={VIEW_SETTLEMENT_AGREEMENT} target=\"_blank\" class=\"govuk-link\"> settlement"
                            + " agreement</a>. </p><p class=\"govuk-body\">The claimant cannot request a County Court "
                            + "Judgment, unless you break the terms of the agreement.</p>")
            );
    }
}
