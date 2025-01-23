package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantNocOnlineDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantDashboardOnlineNotificationAfterDefendantNoc extends DashboardBaseIntegrationTest {

    @Autowired
    private ClaimantNocOnlineDashboardNotificationHandler claimantNocOnlineDashboardNotificationHandler;

    @Test
    void should_create_online_notification_after_defendant_noc() throws Exception {
        String caseId = "15673456";
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(45500)).build())
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(CaseEvent.FULL_REMISSION_HWF).build())
            .hwfFeeType(FeeType.CLAIMISSUED)
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.YES)
            .build();
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);
        //When
        claimantNocOnlineDashboardNotificationHandler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT").andExpect(status().isOk()).andExpectAll(
            status().is(HttpStatus.OK.value()),
            jsonPath("$[0].titleEn").value(
                "mr defendant has assigned a legal representative to act on their behalf"),
            jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You will now need to liaise with their legal representation.</p><p class=\"govuk-body\"><a href=\"{VIEW_INFO_ABOUT_DEFENDANT}\" class=\"govuk-link\">View the defendant legal representative contact details</a>. </p>"),
            jsonPath("$[0].titleCy").value(
                "Mae mr defendant wedi neilltuo cynrychiolydd cyfreithiol i weithredu ar ei ran"),
            jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">Bydd angen i chi nawr gysylltu â''u cynrychiolaeth gyfreithiol.</p><p class=\"govuk-body\"><a href=\"{VIEW_INFO_ABOUT_DEFENDANT}\" class=\"govuk-link\">Gweld manylion cyswllt cynrychiolydd cyfreithiol y diffynnydd</a>.</p>")
        );
    }
}
