package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.DefendantResponseDeadlinePassedNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantResponseDeadlinePassedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseDeadlinePassedNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_scenario_for_when_defendant_response_deadline_passed() throws Exception {

        String caseId = "10002348";
        CaseData caseData = CaseDataBuilder.builder().atStateAwaitingResponseNotFullDefenceReceived().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL)
                    .individualFirstName("Claimant")
                    .individualLastName("John")
                    .build())
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">You have not responded to the claim.</p>"
                               + "<p class=\"govuk-body\">Claimant John can now request a county court judgment."
                               + " You can still respond to the claim before they ask for a judgment.</p>"
                               + "<p class=\"govuk-body\">A County Court Judgment can mean you find it difficult to get credit, like a mortgage or mobile phone contact."
                               + " Bailiffs could also be sent to your home.</p>"
                               + "<p class=\"govuk-body\"><a href=\"{RESPONSE_TASK_LIST_URL}\" class=\"govuk-link\">Respond to"
                               + " claim</a></p>"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">Nid ydych wedi ymateb i’r hawliad.</p>"
                               + "<p class=\"govuk-body\">Gall Claimant John nawr wneud cais am ddyfarniad llys sirol."
                               + " Gallwch dal ymateb i’r hawliad cyn iddynt wneud cais am ddyfarniad.</p>"
                               + "<p class=\"govuk-body\">Gall Dyfarniad Llys Sirol olygu eich bod yn ei chael hi'n anodd cael credyd, fel morgais neu gontract ffôn symudol."
                               + " Gallai beilïaid hefyd gael eu hanfon i'ch cartref.</p>"
                               + "<p class=\"govuk-body\"><a href=\"{RESPONSE_TASK_LIST_URL}\" class=\"govuk-link\">Ymateb i hawliad</a></p>")
            );
    }
}
