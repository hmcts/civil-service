package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DefendantSignSettlementAgreementDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantAcceptSettlementAgreementDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantSignSettlementAgreementDashboardNotificationHandler handler;

    @Test
    void should_create_scenario_for_defendant_accept_defendant_plan_settlement_agreement() throws Exception {

        String caseId = "1234899102";
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .caseDataLiP(
                CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.YES
                ).build()
            )
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .totalClaimAmount(new BigDecimal(10000))
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
                        "<p class=\"govuk-body\">You have accepted the "
                            + "<a href={VIEW_SETTLEMENT_AGREEMENT} target=\"_blank\" class=\"govuk-link\"> settlement"
                            + " agreement (opens in a new tab)</a>. The claimant cannot request a County Court "
                            + "Judgment (CCJ), unless you break the terms of the agreement.</p>"),
                jsonPath("$[0].titleCy").value("Cytundeb setlo"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Rydych wedi derbyn y <a href={VIEW_SETTLEMENT_AGREEMENT} target=\"_blank\" class=\"govuk-link\"> cytundeb setlo (yn agor mewn tab newydd)</a>." +
                            " Ni all yr hawlydd wneud cais am Ddyfarniad Llys Sirol (CCJ) oni bai eich bod yn torri telerau’r cytundeb.</p>")
            );
    }
}
