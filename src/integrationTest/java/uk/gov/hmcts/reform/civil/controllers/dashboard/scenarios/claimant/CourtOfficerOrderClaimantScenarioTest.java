package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.CourtOfficerOrderClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourtOfficerOrderClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CourtOfficerOrderClaimantNotificationHandler handler;

    @Test
    void should_create_court_officer_order_claimant_scenario() throws Exception {

        String caseId = "72016565145";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .previousCCDState(CaseState.DECISION_OUTCOME)
                .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
                .andExpect(status().isOk())
                .andExpectAll(
                        status().is(HttpStatus.OK.value()),
                        jsonPath("$[0].titleEn").value("An order has been made"),
                        jsonPath("$[0].descriptionEn").value(
                            "<p class=\"govuk-body\">The Court has made an order on your claim.</p>" +
                                "<p class=\"govuk-body\"><a href=\"{VIEW_ORDERS_AND_NOTICES}\" rel=\"noopener noreferrer\" target=\"_blank\" " +
                                "class=\"govuk-link\">View the order</a></p>"),
                        jsonPath("$[0].titleCy").value("Mae gorchymyn wedi’i wneud"),
                        jsonPath("$[0].descriptionCy").value(
                            "<p class=\"govuk-body\">Mae’r Llys wedi gwneud gorchymyn ar eich hawliad.</p>" +
                                "<p class=\"govuk-body\"><a href=\"{VIEW_ORDERS_AND_NOTICES}\" rel=\"noopener noreferrer\" target=\"_blank\" " +
                                "class=\"govuk-link\">Gweld y gorchymyn</a></p>"));

    }

    @Test
    void should_create_court_officer_order_claimant_scenario_without_tasks() throws Exception {

        String caseId = "72016565145";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .previousCCDState(CaseState.HEARING_READINESS)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("An order has been made"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The Court has made an order on your claim.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_ORDERS_AND_NOTICES}\" rel=\"noopener noreferrer\" target=\"_blank\" " +
                        "class=\"govuk-link\">View the order</a></p>"),
                jsonPath("$[0].titleCy").value("Mae gorchymyn wedi’i wneud"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae’r Llys wedi gwneud gorchymyn ar eich hawliad.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{VIEW_ORDERS_AND_NOTICES}\" rel=\"noopener noreferrer\" target=\"_blank\" " +
                        "class=\"govuk-link\">Gweld y gorchymyn</a></p>"));
    }
}
