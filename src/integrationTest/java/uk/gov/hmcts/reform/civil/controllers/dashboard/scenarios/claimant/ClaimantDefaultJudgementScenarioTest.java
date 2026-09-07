package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.claimant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardNotificationHandler;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimantDefaultJudgementScenarioTest extends DashboardBaseIntegrationTest {

    public static final String CLAIMANT = "CLAIMANT";

    @Autowired
    private DashboardNotificationHandler dashboardNotificationHandler;

    @Test
    void should_create_default_judgement_issued_scenario_for_claimant_when_judgment_buffer_disabled() throws Exception {

        String caseId = "720111";
        CaseData caseData = defaultJudgmentIssuedCaseData(caseId);

        when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(false);

        dashboardNotificationHandler.handle(callbackParams(caseData));

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
    void should_create_default_judgement_entered_scenario_for_claimant_when_judgment_buffer_enabled() throws Exception {

        String caseId = "720113";
        CaseData caseData = defaultJudgmentIssuedCaseData(caseId);

        when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(true);

        dashboardNotificationHandler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, CLAIMANT)
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("A judgment against the defendant has been made"),
                jsonPath("$[0].descriptionEn")
                    .value("<p class=\"govuk-body\">The defendant should now pay you according to the terms " +
                               "of the judgment.<br>Once they do, you should <a href=\"{CONFIRM_YOU_HAVE_BEEN_PAID_URL}\" class=\"govuk-link\">" +
                               "confirm that they've paid you the full amount that you're owed</a>.<br>If they do not pay you by the date on the judgment, " +
                               "you can <u>ask for enforcement action to be taken against them</u>.<br>If you need to change the terms of payment within the judgment, " +
                               "such as the instalments you had previously agreed, you can <a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">" +
                               "make an application to vary the judgment</a>.</p>")
            );
    }

    @Test
    void should_create_default_judgement_entered_scenario_for_claimant_welsh_when_judgment_buffer_enabled() throws Exception {

        String caseId = "720112";
        CaseData caseData = defaultJudgmentIssuedCaseData(caseId);

        when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(true);

        dashboardNotificationHandler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, CLAIMANT)
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleCy").value("Mae dyfarniad yn erbyn y diffynnydd wedi’i wneud"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Dylai'r diffynnydd nawr eich talu yn unol â thelerau'r dyfarniad." +
                        "<br>Unwaith y byddant yn gwneud hynny, dylech <a href=\"{CONFIRM_YOU_HAVE_BEEN_PAID_URL}\" class=\"govuk-link\">" +
                        "gadarnhau eu bod wedi talu'r swm llawn sy'n ddyledus i chi</a>.<br>Os na fyddant yn eich talu erbyn y dyddiad ar y dyfarniad, gallwch " +
                        "<u>ofyn am gymryd camau gorfodi yn eu herbyn</u>." +
                        "<br>Os oes angen i chi newid y telerau talu o fewn y dyfarniad, fel y rhandaliadau yr oeddech wedi cytuno arnynt yn flaenorol, gallwch " +
                        "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">wneud cais i amrywio’r dyfarniad</a>.</p>")
            );
    }

    private CaseData defaultJudgmentIssuedCaseData(String caseId) {
        return CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .businessProcess(new BusinessProcess().setActivityId(DashboardTaskIds.DJ_NON_DIVERGENT))
            .activeJudgment(new JudgmentDetails()
                                .setType(JudgmentType.DEFAULT_JUDGMENT)
                                .setState(JudgmentState.ISSUED))
            .build();
    }
}
