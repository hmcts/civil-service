package uk.gov.hmcts.reform.civil.controllers.dashboard.scenarios.defendant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.civil.controllers.DashboardBaseIntegrationTest;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant.DefendantDecisionOutcomeDashboardHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateDashboardTaskListDefendantScenarioTest
    extends DashboardBaseIntegrationTest {

    @Autowired
    private DefendantDecisionOutcomeDashboardHandler handler;

    @Test
    void should_update_taskList_for_defendant_when_decision_outcome_small_claims() throws Exception {

        String caseId = "10002348";
        CaseData caseData = CaseDataBuilder.builder().atStateAwaitingResponseNotFullDefenceReceived().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .responseClaimTrack("SMALL_CLAIM")
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        handler.handle(callbackParams(caseData));

        //Verify Notification is created

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a>Upload hearing documents</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a>Llwytho dogfennau'r gwrandawiad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName())
            );
    }

    @Test
    void should_update_taskList_for_defendant_when_decision_outcome_fast_track() throws Exception {

        String caseId = "10002348";
        CaseData caseData = CaseDataBuilder.builder().atStateAwaitingResponseNotFullDefenceReceived().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .responseClaimTrack("FAST_CLAIM")
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        handler.handle(callbackParams(caseData));

        //Verify Notification is created

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a>Upload hearing documents</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a>Llwytho dogfennau'r gwrandawiad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName()),
                jsonPath("$[1].taskNameEn").value(
                    "<a>Add the trial arrangements</a>"),
                jsonPath("$[1].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[1].taskNameCy").value(
                    "<a>Ychwanegu trefniadau'r treial</a>"),
                jsonPath("$[1].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName())
            );
    }

    @Test
    void should_update_taskList_for_defendant_when_decision_outcome_fast_track_trial_ready() throws Exception {

        String caseId = "10002348";
        CaseData caseData = CaseDataBuilder.builder().atStateAwaitingResponseNotFullDefenceReceived().build()
            .toBuilder()
            .legacyCaseReference("reference")
            .ccdCaseReference(Long.valueOf(caseId))
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .responseClaimTrack("FAST_CLAIM")
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        handler.handle(callbackParams(caseData));

        //Verify Notification is created

        doGet(BEARER_TOKEN, GET_TASKS_ITEMS_URL, caseId, "DEFENDANT")
            .andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$[0].reference").value(caseId.toString()),
                jsonPath("$[0].taskNameEn").value(
                    "<a>Upload hearing documents</a>"),
                jsonPath("$[0].currentStatusEn").value(TaskStatus.INACTIVE.getName()),
                jsonPath("$[0].taskNameCy").value(
                    "<a>Llwytho dogfennau'r gwrandawiad</a>"),
                jsonPath("$[0].currentStatusCy").value(TaskStatus.INACTIVE.getWelshName())
            );
    }
}
