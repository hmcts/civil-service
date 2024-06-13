package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.CaseProgressionDashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HelpWithFeeDashboardNoticeHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HelpWithFeeDashboardNoticeHandlerIntegrationTest  extends CaseProgressionDashboardBaseIntegrationTest {

    @Autowired
    private HelpWithFeeDashboardNoticeHandler handler;

    @Test
    void shouldCreateNotificationScenario() throws Exception {

        String caseId = "12345188432991";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(1000))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("We're reviewing your help with fees application"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You've applied for help with the hearing fee. You'll receive an update in 5 to 10 working days.</p>"
                ),
                jsonPath("$[0].titleCy").value("Rydym yn adolygu eich cais am help i dalu ffioedd"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Fe wnaethoch chi gais am help i dalu ffi'r gwrandawiad. Byddwch yn cael diweddariad mewn 5 i 10 diwrnod gwaith.</p>"
                )
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[0].currentStatusEn").value("In progress")
            );
    }
}
