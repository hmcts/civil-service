package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseDeadlineDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantResponseDeadlineScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseDeadlineDashboardNotificationHandler handler;

    @Test
    void should_create_defendant_response_deadline_scenario() throws Exception {

        String caseId = "123477";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        String defendantName = "Mr. Sole Trader";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .applicant1Represented(YesOrNo.NO)
                .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The defendant has not responded to the claim.</p>" +
                    "<p class=\"govuk-body\">You can now request a county court judgment.<p/>" +
                    "<p class=\"govuk-body\">The defendant can still respond to the claim before you ask for a judgment.</p>" +
                    "<p class=\"govuk-body\"><a href=\"{COUNTY_COURT_JUDGEMENT_URL}\" class=\"govuk-link\">Request a CCJ</a></p>")
            );
    }

}
