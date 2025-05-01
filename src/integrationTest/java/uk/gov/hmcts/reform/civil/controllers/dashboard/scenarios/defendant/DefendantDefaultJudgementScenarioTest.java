package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.DefaultJudgementIssuedDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefendantDefaultJudgementScenarioTest extends  DashboardBaseIntegrationTest {

    public static final String DEFENDANT = "DEFENDANT";
    public static final String CLAIMANT = "CLAIMANT";

    @Autowired
    private DefaultJudgementIssuedDefendantNotificationHandler defaultJudgementIssuedDefendantNotificationHandler;

    @Autowired
    private DefaultJudgementIssuedDefendantNotificationHandler handler;

    @Test
    void should_create_scenario_for_default_judgement() throws Exception {

        String caseId = "720111";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .respondent1Represented(YesOrNo.NO)
                .build();

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(featureToggleService.isGeneralApplicationsEnabled()).thenReturn(true);

        defaultJudgementIssuedDefendantNotificationHandler.handle(callbackParams(caseData));
        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, DEFENDANT)
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("A judgment has been made against you"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">The exact details of what you need to pay, and by when, are stated on the judgment.   " +
                               "If you want to dispute the judgment, or ask to change how and when you pay back the claim amount, you can " +
                               "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to set aside (remove) or vary the judgment</a>.</p>"),
                jsonPath("$[0].titleCy").value("Mae Dyfarniad wedi’i wneud yn eich erbyn"),
                jsonPath("$[0].descriptionCy")
                    .value("<p class=\"govuk-body\">Mae union fanylion yr hyn mae arnoch angen ei dalu," +
                               " ac erbyn pryd, wedi’u nodi ar y dyfarniad. Os ydych eisiau gwrthwynebu’r dyfarniad," +
                               " neu ofyn i newid pryd a sut y byddwch yn talu swm yr hawliad yn ôl," +
                               " gallwch <a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">" +
                               "wneud cais i roi’r dyfarniad o’r naill du (ei ddileu) neu amrywio’r dyfarniad</a>.</p>")
            );
    }
}
