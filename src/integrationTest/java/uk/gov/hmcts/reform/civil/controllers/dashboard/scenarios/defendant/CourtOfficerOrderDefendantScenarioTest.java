package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.CourtOfficerOrderDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourtOfficerOrderDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CourtOfficerOrderDefendantNotificationHandler handler;

    @Test
    void should_create_court_officer_order_defendant_scenario() throws Exception {

        String caseId = "72016577145";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .previousCCDState(CaseState.HEARING_READINESS)
                .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
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
    void should_create_court_officer_order_defendant_scenario_without_tasks() throws Exception {

        String caseId = "72016577183";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .previousCCDState(CaseState.DECISION_OUTCOME)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
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
