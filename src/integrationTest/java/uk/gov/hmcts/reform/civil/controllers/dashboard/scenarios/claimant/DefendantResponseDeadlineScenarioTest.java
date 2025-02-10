package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefendantResponseDeadlineDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantResponseDeadlineScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantResponseDeadlineDashboardNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_defendant_response_deadline_scenario() throws Exception {

        String caseId = "123477";
        LocalDate responseDeadline = OffsetDateTime.now().toLocalDate();
        String defendantName = "Mr. Sole Trader";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                        .individualFirstName("James")
                        .individualLastName("John")
                        .build())
                .applicant1Represented(YesOrNo.NO)
                .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "CLAIMANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Response to the claim"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">James John has not responded to the claim." +
                    " You can now request a county court judgment." +
                    " The defendant can still respond to the claim before you ask for a judgment.</p>" +
                    "<p class=\"govuk-body\"><a href=\"{REQUEST_CCJ_URL}\" class=\"govuk-link\">Request a CCJ</a></p>"),
                jsonPath("$[0].titleCy").value("Ymateb i’r hawliad"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Nid yw James John wedi ymateb i’r hawliad." +
                        " Gallwch nawr wneud cais am ddyfarniad llys sirol." +
                        " Gall y diffynnydd dal ymateb i’r hawliad cyn i chi ofyn am ddyfarniad.</p>" +
                        "<p class=\"govuk-body\"><a href=\"{REQUEST_CCJ_URL}\" class=\"govuk-link\">Gwneud cais am CCJ</a></p>")
            );
    }

}
