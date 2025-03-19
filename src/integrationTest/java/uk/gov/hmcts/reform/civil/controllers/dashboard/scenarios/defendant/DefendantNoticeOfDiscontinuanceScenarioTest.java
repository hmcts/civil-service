package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.DefendantNotifyDiscontinuanceDashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantNoticeOfDiscontinuanceScenarioTest extends DashboardBaseIntegrationTest {

    public static final String DEFENDANT = "DEFENDANT";

    @Autowired
    private DefendantNotifyDiscontinuanceDashboardNotificationHandler handler;

    @Test
    void should_create_scenario_for_discontinuance_claim() throws Exception {

        String caseId = "4321456";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, DEFENDANT)
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("A notice of discontinuance has been created and sent to all parties"),
                jsonPath("$[0].descriptionEn")
                    .value(
                        "<p class=\"govuk-body\">This means that all or part of this claim has been discontinued.<br>Please review the "
                            + "<a href=\"{NOTICE_OF_DISCONTINUANCE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">"
                            + "notice of discontinuance</a> carefully.</p>"),
                jsonPath("$[0].titleCy").value("MMae hysbysiad o ddirwyn i ben wedi’i greu a’i anfon at yr holl bartïon"),
                jsonPath("$[0].descriptionCy")
                    .value(
                        "<p class=\"govuk-body\">Mae hyn yn golygu bod rhan, neu'r cyfan, o'r hawliad hwn wedi dod i ben.<br>Adolygwch yr "
                            + "<a href=\"{NOTICE_OF_DISCONTINUANCE}\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"govuk-link\">"
                            + "hysbysiad o ddirwyn i ben</a> yn ofalus.</p>")
            );
    }
}
