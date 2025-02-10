package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.DefaultJudgementIssuedClaimantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantDefaultJudgementScenarioTest extends DashboardBaseIntegrationTest {

    public static final String CLAIMANT = "CLAIMANT";

    @Autowired
    private DefaultJudgementIssuedClaimantNotificationHandler defaultJudgementIssuedClaimantNotificationHandler;

    @Test
    @DirtiesContext
    void should_create_scenario_for_default_judgement_claimant() throws Exception {

        String caseId = "720111";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .build();

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(featureToggleService.isGeneralApplicationsEnabled()).thenReturn(true);

        defaultJudgementIssuedClaimantNotificationHandler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, CLAIMANT)
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("A judgment against the defendant has been made"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">The defendant should now pay you according to the terms " +
                               "of the judgment. <br> Once they do, you should <a href=\"{CONFIRM_YOU_HAVE_BEEN_PAID_URL}\" class=\"govuk-link\">" +
                               "confirm that they’ve paid you the full amount that you’re owed</a>.<br>If they do not pay you by the date on the judgment, " +
                               "you can <u>ask for enforcement action to be taken against them</u>. <br>If you need to change the terms of payment within the judgment, " +
                               "such as the instalments you had previously agreed, you can <a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">" +
                               "make an application to vary the judgment</a>.</p>")
            );
    }

    @Test
    @DirtiesContext
    void should_create_scenario_for_default_judgement_claimant_welsh() throws Exception {

        String caseId = "720112";
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .build();

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(featureToggleService.isGeneralApplicationsEnabled()).thenReturn(true);

        defaultJudgementIssuedClaimantNotificationHandler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, CLAIMANT)
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleCy").value("Mae dyfarniad wedi’i wneud yn erbyn y diffynnydd"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Dylai’r diffynnydd eich talu yn unol â thelerau’r dyfarniad. " +
                        "<br> Unwaith y byddant yn gwneud hynny, dylech <a href=\"{CONFIRM_YOU_HAVE_BEEN_PAID_URL}\" class=\"govuk-link\">" +
                        "gadarnhau eu bod wedi talu’r swm llawn sy’n ddyledus i chi</a>.<br>Os na fyddant yn eich talu erbyn y dyddiad ar y dyfarniad, gallwch " +
                        "<u>ofyn am gymryd camau  gorfodi yn eu herbyn</u>." +
                        " <br>Os oes arnoch angen newid y telerau talu o fewn y dyfarniad, fel y rhandaliadau roeddech wedi cytuno arnynt yn flaenorol, gallwch " +
                        "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">wneud cais i amrywio’r dyfarniad</a>.</p>")
            );
    }
}
