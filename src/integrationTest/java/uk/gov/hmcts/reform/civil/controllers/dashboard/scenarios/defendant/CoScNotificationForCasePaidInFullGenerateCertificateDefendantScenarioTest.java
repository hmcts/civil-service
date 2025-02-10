package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.CoscCertificateGeneratedDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class CoScNotificationForCasePaidInFullGenerateCertificateDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CoscCertificateGeneratedDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_enable_dashboardNotification_scenario() throws Exception {

        String caseId = "14323365438241";
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDueUnpaid().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(NO)
            .certOfSC(CertOfSC.builder().defendantFinalPaymentDate(LocalDate.now().plusDays(10)).build())
            .activeJudgment(JudgmentDetails.builder()
                                .fullyPaymentMadeDate(LocalDate.now())
                                .build())
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Proof of debt payment issued"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">You’ve successfully confirmed that you’ve paid a judgment (CCJ)"
                        + " debt in full. The public register will be updated and you can now "
                        + "<a href=\"{VIEW_COSC_CERTIFICATE_URL}\" class=\"govuk-link\">view the certificate</a>. "
                        + "You should download this certificate for your records. The certificate can be found in "
                        + "‘Orders and notices from the court’.</p>"),
                jsonPath("$[0].titleCy").value("Cadarnhad o daliad dyled dyfarniad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Rydych chi wedi cadarnhau’n llwyddiannus eich bod wedi talu dyled y "
                        + "dyfarniad (CCJ) yn llawn. Bydd y gofrestr gyhoeddus yn cael ei diweddaru a gallwch <a "
                        + "href=\"{VIEW_COSC_CERTIFICATE_URL}\" class=\"govuk-link\">nawr weld y dystysgrif</a>. "
                        + "Dylech lawrlwytho’r dystysgrif hon ar gyfer eich cofnodion. Gellir dod o hyd i’r "
                        + "dystysgrif yn ’Gorchmynion a hysbysiadau gan y llys’.</p>"));
    }

}
