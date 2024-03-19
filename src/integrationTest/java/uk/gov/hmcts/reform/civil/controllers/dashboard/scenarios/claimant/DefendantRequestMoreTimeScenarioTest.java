package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.ResponseDeadlineExtendedDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

public class DefendantRequestMoreTimeScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ResponseDeadlineExtendedDashboardNotificationHandler handler;

    @Test
    void should_create_more_time_requested_scenario() throws Exception {

        String caseId = "31415926";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder().respondent1Represented(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andDo(handler -> {
                String response = handler.getResponse().getContentAsString();
                System.out.println(response);
            })
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("More time requested"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The response deadline for the defendant is now ${defaultRespondTime} on ${respondent1ResponseDeadlineEn} ({daysLeftToRespond} days remaining).</p>"),
                jsonPath("$[0].titleCy").value("More time requested"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">The response deadline for the defendant is now ${defaultRespondTime} on ${respondent1ResponseDeadlineEn} ({daysLeftToRespond} days remaining).</p>")
            );
    }
}
