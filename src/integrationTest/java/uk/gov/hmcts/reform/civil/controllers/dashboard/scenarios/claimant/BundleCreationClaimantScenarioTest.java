package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.CaseProgressionDashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.BundleCreationClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class BundleCreationClaimantScenarioTest extends CaseProgressionDashboardBaseIntegrationTest {

    @Autowired
    private BundleCreationClaimantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_bundle_created_scenario() throws Exception {
        String caseId = "12325480";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
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
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_BUNDLE} class=\"govuk-link\">View the bundle</a>")
            );
    }

    @Test
    void should_create_bundle_created_scenario_without_trial_arrangements_when_small_claims() throws Exception {
        String caseId = "18749472";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
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
                jsonPath("$[0].titleEn").value("The bundle is ready to view"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The bundle contains all the documents that will be referred to at the hearing. " +
                        "<a href=\"{VIEW_BUNDLE_REDIRECT}\" class=\"govuk-link\">Review the bundle</a>" +
                        " to ensure that the information is accurate.</p>")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_BUNDLE} class=\"govuk-link\">View the bundle</a>")
            );
    }

    @Test
    void should_create_bundle_created_scenario_when_trial_ready() throws Exception {
        String caseId = "12344483";
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyApplicant().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .orderType(OrderType.DECIDE_DAMAGES)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
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
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "CLAIMANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_BUNDLE} class=\"govuk-link\">View the bundle</a>")
            );
    }
}
