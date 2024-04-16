package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.TrialArrangementsDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class DefendantTrialArrangementsNotificationScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private TrialArrangementsDefendantNotificationHandler handler;

    @Test
    void shouldCreateAddTrialArrangementsForDefendant() throws Exception {
        String caseId = "1234";
        String defendantName = "Mr. Sole Trader";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .respondent1Represented(NO)
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created for DEFENDANT
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value(
                "Confirm your trial arrangements"),
                jsonPath("$[0].descriptionEn").value(
                "<p class=\"govuk-body\">You must <a href=\"{ADD_TRIAL_ARRANGEMENTS}\" class=\"govuk-link\">confirm your trial arrangements</a> by {HEARING_DUE_DATE}. This means that you'll need to confirm if the case is ready for trial or not. You'll also need to confirm whether circumstances have changed since you completed the directions questionnaire. Refer to the <a href=\"{VIEW_DEFENDANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">questionnaire you submitted</a> if you're not sure what you previously said.</p>"),
                jsonPath("$[0].titleCy").value(
                "Confirm your trial arrangements"),
                jsonPath("$[0].descriptionCy").value(
                "<p class=\"govuk-body\">You must <a href=\"{ADD_TRIAL_ARRANGEMENTS}\" class=\"govuk-link\">confirm your trial arrangements</a> by {HEARING_DUE_DATE}. This means that you'll need to confirm if the case is ready for trial or not. You'll also need to confirm whether circumstances have changed since you completed the directions questionnaire. Refer to the <a href=\"{VIEW_DEFENDANT_HEARING_REQS}\" rel=\"noopener noreferrer\" class=\"govuk-link\" target=\"_blank\">questionnaire you submitted</a> if you're not sure what you previously said.</p>")
            );
    }
}
