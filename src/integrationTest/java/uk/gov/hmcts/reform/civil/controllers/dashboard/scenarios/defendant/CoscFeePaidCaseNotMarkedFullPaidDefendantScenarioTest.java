package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.CoscFeePaidCaseNotMarkedFullPaidDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class CoscFeePaidCaseNotMarkedFullPaidDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CoscFeePaidCaseNotMarkedFullPaidDefendantNotificationHandler handler;

    @Test
    void should_enable_dashboardNotification_scenario() throws Exception {

        String caseId = "14323345634568241";
        LocalDate hearingDueDate = LocalDate.now().minusDays(1);
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDueUnpaid().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Awaiting claimant confirmation"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">We've received your application for proof that you've paid your"
                        + " debt. The claimant will now have 30 days to confirm this. If they don't respond in this time"
                        + " then the certificate will be issued automatically.</p>"),
                jsonPath("$[0].titleCy").value("Awaiting claimant confirmation"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">We've received your application for proof that you've paid your"
                        + " debt. The claimant will now have 30 days to confirm this. If they don't respond in this time"
                        + " then the certificate will be issued automatically.</p>"
                )
            );
    }

}
