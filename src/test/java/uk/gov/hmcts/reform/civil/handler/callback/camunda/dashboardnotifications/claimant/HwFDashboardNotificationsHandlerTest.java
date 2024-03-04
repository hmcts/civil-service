package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT1_HWF_DASHBOARD_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA7_CLAIM_ISSUE_HWF_INVALID_REF;

@ExtendWith(MockitoExtension.class)
public class HwFDashboardNotificationsHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @InjectMocks
    private HwFDashboardNotificationsHandler handler;

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));
        }

        @ParameterizedTest
        @MethodSource("provideHwfEventsForConfigureScenario")
        void shouldConfigureScenariosForHwfEvents(CaseEvent hwfEvent, DashboardScenarios dashboardScenario) {
            CaseData caseData = CaseDataBuilder.builder()
                .buildClaimIssuedPaymentCaseData();
            caseData = caseData.toBuilder()
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                           .hwfCaseEvent(hwfEvent)
                                           .build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CLAIMANT1_HWF_DASHBOARD_NOTIFICATION.name()).build()
            ).build();

            Map<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("typeOfFee", "claim");

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            handler.handle(params);
            verify(dashboardApiClient, times(1)).recordScenario(
                caseData.getCcdCaseReference().toString(),
                dashboardScenario.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        private static Stream<Arguments> provideHwfEventsForConfigureScenario() {
            return Stream.of(
                Arguments.of(INVALID_HWF_REFERENCE, SCENARIO_AAA7_CLAIM_ISSUE_HWF_INVALID_REF)
            );
        }
    }
}
