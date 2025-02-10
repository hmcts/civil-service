package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.GenerateDashboardNotificationHwfHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HwfRequestedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private GenerateDashboardNotificationHwfHandler claimHwFHandler;
    private static final String GET_NOTIFICATIONS_URL
        = "/dashboard/notifications/{ccd-case-identifier}/role/{role-type}";
    private static final String GET_TASKS_ITEMS_URL = "/dashboard/taskList/{ccd-case-identifier}/role/{role-type}";

    @Test
    @DirtiesContext
    void should_create_scenario_hwf_requested_small_claims() throws Exception {

        String caseId = "123973";

        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(7000)).build())
            .applicant1Represented(YesOrNo.NO)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();

        //When
        claimHwFHandler.handle(callbackParams(caseData));

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a href={VIEW_CLAIM_URL}  rel=\"noopener noreferrer\" class=\"govuk-link\">View the claim</a>"),
                jsonPath("$[0].currentStatusEn").value("Available"),
                jsonPath("$[1].taskNameEn").value("<a href={VIEW_INFO_ABOUT_CLAIMANT}  rel=\"noopener noreferrer\" class=\"govuk-link\">View information about the claimant</a>"),
                jsonPath("$[1].currentStatusEn").value("Available"),
                jsonPath("$[2].taskNameEn").value("<a>View the response to the claim</a>"),
                jsonPath("$[2].currentStatusEn").value("Not available yet"),
                jsonPath("$[3].taskNameEn").value("<a>View information about the defendant</a>"),
                jsonPath("$[3].currentStatusEn").value("Not available yet"),
                jsonPath("$[4].taskNameEn").value("<a>View mediation settlement agreement</a>"),
                jsonPath("$[4].currentStatusEn").value("Not available yet"),
                jsonPath("$[5].taskNameEn").value("<a>Upload mediation documents</a>"),
                jsonPath("$[5].currentStatusEn").value("Not available yet"),
                jsonPath("$[6].taskNameEn").value("<a>View mediation documents</a>"),
                jsonPath("$[6].currentStatusEn").value("Not available yet"),
                jsonPath("$[7].taskNameEn").value("<a>View the hearing</a>"),
                jsonPath("$[7].currentStatusEn").value("Not available yet"),
                jsonPath("$[8].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[8].currentStatusEn").value("Not available yet"),
                jsonPath("$[9].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[9].currentStatusEn").value("Not available yet"),
                jsonPath("$[10].taskNameEn").value("<a>View documents</a>"),
                jsonPath("$[10].currentStatusEn").value("Not available yet"),
                jsonPath("$[11].taskNameEn").value("<a>View the bundle</a>"),
                jsonPath("$[11].currentStatusEn").value("Not available yet"),
                jsonPath("$[12].taskNameEn").value("<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[12].currentStatusEn").value("Available"),
                jsonPath("$[13].taskNameEn").value("<a>View the judgment</a>"),
                jsonPath("$[13].currentStatusEn").value("Not available yet"),
                jsonPath("$[14].taskNameEn").value("<a>Contact the court to request a change to my case</a>"),
                jsonPath("$[14].currentStatusEn").value("Not available yet"),
                jsonPath("$[15].taskNameEn").value("<a>View applications</a>"),
                jsonPath("$[15].currentStatusEn").value("Not available yet")

            );

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("We're reviewing your help with fees application"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You've applied for help with the claim fee. " +
                        "You'll receive an update in 5 to 10 working days.</p>"),
                jsonPath("$[0].titleCy").value("Rydym yn adolygu eich cais am help i dalu ffioedd"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Fe wnaethoch gais am help i dalu ffi’r hawliad. " +
                        "Byddwch yn cael diweddariad mewn 5 i 10 diwrnod gwaith.</p>")
            );
    }

    @Test
    @DirtiesContext
    void should_create_scenario_hwf_requested_fast_track() throws Exception {

        String caseId = "123973";

        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(7000)).build())
            .applicant1Represented(YesOrNo.NO)
            .totalClaimAmount(BigDecimal.valueOf(10001))
            .build();

        //When
        claimHwFHandler.handle(callbackParams(caseData));

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_CLAIM_URL}  rel=\"noopener noreferrer\" class=\"govuk-link\">View the claim</a>"),
                jsonPath("$[0].currentStatusEn").value("Available"),
                jsonPath("$[1].taskNameEn").value(
                    "<a href={VIEW_INFO_ABOUT_CLAIMANT}  rel=\"noopener noreferrer\" class=\"govuk-link\">View information about the claimant</a>"),
                jsonPath("$[1].currentStatusEn").value("Available"),
                jsonPath("$[2].taskNameEn").value("<a>View the response to the claim</a>"),
                jsonPath("$[2].currentStatusEn").value("Not available yet"),
                jsonPath("$[3].taskNameEn").value("<a>View information about the defendant</a>"),
                jsonPath("$[3].currentStatusEn").value("Not available yet"),
                jsonPath("$[4].taskNameEn").value("<a>View mediation settlement agreement</a>"),
                jsonPath("$[4].currentStatusEn").value("Not available yet"),
                jsonPath("$[5].taskNameEn").value("<a>Upload mediation documents</a>"),
                jsonPath("$[5].currentStatusEn").value("Not available yet"),
                jsonPath("$[6].taskNameEn").value("<a>View mediation documents</a>"),
                jsonPath("$[6].currentStatusEn").value("Not available yet"),
                jsonPath("$[7].taskNameEn").value("<a>View the hearing</a>"),
                jsonPath("$[7].currentStatusEn").value("Not available yet"),
                jsonPath("$[8].taskNameEn").value("<a>Pay the hearing fee</a>"),
                jsonPath("$[8].currentStatusEn").value("Not available yet"),
                jsonPath("$[8].currentStatusEn").value("Not available yet"),
                jsonPath("$[9].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[9].currentStatusEn").value("Not available yet"),
                jsonPath("$[10].taskNameEn").value("<a>View documents</a>"),
                jsonPath("$[10].currentStatusEn").value("Not available yet"),
                jsonPath("$[11].taskNameEn").value("<a>Add the trial arrangements</a>"),
                jsonPath("$[11].currentStatusEn").value("Not available yet"),
                jsonPath("$[12].taskNameEn").value("<a>View the bundle</a>"),
                jsonPath("$[12].currentStatusEn").value("Not available yet"),
                jsonPath("$[13].taskNameEn").value(
                    "<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[13].currentStatusEn").value("Available"),
                jsonPath("$[14].taskNameEn").value("<a>View the judgment</a>"),
                jsonPath("$[14].currentStatusEn").value("Not available yet"),
                jsonPath("$[15].taskNameEn").value("<a>Contact the court to request a change to my case</a>"),
                jsonPath("$[15].currentStatusEn").value("Not available yet"),
                jsonPath("$[16].taskNameEn").value("<a>View applications</a>"),
                jsonPath("$[16].currentStatusEn").value("Not available yet")

            );

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("We're reviewing your help with fees application"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You've applied for help with the claim fee. " +
                        "You'll receive an update in 5 to 10 working days.</p>"),
                jsonPath("$[0].titleCy").value("Rydym yn adolygu eich cais am help i dalu ffioedd"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Fe wnaethoch gais am help i dalu ffi’r hawliad. " +
                        "Byddwch yn cael diweddariad mewn 5 i 10 diwrnod gwaith.</p>")
            );
    }
}
