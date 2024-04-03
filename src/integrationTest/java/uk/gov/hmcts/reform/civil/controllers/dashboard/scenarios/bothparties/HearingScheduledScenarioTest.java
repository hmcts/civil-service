package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.bothparties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.HearingScheduledNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel.IN_PERSON;

public class HearingScheduledScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private HearingScheduledNotificationHandler handler;

    @Test
    void should_create_hearing_scheduled_scenario() throws Exception {
        String caseId = "503206541654";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .hearingDate(LocalDate.of(2024, 4, 1))
            .hearingTimeHourMinute("14:00")
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .channel(IN_PERSON)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "BOTH")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("A hearing has been scheduled"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Your hearing has been scheduled for 4 April 2024 at 14:00 at "
                        + "County Court and will be held in person.</p><a href=\"{VIEW_HEARING_NOTICE}\""
                        + " class=\"govuk-link\">View the hearing notice</a>."),
                jsonPath("$[0].titleCy").value("A hearing has been scheduled"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Your hearing has "
                        + "been scheduled for 4 April 2024 at 14:00 at County Court"
                        + " and will be held in person.</p><a"
                        + " href=\"{VIEW_HEARING_NOTICE}\" class=\"govuk-link\">View the hearing notice</a>.")
            );

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "BOTH")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a href={VIEW_HEARINGS}  rel=\"noopener noreferrer\" class=\"govuk-link\">View hearings</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a href={VIEW_HEARINGS}  rel=\"noopener noreferrer\" class=\"govuk-link\">View hearings</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[1].reference").value(caseId.toString()),
                jsonPath("$[1].taskNameEn").value(
                    "<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.AVAILABLE.getName()),
                jsonPath("$[1].taskNameCy").value(
                    "<a href={VIEW_ORDERS_AND_NOTICES}  rel=\"noopener noreferrer\" class=\"govuk-link\">View orders and notices</a>"),
                jsonPath("$[1].currentStatusCy").value(TaskStatus.AVAILABLE.getName())

            );
    }
}
