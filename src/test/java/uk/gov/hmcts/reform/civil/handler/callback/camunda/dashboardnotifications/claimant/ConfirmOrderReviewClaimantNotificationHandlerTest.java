package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_DASHBOARD_CLAIMANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class ConfirmOrderReviewClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ConfirmOrderReviewClaimantNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    public static final String TASK_ID = "UpdateTaskListConfirmOrderReviewClaimant";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void shouldConfigureDashboardNotificationsStayCase() {

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "url");

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder().applicant1Represented(YesOrNo.NO)
            .isFinalOrder(YesOrNo.YES)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData).request(
                        CallbackRequest.builder().eventId(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT.name()).build()).build();

        handler.handle(callbackParams);

        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "CLAIMANT",
            "Applications"
        );

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            caseData.getCcdCaseReference().toString(),
            "CLAIMANT"
        );
    }

    @Test
    void configureDashboardScenario_shouldNotMakeTasksInactiveOrDeleteNotifications_whenScenarioShouldNotBeRecorded() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder().applicant1Represented(YesOrNo.YES)
            .isFinalOrder(YesOrNo.NO)
            .build();
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT.name()).build()).build();

        handler.configureDashboardScenario(callbackParams);

        verifyNoInteractions(dashboardNotificationService);
        verifyNoInteractions(taskListService);
    }

    @Test
    void shouldRecordScenarioClaimantFinalOrder_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .isFinalOrder(YesOrNo.YES)
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT.name())
                .caseDetails(CaseDetails.builder().state(All_FINAL_ORDERS_ISSUED.toString()).build()).build()).build();

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        handler.handle(params);

        // Then
        HashMap<String, Object> scenarioParams = new HashMap<>();
        verifyDeleteNotificationsAndTaskListUpdates(caseData);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_UPDATE_DASHBOARD_CLAIMANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldRecordScenarioClaimantFinalOrderFastTrackNotReadyTrial_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .isFinalOrder(YesOrNo.YES)
            .build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT.name())
                .caseDetails(CaseDetails.builder().state(All_FINAL_ORDERS_ISSUED.toString()).build()).build()).build();

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        handler.handle(params);

        // Then
        verifyDeleteNotificationsAndTaskListUpdates(caseData);
        HashMap<String, Object> scenarioParams = new HashMap<>();
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    private void verifyDeleteNotificationsAndTaskListUpdates(CaseData caseData) {
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "CLAIMANT",
            "Applications"
        );

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            caseData.getCcdCaseReference().toString(),
            "CLAIMANT"
        );
    }
}
