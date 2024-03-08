package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.ClaimantResponseNotificationHandler.TASK_ID;

@ExtendWith(MockitoExtension.class)
public class ClaimantResponseNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @InjectMocks
    private ClaimantResponseNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldCreateDashboardNotificationsForSignSettlementAgreement() {
            // Given
            Map<String, Object> scenarioParams  = new HashMap<>();
            scenarioParams.put("claimantSettlementAgreement", "accepted");
            scenarioParams.put("respondent1SettlementAgreementDeadline", LocalDateTime.now().plusDays(7));

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder()
                    .build().toBuilder()
                    .ccdCaseReference(1234L)
                    .caseDataLiP(CaseDataLiP.builder()
                            .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                    .applicant1SignedSettlementAgreement(YesOrNo.YES).build()
                            )
                            .build())
                    .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .build();
            // When
            handler.handle(callbackParams);

            // Then
            verify(dashboardApiClient).recordScenario(
                    caseData.getCcdCaseReference().toString(),
                    SCENARIO_AAA7_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT.getScenario(),
                    "BEARER_TOKEN",
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE").build()).build())).isEqualTo(TASK_ID);
    }

}

