package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.StayCaseDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StayCaseDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private StayCaseDefendantNotificationHandler handler;

    @Test
    void should_create_stay_case_defendant_scenario() throws Exception {
        String caseId = "720144638756912";

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build().toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(700))
            .build();
        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The case has been stayed"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The case has been stayed. This could be as a result of a judge’s order."
                        + " Any upcoming hearings will be cancelled.</p>"),
                jsonPath("$[0].titleCy").value("Mae’r achos wedi cael ei atal"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae’r achos wedi’i atal. Gallai hyn fod o ganlyniad i orchymyn a waned gan farnwr."
                        + " Bydd unrhyw wrandawiadau sydd i ddod yn cael eu canslo.</p>")
            );
    }
}
