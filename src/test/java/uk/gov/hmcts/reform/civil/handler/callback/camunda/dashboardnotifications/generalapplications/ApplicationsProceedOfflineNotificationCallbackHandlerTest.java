package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.generalapplications;

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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
public class ApplicationsProceedOfflineNotificationCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ApplicationsProceedOfflineNotificationCallbackHandler handler;
    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;
    private static final String EVENT_ID_CLAIMANT = "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT";
    private static final String EVENT_ID_DEFENDANT = "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT";
    private static final String TASK_ID_CLAIMANT = "claimantLipApplicationOfflineDashboardNotification";
    private static final String TASK_ID_DEFENDANT = "defendantLipApplicationOfflineDashboardNotification";

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
            when(toggleService.isGeneralApplicationsEnabled()).thenReturn(true);
        }

        @Test
        void shouldNotCallRecordScenario_whenLipVLipIsDisabled() {
            when(toggleService.isLipVLipEnabled()).thenReturn(false);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, CaseData.builder().build())
                .build();

            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldNotCallRecordScenario_whenGeneralApplicationIsDisabled() {
            when(toggleService.isGeneralApplicationsEnabled()).thenReturn(false);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, CaseData.builder().build())
                .build();

            handler.handle(callbackParams);
            verify(dashboardApiClient, never())
                .recordScenario(anyString(), anyString(), anyString(), any(ScenarioRequestParams.class));
        }

        @Test
        void shouldReturnCorrectActivityId_whenClaimant() {
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder().build();
            // WHEN
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_CLAIMANT).build())
                .build();
            // THEN
            assertThat(handler.camundaActivityId(params)).isEqualTo(TASK_ID_CLAIMANT);
        }

        @Test
        void shouldReturnCorrectActivityId_whenDefendant() {
            // GIVEN
            CaseData caseData = CaseDataBuilder.builder().build();
            // WHEN
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(EVENT_ID_DEFENDANT).build())
                .build();
            // THEN
            assertThat(handler.camundaActivityId(params)).isEqualTo(TASK_ID_DEFENDANT);
        }
    }
}
