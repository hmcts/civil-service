package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseWelshClaimantDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EnglishDefendantResponseWelshTranslationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseWelshClaimantDashboardNotificationHandler handler;

    @Test
    void should_create_defendant_response_claimant_dashboard_welsh_scenario() throws Exception {

        String caseId = "123451";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
                .toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").doesNotExist(),
                jsonPath("$[0].descriptionEn").doesNotExist(),
                jsonPath("$[0].titleCy").doesNotExist(),
                jsonPath("$[0].descriptionCy").doesNotExist()
            );

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").doesNotExist(),
                jsonPath("$[0].taskNameEn").doesNotExist(),
                jsonPath("$[0].taskNameCy").doesNotExist(),
                jsonPath("$[0].currentStatusCy").doesNotExist()
            );
    }

}
