package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_BUNDLE_CREATED_FOR_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_BUNDLE_CREATED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_BUNDLE_CREATED_TRIAL_READY_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class BundleCreationDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private BundleCreationDefendantNotificationHandler handler;
    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
            when(toggleService.isCaseProgressionEnabled()).thenReturn(true);
        }

        @Test
        void shouldRecordScenario_whenInvokedWhenNotTrialReady() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
                .toBuilder().respondent1Represented(YesOrNo.NO)
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .orderType(OrderType.DECIDE_DAMAGES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_BUNDLE_CREATED_FOR_DEFENDANT1.name()).build()
            ).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_BUNDLE_CREATED_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvokedWhenSmallClaims() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build()
                .toBuilder().respondent1Represented(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_BUNDLE_CREATED_FOR_DEFENDANT1.name()).build()
            ).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_BUNDLE_CREATED_TRIAL_READY_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvokedWhenTrialReady() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyRespondent1().build()
                .toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .orderType(OrderType.DECIDE_DAMAGES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_BUNDLE_CREATED_FOR_DEFENDANT1.name()).build()
            ).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_BUNDLE_CREATED_TRIAL_READY_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
