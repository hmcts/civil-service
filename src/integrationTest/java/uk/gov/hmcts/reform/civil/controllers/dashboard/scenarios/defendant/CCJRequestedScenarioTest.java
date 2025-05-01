package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.CCJRequestedDashboardNotificationDefendantHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CCJRequestedScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CCJRequestedDashboardNotificationDefendantHandler handler;

    @Test
    void should_create_ccj_requested_scenario() throws Exception {

        String caseId = "12348991012";

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .ccdCaseReference(Long.valueOf(caseId))
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Mr. John Rambo has requested a County Court Judgment (CCJ)"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Mr. John Rambo has requested a CCJ against you because you have not responded to the claim and the response deadline" +
                        " has passed.</p><p class=\"govuk-body\">Your online account will not be updated with the progress of the claim, and any further updates will be by post." +
                        "</p><p class=\"govuk-body\">If your deadline has passed, but the CCJ has not been issued, you can " +
                        "still respond. Get in touch with HMCTS on {civilMoneyClaimsTelephone} " +
                        "if you are in England and Wales. You can call from Monday to Friday, between 8.30am to 5pm. " +
                        "<a href=\"https://www.gov.uk/call-charges\" target=\"_blank\" " +
                        "rel=\"noopener noreferrer\" class=\"govuk-link\">Find out about call charges (opens in new tab)</a>.</p>" +
                        "<p class=\"govuk-body\">If you do not get in touch, " +
                        "we will post a CCJ to yourself and Mr. John Rambo and explain what to do next.</p>"),
                jsonPath("$[0].titleCy").value("Mae Mr. John Rambo wedi gwneud cais am Ddyfarniad Llys Sirol (CCJ)"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Mae Mr. John Rambo wedi gwneud cais am CCJ yn " +
                        "eich erbyn oherwydd nid ydych wedi ymateb i’r hawliad ac " +
                        "mae’r terfyn amser ar gyfer ymateb wedi bod.</p><p class=\"govuk-body\">" +
                        "Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru gyda manylion " +
                        "cynnydd yr hawliad, a bydd unrhyw ddiweddariadau pellach yn cael eu " +
                        "hanfon drwy’r post.</p><p class=\"govuk-body\">Os yw eich terfyn amser " +
                        "wedi pasio, ond nad yw’r CCJ wedi’i gyhoeddi, gallwch dal ymateb. " +
                        "Cysylltwch â Gwasanaeth Llysoedd a Thribiwnlysoedd EF (GLlTEF) ar " +
                        "{civilMoneyClaimsTelephone} os ydych yn Nghymru a Lloegr. Gallwch " +
                        "ffonio rhwng 8.30am a 5pm dydd Llun i ddydd Gwener. " +
                        "<a href=\"https://www.gov.uk/call-charges\" target=\"_blank\" " +
                        "rel=\"noopener noreferrer\" class=\"govuk-link\">Gwybodaeth am gost " +
                        "galwadau (yn agor mewn tab newydd)</a>.</p><p class=\"govuk-body\">" +
                        "Os na fyddwch yn cysylltu, byddwn yn anfon CCJ drwy’r post atoch chi a " +
                        "Mr. John Rambo ac yn egluro beth i’w wneud nesaf.</p>"
                    )
            );

    }

}
