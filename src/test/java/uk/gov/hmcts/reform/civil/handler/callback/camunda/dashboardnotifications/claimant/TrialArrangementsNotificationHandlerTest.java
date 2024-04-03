package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

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
import uk.gov.hmcts.reform.civil.enums.FeeType;
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
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.*;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.*;

@ExtendWith(MockitoExtension.class)
public class TrialArrangementsNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @InjectMocks
    private HwFDashboardNotificationsHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @ParameterizedTest
        @MethodSource("provideHwfEventsForConfigureScenario")
        void shouldConfigureScenariosForHwfEvents(CaseEvent hwfEvent, DashboardScenarios dashboardScenario) {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .buildClaimIssuedPaymentCaseData();
            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.CLAIMISSUED)
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                           .hwfCaseEvent(hwfEvent)
                                           .build())
                .build();

            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));

            Map<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("typeOfFee", "claim");
            scenarioParams.put("claimIssueRemissionAmount", "£1000");
            scenarioParams.put("claimIssueOutStandingAmount", "£25");
            scenarioParams.put("claimFee", "£455");

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CLAIMANT1_HWF_DASHBOARD_NOTIFICATION.name()).build()
            ).build();

            //When
            handler.handle(params);

            //Then
            verify(dashboardApiClient, times(1)).recordScenario(
                caseData.getCcdCaseReference().toString(),
                dashboardScenario.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @ParameterizedTest
        @MethodSource("provideHwfEventsForConfigureScenario")
        void shouldNotConfigureScenariosForHwfEvents(CaseEvent hwfEvent, DashboardScenarios dashboardScenario) {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .buildClaimIssuedPaymentCaseData();
            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.HEARING)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CLAIMANT1_HWF_DASHBOARD_NOTIFICATION.name()).build()
            ).build();

            //When
            handler.handle(params);

            //Then
            verify(dashboardApiClient, times(0)).recordScenario(
                caseData.getCcdCaseReference().toString(),
                dashboardScenario.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(Map.of()).build()
            );
        }

        private static Stream<Arguments> provideHwfEventsForConfigureScenario() {
            return Stream.of(
                Arguments.of(PARTIAL_REMISSION_HWF_GRANTED, SCENARIO_AAA6_CLAIM_ISSUE_HWF_PART_REMISSION),
                Arguments.of(INVALID_HWF_REFERENCE, SCENARIO_AAA6_CLAIM_ISSUE_HWF_INVALID_REF),
                Arguments.of(MORE_INFORMATION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_INFO_REQUIRED),
                Arguments.of(UPDATE_HELP_WITH_FEE_NUMBER, SCENARIO_AAA6_CLAIM_ISSUE_HWF_UPDATED),
                Arguments.of(NO_REMISSION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_NO_REMISSION),
                Arguments.of(FULL_REMISSION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_FULL_REMISSION)
            );
        }
    }
}
