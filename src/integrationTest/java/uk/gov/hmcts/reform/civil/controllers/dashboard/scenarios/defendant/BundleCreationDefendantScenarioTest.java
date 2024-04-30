package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.BundleCreationDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BundleCreationDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private BundleCreationDefendantNotificationHandler handler;

    @Test
    void should_create_bundle_created_scenario_when_trial_ready() throws Exception {
        String caseId = "12349485";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyRespondent1().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The bundle is ready to view"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The bundle contains all the documents that will be referred to at the hearing. " +
                        "<a href=\"{VIEW_BUNDLE_REDIRECT}\" class=\"govuk-link\">Review the bundle</a>" +
                        " to ensure that the information is accurate.</p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_BUNDLE} class=\"govuk-link\">View the bundle</a>")
            );
    }

    @Test
    void should_create_bundle_created_scenario_when_not_trial_ready() throws Exception {
        String caseId = "12349485";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("The bundle is ready to view"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The bundle contains all the documents that will be referred to at the hearing. " +
                        "<a href=\"{VIEW_BUNDLE_REDIRECT}\" class=\"govuk-link\">Review the bundle</a>" +
                        " to ensure that the information is accurate.</p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_BUNDLE} class=\"govuk-link\">View the bundle</a>")
            );
    }
}
