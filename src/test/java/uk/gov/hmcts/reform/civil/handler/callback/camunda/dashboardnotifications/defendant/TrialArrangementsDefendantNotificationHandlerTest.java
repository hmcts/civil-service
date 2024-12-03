package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.stream.Stream;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_CP_TRIAL_ARRANGEMENTS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_REQUIRED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class TrialArrangementsDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private TrialArrangementsDefendantNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "GenerateDashboardDefendantNotificationTrialArrangements";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_CP_TRIAL_ARRANGEMENTS_DEFENDANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_CP_TRIAL_ARRANGEMENTS_DEFENDANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @ParameterizedTest
    @MethodSource("provideCaseData")
    void configureDashboardNotificationsForDefendant(CaseData caseData, boolean shouldRecordScenario) {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        if (shouldRecordScenario) {
            verify(dashboardApiClient, times(1)).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_REQUIRED_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        } else {
            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_REQUIRED_DEFENDANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }
    }

    private static Stream<Arguments> provideCaseData() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimIssued().build()
                    .toBuilder().respondent1Represented(YesOrNo.NO)
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .claimsTrack(ClaimsTrack.fastTrack)
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .build(),
                true
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimIssued().build()
                    .toBuilder().respondent1Represented(YesOrNo.NO)
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .claimsTrack(ClaimsTrack.fastTrack)
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .trialReadyRespondent1(YES)
                    .build(),
                false
            )
        );
    }

}
