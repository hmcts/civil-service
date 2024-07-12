package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_FAST_TRACK;

@ExtendWith(MockitoExtension.class)
public class CaseProceedOfflineDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CaseProceedOfflineDefendantNotificationHandler handler;
    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;
    public static final String TASK_ID = "GenerateDefendantDashboardNotificationCaseProceedOffline";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
                CallbackParamsBuilder.builder()
                        .request(CallbackRequest.builder()
                                .eventId(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE.name())
                                .build())
                        .build()))
                .isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldRecordScenario_whenInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                    .respondent1Represented(YesOrNo.NO)
                    .ccdCaseReference(12890L)
                    .previousCCDState(AWAITING_APPLICANT_INTENTION).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE.name()).build()
            ).build();
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            // When
            handler.handle(params);

            // Then
            verify(dashboardApiClient).recordScenario(
                    caseData.getCcdCaseReference().toString(),
                    SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario(),
                    "BEARER_TOKEN",
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvokedForCaseProgression() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .ccdCaseReference(12890L)
                .previousCCDState(CASE_PROGRESSION).build();

            when(toggleService.isLipVLipEnabled()).thenReturn(true);
            when(toggleService.isCaseProgressionEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE.name()).build()
            ).build();

            // When
            handler.handle(params);

            // Then
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvokedForCaseProgressionOnFastTrackClaim() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .ccdCaseReference(12890L)
                .responseClaimTrack(AllocatedTrack.FAST_CLAIM.toString())
                .previousCCDState(CASE_PROGRESSION).build();

            when(toggleService.isLipVLipEnabled()).thenReturn(true);
            when(toggleService.isCaseProgressionEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE.name()).build()
            ).build();

            // When
            handler.handle(params);

            // Then
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_FAST_TRACK.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenario_whenInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                    .respondent1Represented(YesOrNo.NO)
                    .ccdCaseReference(12890L)
                    .previousCCDState(PENDING_CASE_ISSUED).build();

            when(toggleService.isLipVLipEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE.name()).build()
            ).build();

            // When
            handler.handle(params);

            // Then
            verify(dashboardApiClient, never()).recordScenario(
                    caseData.getCcdCaseReference().toString(),
                    SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario(),
                    "BEARER_TOKEN",
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenario_whenCaseProgressionIsNotEnabledAndIsOnCaseProgressionState() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .ccdCaseReference(12890L)
                .previousCCDState(CASE_PROGRESSION).build();

            when(toggleService.isLipVLipEnabled()).thenReturn(true);
            when(toggleService.isCaseProgressionEnabled()).thenReturn(false);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE.name()).build()
            ).build();

            // When
            handler.handle(params);

            // Then
            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenario_whenNotInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                    .respondent1Represented(YesOrNo.NO)
                    .ccdCaseReference(12890L)
                    .build();

            when(toggleService.isLipVLipEnabled()).thenReturn(true);
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE.name()).build()
            ).build();

            // When
            handler.handle(params);

            // Then
            verify(dashboardApiClient, never()).recordScenario(
                    caseData.getCcdCaseReference().toString(),
                    SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario(),
                    "BEARER_TOKEN",
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
