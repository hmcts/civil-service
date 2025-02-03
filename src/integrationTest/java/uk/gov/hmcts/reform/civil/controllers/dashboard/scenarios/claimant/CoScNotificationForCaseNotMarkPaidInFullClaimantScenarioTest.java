package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.CoScNotificationForCaseNotMarkPaidInFullClaimantHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CoScNotificationForCaseNotMarkPaidInFullClaimantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CoScNotificationForCaseNotMarkPaidInFullClaimantHandler handler;

    @Test
    void should_enable_dashboardNotification_scenario() throws Exception {

        String caseId = "14323365438241";
        LocalDate coscFullPaymentDate = LocalDate.now();
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDueUnpaid().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .certOfSC(CertOfSC.builder().defendantFinalPaymentDate(coscFullPaymentDate).build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Confirm if a judgment debt has been paid"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">The defendant who owed you money has told us that they paid in full on " + DateUtils.formatDate(
                        coscFullPaymentDate) +
                        ". You should <a href=\"{CONFIRM_YOU_HAVE_BEEN_PAID_URL}\" class=\"govuk-link\">confirm if you've been paid</a>." +
                        "<br><br>If you have not been paid, you will need to write to the court where the judgment was issued within a month from " + DateUtils.formatDate(
                        coscFullPaymentDate) +
                        " so the application can be reviewed by a district judge. You can find the name of the court on the top right of the judgment." +
                        " You can then <u>search for the contact details of the court</u> to get the address.</p>"),
                jsonPath("$[0].titleCy").value("Cadarnhau a yw dyled dyfarniad wedi'i thalu"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae diffynnydd a oedd arnynt arian ichi wedi dweud wrthym eu bod wedi talu’n llawn ar " + DateUtils.formatDateInWelsh(
                        coscFullPaymentDate) +
                        ". Dylech <a href=\"{CONFIRM_YOU_HAVE_BEEN_PAID_URL}\" class=\"govuk-link\">gadarnhau os ydych wedi cael eich talu</a>." +
                        "<br><br>Os nad ydych wedi cael eich talu, bydd angen i chi ysgrifennu at y llys lle cyhoeddwyd y dyfarniad o fewn mis i " + DateUtils.formatDateInWelsh(
                        coscFullPaymentDate) +
                        " fel y gall barnwr rhanbarth adolygu’r cais. Gallwch ddod o hyd i enw’r llys ar ochr dde uchaf y dyfarniad." +
                        " Yna gallwch <u>chwilio am fanylion cyswllt y llys</u> i gael y cyfeiriad.</p>"));
    }

}
