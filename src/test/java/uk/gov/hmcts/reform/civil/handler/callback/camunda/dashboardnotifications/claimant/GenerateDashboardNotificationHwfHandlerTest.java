package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
public class GenerateDashboardNotificationHwfHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private GenerateDashboardNotificationHwfHandler handler;
    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;

    HashMap<String, Object> params = new HashMap<>();

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(toggleService.isLipVLipEnabled()).thenReturn(true);
        }

        @Test
        void shouldRecordScenario_whenInvoked_smallClaims() {
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTrialReadyCheck()
                .applicant1Represented(NO)
                .totalClaimAmount(new BigDecimal(1000))
                .build();
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1.name()).build()
            ).build();

            when(mapper.mapCaseDataToParams(any())).thenReturn(params);

            handler.handle(callbackParams);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.ClaimIssue.HWF.Requested",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvoked_FastTrack() {
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateTrialReadyCheck()
                .applicant1Represented(NO)
                .totalClaimAmount(new BigDecimal(10001))
                .build();
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1.name()).build()
            ).build();

            when(mapper.mapCaseDataToParams(any())).thenReturn(params);

            handler.handle(callbackParams);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.ClaimIssue.HWF.Requested",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.ClaimIssue.Claimant.FastTrack",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void shouldNotRecordScenario_whenInvoked_legalRep() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTrialReadyCheck()
                .totalClaimAmount(new BigDecimal(10001))
                .build();
            CallbackParams callbackParams = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1.name()).build()
            ).build();

            handler.handle(callbackParams);

            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.ClaimIssue.HWF.Requested",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );

            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA6.ClaimIssue.Claimant.FastTrack",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }
    }
}
