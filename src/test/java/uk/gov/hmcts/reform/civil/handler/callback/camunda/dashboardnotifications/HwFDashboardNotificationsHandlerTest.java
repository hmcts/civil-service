package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICANT_LIP_HWF_DASHBOARD_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_INVALID_REFERENCE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_MORE_INFORMATION_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_PARTIAL_REMISSION_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_HWF_REJECTED_APPLICANT;

@ExtendWith(MockitoExtension.class)
public class HwFDashboardNotificationsHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private HwFDashboardNotificationsHandler handler;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(APPLICANT_LIP_HWF_DASHBOARD_NOTIFICATION);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParams.builder().build()))
            .isEqualTo("default");
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordClaimantScenarioApplicationFee_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.APPLICATION)
                .gaHwfDetails(HelpWithFeesDetails.builder()
                                  .hwfCaseEvent(NO_REMISSION_HWF_GA)
                                  .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(APPLICANT_LIP_HWF_DASHBOARD_NOTIFICATION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPS_HWF_REJECTED_APPLICANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordClaimantScenarioAdditionalApplicationFee_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.ADDITIONAL)
                .additionalHwfDetails(HelpWithFeesDetails.builder()
                                  .hwfCaseEvent(NO_REMISSION_HWF_GA)
                                  .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(APPLICANT_LIP_HWF_DASHBOARD_NOTIFICATION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPS_HWF_REJECTED_APPLICANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordClaimantScenarioApplicationFee_whenPartialRemissionInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.APPLICATION)
                .gaHwfDetails(HelpWithFeesDetails.builder()
                                  .hwfCaseEvent(PARTIAL_REMISSION_HWF_GA)
                                  .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(APPLICANT_LIP_HWF_DASHBOARD_NOTIFICATION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPS_HWF_PARTIAL_REMISSION_APPLICANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordApplicantScenarioAdditionalApplicationFee_MoreInfo_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.ADDITIONAL)
                .additionalHwfDetails(HelpWithFeesDetails.builder()
                                          .hwfCaseEvent(MORE_INFORMATION_HWF_GA)
                                          .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();

            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(APPLICANT_LIP_HWF_DASHBOARD_NOTIFICATION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPS_HWF_MORE_INFORMATION_APPLICANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordApplicantScenarioApplicationFee_InvalidHwfRef_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.APPLICATION)
                .gaHwfDetails(HelpWithFeesDetails.builder()
                                  .hwfCaseEvent(INVALID_HWF_REFERENCE_GA)
                                  .build())
                .build();
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(APPLICANT_LIP_HWF_DASHBOARD_NOTIFICATION.name())
                    .build()
            ).build();
            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPS_HWF_INVALID_REFERENCE_APPLICANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordApplicantScenarioAdditionalApplicationFee_InvalidHwfRef_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .hwfFeeType(FeeType.ADDITIONAL)
                .additionalHwfDetails(HelpWithFeesDetails.builder()
                                          .hwfCaseEvent(INVALID_HWF_REFERENCE_GA)
                                          .build())
                .build();
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(APPLICANT_LIP_HWF_DASHBOARD_NOTIFICATION.name())
                    .build()
            ).build();
            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPS_HWF_INVALID_REFERENCE_APPLICANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

    }
}
