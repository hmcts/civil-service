package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
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
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_CP_TRIAL_ARRANGEMENTS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_REQUIRED_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class TrialArrangementsClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private TrialArrangementsClaimantNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "GenerateDashboardClaimantNotificationTrialArrangements";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_CP_TRIAL_ARRANGEMENTS_CLAIMANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_CP_TRIAL_ARRANGEMENTS_CLAIMANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @ParameterizedTest
    @MethodSource("provideCaseData")
    void configureDashboardNotificationsForClaimant(CaseData caseData, boolean shouldRecordScenario) {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        if (shouldRecordScenario) {
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_REQUIRED_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        } else {
            verifyNoInteractions(dashboardScenariosService);
        }
    }

    private static Stream<Arguments> provideCaseData() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimIssued().build()
                    .toBuilder().applicant1Represented(YesOrNo.NO)
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .claimsTrack(ClaimsTrack.fastTrack)
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .build(),
                true
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimIssued().build()
                    .toBuilder().applicant1Represented(YesOrNo.NO)
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .claimsTrack(ClaimsTrack.fastTrack)
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .trialReadyApplicant(YES)
                    .build(),
                false
            )
        );
    }

}
