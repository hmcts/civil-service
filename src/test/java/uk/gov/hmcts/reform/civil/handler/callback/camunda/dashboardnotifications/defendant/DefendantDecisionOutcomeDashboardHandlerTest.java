package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_DASHBOARD_TASK_LIST_DEFENDANT_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_TRIAL_READY_DECISION_OUTCOME;

@ExtendWith(MockitoExtension.class)
class DefendantDecisionOutcomeDashboardHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private DefendantDecisionOutcomeDashboardHandler handler;
    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;

    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;

    public static final String TASK_ID = "GenerateDashboardDefendantDecisionOutcome";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(
                CallbackParamsBuilder.builder()
                    .request(CallbackRequest.builder()
                                 .eventId(UPDATE_DASHBOARD_TASK_LIST_DEFENDANT_DECISION_OUTCOME.name())
                                 .build())
                    .build()))
                .isEqualTo(TASK_ID);
        }

        @Test
        void shouldRecordScenario_whenInvokedWhenCaseProgressionSmallClaims() {
            CaseData caseData = CaseDataBuilder.builder().atCaseProgressionCheck().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .responseClaimTrack("SMALL_CLAIM")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_DASHBOARD_TASK_LIST_DEFENDANT_DECISION_OUTCOME.name()).build()
            ).build();

            when(toggleService.isCaseProgressionEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            handler.handle(params);

            // Then
            verifyDeleteNotificationsAndTaskListUpdates(caseData);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_TRIAL_READY_DECISION_OUTCOME.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvokedWhenCaseProgressionFastTrack() {
            CaseData caseData = CaseDataBuilder.builder().atCaseProgressionCheck().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .responseClaimTrack("FAST_CLAIM")
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_DASHBOARD_TASK_LIST_DEFENDANT_DECISION_OUTCOME.name()).build()
            ).build();

            when(toggleService.isCaseProgressionEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            handler.handle(params);

            // Then
            verifyDeleteNotificationsAndTaskListUpdates(caseData);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_DECISION_OUTCOME.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvokedWhenCaseProgressionFastTrack_TrialReadinessDone() {
            CaseData caseData = CaseDataBuilder.builder().atCaseProgressionCheck().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .responseClaimTrack("FAST_CLAIM")
                .trialReadyRespondent1(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_DASHBOARD_TASK_LIST_DEFENDANT_DECISION_OUTCOME.name()).build()
            ).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            when(toggleService.isCaseProgressionEnabled()).thenReturn(true);

            handler.handle(params);

            // Then
            verifyDeleteNotificationsAndTaskListUpdates(caseData);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_TRIAL_READY_DECISION_OUTCOME.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }

    private void verifyDeleteNotificationsAndTaskListUpdates(CaseData caseData) {
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT"
        );
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT",
            null
        );
    }
}
