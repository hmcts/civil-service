package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_DASHBOARD_DEFENDANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class ConfirmOrderReviewDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ConfirmOrderReviewDefendantNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    public static final String TASK_ID = "UpdateTaskListConfirmOrderReviewDefendant";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void shouldConfigureDashboardNotificationsStayCase() {

        when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder().respondent1Represented(YesOrNo.NO)
            .isFinalOrder(YesOrNo.YES)
            .build();

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "url");

        when(mapper.mapCaseDataToParams(any(), any())).thenReturn(scenarioParams);
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT.name()).build()).build();

        handler.handle(callbackParams);

        verify(dashboardApiClient).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT",
            ConfirmOrderReviewDefendantNotificationHandler.GA,
            "BEARER_TOKEN"
        );

        verify(dashboardApiClient).deleteNotificationsForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT",
            "BEARER_TOKEN"
        );
    }

    @Test
    void configureDashboardScenario_shouldNotMakeTasksInactiveOrDeleteNotifications_whenScenarioShouldNotBeRecorded() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder().respondent1Represented(YesOrNo.YES)
            .isFinalOrder(YesOrNo.NO)
            .build();
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT.name()).build()).build();

        handler.configureDashboardScenario(callbackParams);

        verify(dashboardApiClient, never()).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            anyString(),
            anyString(),
            anyString(),
            anyString()
        );
        verify(dashboardApiClient, never()).deleteNotificationsForCaseIdentifierAndRole(
            anyString(),
            anyString(),
            anyString()
        );
    }

    @Test
    void shouldRecordScenarioDefendantFinalOrder_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .isFinalOrder(YesOrNo.YES)
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT.name())
                .caseDetails(CaseDetails.builder().state(All_FINAL_ORDERS_ISSUED.toString()).build()).build()).build();

        when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
        handler.handle(params);

        // Then
        HashMap<String, Object> scenarioParams = new HashMap<>();
        verifyDeleteNotificationsAndTaskListUpdates(caseData);
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_UPDATE_DASHBOARD_DEFENDANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldRecordScenarioDefendantFinalOrderFastTrackNotReadyTrial_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .isFinalOrder(YesOrNo.YES)
            .build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT.name())
                .caseDetails(CaseDetails.builder().state(All_FINAL_ORDERS_ISSUED.toString()).build()).build()).build();

        when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
        handler.handle(params);

        // Then
        verifyDeleteNotificationsAndTaskListUpdates(caseData);
        HashMap<String, Object> scenarioParams = new HashMap<>();
        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_DEFENDANT.getScenario(),
            "BEARER_TOKEN",
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    private void verifyDeleteNotificationsAndTaskListUpdates(CaseData caseData) {
        verify(dashboardApiClient).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT",
            ConfirmOrderReviewDefendantNotificationHandler.GA,
            "BEARER_TOKEN"
        );

        verify(dashboardApiClient).deleteNotificationsForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT",
            "BEARER_TOKEN"
        );
    }

}
