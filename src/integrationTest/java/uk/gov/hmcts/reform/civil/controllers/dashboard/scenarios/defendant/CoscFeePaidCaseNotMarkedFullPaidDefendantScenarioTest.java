package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
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
    @DirtiesContext
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
                    "<p class=\"govuk-body\">We've received your application " +
                        "to confirm you’ve paid a judgment debt. The person or business you owe money to now has a month to respond.</p>"),
                jsonPath("$[0].titleCy").value("Aros am gadarnhad yr hawlydd"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydym wedi cael eich cais i gadarnhau eich bod wedi talu dyled ddyfarniad. " +
                        "Mae gan yr unigolyn neu'r busnes y mae arnoch arian iddynt nawr fis i ymateb.</p>"
                )
            );
    }

}
