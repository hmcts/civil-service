package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantNocDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;

public class NocLipCaseOfflineAllFinalOrdersClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantNocDashboardNotificationHandler handler;

    @Test
    void should_create_noc_lip_offline_scenario() throws Exception {

        String caseId = "72014544355416";

        when(featureToggleService.isLrAdmissionBulkEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);

        JudgmentDetails activeJudgment = JudgmentDetails.builder().judgmentId(123).build();

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(All_FINAL_ORDERS_ISSUED)
            .applicant1Represented(YesOrNo.NO)
            .activeJudgment(activeJudgment)
            .build();

        handler.handle(callbackParamsTest(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mr. Sole Trader has asked for a legal representative to act on their behalf"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr. Sole Trader has asked for a legal representative to act on"
                        + " their behalf. From now on you will need to liaise with their representative.<br>"
                        + "<a href=\"{VIEW_INFO_ABOUT_DEFENDANT}\" class=\"govuk-link\">View the contact details of the defendant's legal representative</a>.<br>"
                        + "This claim will now move offline.</p>"),
                jsonPath("$[0].titleCy").value("Mae Mr. Sole Trader wedi gofyn i gynrychiolydd cyfreithiol weithredu ar eu rhan"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mr. Sole Trader wedi gofyn i gynrychiolydd cyfreithiol"
                        + " weithredu ar eu rhan.  O hyn ymlaen bydd angen i chi gysylltu â'u cynrychiolydd.<br>"
                        + "<a href=\"{VIEW_INFO_ABOUT_DEFENDANT}\" class=\"govuk-link\">Gweld manylion cyswllt cynrychiolydd cyfreithiol y diffynnydd</a>.<br>"
                        + "Bydd yr hawliad hwn nawr yn symud i fod all-lein.</p>")
            );
    }

    private static CallbackParams callbackParamsTest(CaseData caseData) {
        return CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder().eventId(
                CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC.name())
                         .caseDetails(CaseDetails.builder().state(All_FINAL_ORDERS_ISSUED.toString()).build()).build())
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }

}
