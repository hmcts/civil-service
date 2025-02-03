package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ResponseDeadlineExtendedDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantRequestMoreTimeScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private ResponseDeadlineExtendedDashboardNotificationHandler handler;

    @Test
    void should_create_more_time_requested_scenario() throws Exception {

        String caseId = "12349";
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
            .toBuilder()
            .respondent1ResponseDeadline(LocalDateTime.of(2024, 4, 1, 12, 0))
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .totalClaimAmount(new BigDecimal(5000))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("More time requested"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The response deadline for the defendant is now 4pm on 1 April 2024. There are {daysLeftToRespond} days remaining.</p>"),
                jsonPath("$[0].titleCy").value("Cais am fwy o amser"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Y terfyn amser ar gyfer ymateb y diffynnydd nawr yw 4pm ar 1 Ebrill 2024. Mae yna {daysLeftToRespond} diwrnod yn weddill.</p>")
            );
    }
}
