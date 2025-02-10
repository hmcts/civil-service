package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HwFDashboardNotificationsHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;

public class ClaimIssueHwfNumUpdatedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HwFDashboardNotificationsHandler handler;

    @Test
    @DirtiesContext
    void should_create_claimIssue_hwf_num_updated_scenario() throws Exception {

        String caseId = "12346782";

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .hwfFeeType(FeeType.CLAIMISSUED)
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER).build())
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
                .andExpect(status().isOk())
                .andExpectAll(
                        status().is(HttpStatus.OK.value()),
                        jsonPath("$[0].titleEn").value("Your help with fees application has been updated"),
                        jsonPath("$[0].descriptionEn")
                                .value("<p class=\"govuk-body\">You've applied for help with the claim fee. " +
                                           "You'll receive an update from us within 5 to 10 working days.</p>"),
                        jsonPath("$[0].titleCy").value("Mae eich cais am help i dalu ffioedd wedi cael ei ddiweddaru"),
                        jsonPath("$[0].descriptionCy")
                                .value("<p class=\"govuk-body\">Fe wnaethoch gais am help i dalu ffi’r hawliad." +
                                           " Byddwch yn cael diweddariad gennym mewn 5 i 10 diwrnod gwaith.</p>"));
    }
}
