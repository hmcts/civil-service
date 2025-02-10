package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.CaseProceedOfflineDefendantNotificationHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CaseProceedOfflineDefendantScenarioTest extends DashboardBaseIntegrationTest {

    @Autowired
    private CaseProceedOfflineDefendantNotificationHandler handler;

    @Test
    @DirtiesContext
    void should_create_case_proceed_offline_defendant_scenario() throws Exception {

        String caseId = "72016577145";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .respondent1Represented(YesOrNo.NO)
                .previousCCDState(CaseState.AWAITING_APPLICANT_INTENTION)
                .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
                .andExpect(status().isOk())
                .andExpectAll(
                        status().is(HttpStatus.OK.value()),
                        jsonPath("$[0].titleEn").value("Your online account will no longer be updated"),
                        jsonPath("$[0].descriptionEn").value(
                                "<p class=\"govuk-body\">Your online account will no longer be updated."
                                        + " If there are any further updates to your case these will be by post.</p>"),
                        jsonPath("$[0].titleCy").value("Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru mwyach"),
                        jsonPath("$[0].descriptionCy").value(
                                "<p class=\"govuk-body\">Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru mwyach."
                                        + " Os oes unrhyw ddiweddariadau pellach i’ch achos, bydd y rhain yn cael eu hanfon atoch drwy'r post.</p>"));

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a>Upload hearing documents</a>"),
                jsonPath("$[0].currentStatusEn").value("Inactive")

            );
    }

    @Test
    @DirtiesContext
    void should_create_case_proceed_offline__claimant_scenario_without_tasks() throws Exception {

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        String caseId = "72016577183";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .previousCCDState(CaseState.AWAITING_APPLICANT_INTENTION)
            .build();

        handler.handle(callbackParams(caseData));

        //Verify Notification is created
        doGet(BEARER_TOKEN, GET_NOTIFICATIONS_URL, caseId, "DEFENDANT")
            .andExpect(status().isOk())
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].titleEn").value("Your online account will no longer be updated"),
                jsonPath("$[0].descriptionEn").value(
                    "<p class=\"govuk-body\">Your online account will no longer be updated."
                        + " If there are any further updates to your case these will be by post.</p>"),
                jsonPath("$[0].titleCy").value("Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru mwyach"),
                jsonPath("$[0].descriptionCy").value(
                    "<p class=\"govuk-body\">Ni fydd eich cyfrif ar-lein yn cael ei ddiweddaru mwyach."
                        + " Os oes unrhyw ddiweddariadau pellach i’ch achos, bydd y rhain yn cael eu hanfon atoch drwy'r post.</p>"));

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0]").doesNotExist()
            );
    }

    @Test
    @DirtiesContext
    void shouldUpdateCaseProceedOffLineTaskList() throws Exception {

        when(featureToggleService.isCoSCEnabled()).thenReturn(true);
        String caseId = "72016577333";

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .respondent1Represented(YesOrNo.NO)
            .activeJudgment(JudgmentDetails.builder().build())
            .previousCCDState(CaseState.All_FINAL_ORDERS_ISSUED)
            .build();
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        when(featureToggleService.isCoSCEnabled()).thenReturn(true);

        handler.handle(callbackParams(caseData));

        //Verify task Item is created
        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value("<a>Upload hearing documents</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[0].taskNameCy").value("<a>Llwytho dogfennau'r gwrandawiad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[1].reference").value(caseId.toString()),
                jsonPath("$[1].taskNameEn").value("<a>Confirm you've paid a judgment (CCJ) debt</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[1].taskNameCy").value("<a>Cadarnhewch eich bod wedi talu dyled dyfarniad (CCJ)</a>"),
                jsonPath("$[1].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName())
            );
    }
}
