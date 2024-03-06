package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;


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
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_CLAIM_SETTLED_CLAIMANT1;

@ExtendWith(MockitoExtension.class)
public class ClaimSettledClaimant1NotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @InjectMocks
    private ClaimSettledClaimant1NotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordScenario_whenInvokedWhenCaseStateIsClaimSettled() {
            CaseData caseData = CaseDataBuilder.builder().atStateLipClaimSettled().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_CLAIM_SETTLED_CLAIMANT1.name()).build()
            ).build();

            Map<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("claimSettledAmount", "500");
            scenarioParams.put("claimSettledDate", "12/01/2024");

            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            handler.handle(params);

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA7.ClaimantIntent.ClaimSettled.Claimant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordScenario_whenInvokedWhenCaseStateIsNotClaimSettled() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_CLAIM_SETTLED_CLAIMANT1.name()).build()
            ).build();

            Map<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("claimSettledAmount", "500");
            scenarioParams.put("claimSettledDate", "12/01/2024");

            handler.handle(params);

            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                "Scenario.AAA7.ClaimantIntent.ClaimSettled.Claimant",
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
