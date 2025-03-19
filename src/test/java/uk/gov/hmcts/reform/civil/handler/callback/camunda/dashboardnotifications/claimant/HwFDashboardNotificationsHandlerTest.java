package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT1_HWF_DASHBOARD_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_FULL_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_INFO_REQUIRED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_INVALID_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_NO_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_PART_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_UPDATED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_FULL_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_INFO_REQUIRED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_INVALID_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_NO_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_PART_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_UPDATED;

@ExtendWith(MockitoExtension.class)
class HwFDashboardNotificationsHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private HwFDashboardNotificationsHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @ParameterizedTest
        @MethodSource("provideClaimIssueHwfEventsForConfigureScenario")
        void shouldConfigureScenariosForClaimIssueHwfEvents(CaseEvent hwfEvent, DashboardScenarios dashboardScenario) {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .buildClaimIssuedPaymentCaseData();
            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.CLAIMISSUED)
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                           .hwfCaseEvent(hwfEvent)
                                           .build())
                .applicant1Represented(YesOrNo.NO)
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
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
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                dashboardScenario.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @ParameterizedTest
        @MethodSource("provideHearingFeeHwfEventsForConfigureScenario")
        void shouldConfigureScenariosForHearingFeeHwfEvents(CaseEvent hwfEvent, DashboardScenarios dashboardScenario) {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .buildMakePaymentsCaseDataWithHearingDueDateWithHearingFeePBADetails();
            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.HEARING)
                .hearingHwfDetails(HelpWithFeesDetails.builder()
                                           .hwfCaseEvent(hwfEvent)
                                           .build())
                .applicant1Represented(YesOrNo.NO)
                .build();

            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("hearingFeeRemissionAmount", "£1000");
            scenarioParams.put("hearingFeeOutStandingAmount", "£25");
            scenarioParams.put("hearingFee", "£455");

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CLAIMANT1_HWF_DASHBOARD_NOTIFICATION.name()).build()
            ).build();

            //When
            handler.handle(params);

            //Then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                dashboardScenario.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @ParameterizedTest
        @MethodSource("provideClaimIssueHwfEventsForConfigureScenario")
        void shouldNotConfigureScenariosForHwfEventsWhenFeeTypeNull(CaseEvent hwfEvent, DashboardScenarios dashboardScenario) {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .buildClaimIssuedPaymentCaseData();

            caseData = caseData.toBuilder()
                .hwfFeeType(null)
                .hearingHwfDetails(HelpWithFeesDetails.builder()
                                       .hwfCaseEvent(hwfEvent)
                                       .build())
                .applicant1Represented(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CLAIMANT1_HWF_DASHBOARD_NOTIFICATION.name()).build()
            ).build();

            //When
            handler.handle(params);

            //Then
            verifyNoInteractions(dashboardScenariosService);
        }

        @ParameterizedTest
        @MethodSource("provideClaimIssueHwfEventsForConfigureScenario")
        void shouldNotConfigureScenariosForHwfEventsWhenHwfDetailsNull(CaseEvent hwfEvent, DashboardScenarios dashboardScenario) {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .buildClaimIssuedPaymentCaseData();

            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.HEARING)
                .hearingHwfDetails(null)
                .applicant1Represented(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CLAIMANT1_HWF_DASHBOARD_NOTIFICATION.name()).build()
            ).build();

            //When
            handler.handle(params);

            //Then
            verifyNoInteractions(dashboardScenariosService);
        }

        @ParameterizedTest
        @MethodSource("provideClaimIssueHwfEventsForConfigureScenario")
        void shouldNotConfigureScenariosForHwfEventsWhenRepresented(CaseEvent hwfEvent, DashboardScenarios dashboardScenario) {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .buildClaimIssuedPaymentCaseData();

            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.HEARING)
                .hearingHwfDetails(HelpWithFeesDetails.builder()
                                .hwfCaseEvent(hwfEvent)
                                .build())
                .applicant1Represented(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CLAIMANT1_HWF_DASHBOARD_NOTIFICATION.name()).build()
            ).build();

            //When
            handler.handle(params);

            //Then
            verifyNoInteractions(dashboardScenariosService);
        }

        private static Stream<Arguments> provideClaimIssueHwfEventsForConfigureScenario() {
            return Stream.of(
                Arguments.of(PARTIAL_REMISSION_HWF_GRANTED, SCENARIO_AAA6_CLAIM_ISSUE_HWF_PART_REMISSION),
                Arguments.of(INVALID_HWF_REFERENCE, SCENARIO_AAA6_CLAIM_ISSUE_HWF_INVALID_REF),
                Arguments.of(MORE_INFORMATION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_INFO_REQUIRED),
                Arguments.of(UPDATE_HELP_WITH_FEE_NUMBER, SCENARIO_AAA6_CLAIM_ISSUE_HWF_UPDATED),
                Arguments.of(NO_REMISSION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_NO_REMISSION),
                Arguments.of(FULL_REMISSION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_FULL_REMISSION)
            );
        }

        private static Stream<Arguments> provideHearingFeeHwfEventsForConfigureScenario() {
            return Stream.of(
                Arguments.of(PARTIAL_REMISSION_HWF_GRANTED, SCENARIO_AAA6_HEARING_FEE_HWF_PART_REMISSION),
                Arguments.of(INVALID_HWF_REFERENCE, SCENARIO_AAA6_HEARING_FEE_HWF_INVALID_REF),
                Arguments.of(MORE_INFORMATION_HWF, SCENARIO_AAA6_HEARING_FEE_HWF_INFO_REQUIRED),
                Arguments.of(UPDATE_HELP_WITH_FEE_NUMBER, SCENARIO_AAA6_HEARING_FEE_HWF_UPDATED),
                Arguments.of(NO_REMISSION_HWF, SCENARIO_AAA6_HEARING_FEE_HWF_NO_REMISSION),
                Arguments.of(FULL_REMISSION_HWF, SCENARIO_AAA6_HEARING_FEE_HWF_FULL_REMISSION)
            );
        }
    }
}
