package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_CLAIMANT1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class HearingFeeUnpaidClaimantNotificationHandlerTest {

    @InjectMocks
    private HearingFeeUnpaidClaimantNotificationHandler handler;
    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService toggleService;

    public static final String TASK_ID = "CreateHearingFeeUnpaidDashboardNotificationsForClaimant";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_CLAIMANT1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_CLAIMANT1.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(toggleService.isCaseProgressionEnabled()).thenReturn(true);
        }

        @Test
        void shouldRecordScenario_notTrialReady_whenInvoked() {
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("hearingFeeDueDateEn", DateUtils.formatDate(LocalDate.of(2024, Month.APRIL, 1)));
            scenarioParams.put("hearingFeeDueDateCy", DateUtils.formatDate(LocalDate.of(2024, Month.APRIL, 1)));
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateHearingFeeDueUnpaid().build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .orderType(OrderType.DECIDE_DAMAGES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_CLAIMANT1.name()).build()
            ).build();

            handler.handle(params);

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.Claimant",
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
            verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
                caseData.getCcdCaseReference().toString(), "CLAIMANT");
        }

        @Test
        void shouldRecordScenario_TrialReady_whenInvokedAsSmallClaims() {
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("hearingFeeDueDateEn", DateUtils.formatDate(LocalDate.of(2024, Month.APRIL, 1)));
            scenarioParams.put("hearingFeeDueDateCy", DateUtils.formatDate(LocalDate.of(2024, Month.APRIL, 1)));
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateHearingFeeDueUnpaid()
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_CLAIMANT1.name()).build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Claimant",
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordScenario_TrialReady_whenInvoked() {
            HashMap<String, Object> scenarioParams = new HashMap<>();
            scenarioParams.put("hearingFeeDueDateEn", DateUtils.formatDate(LocalDate.of(2024, Month.APRIL, 1)));
            scenarioParams.put("hearingFeeDueDateCy", DateUtils.formatDate(LocalDate.of(2024, Month.APRIL, 1)));
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateTrialReadyApplicant().build().toBuilder()
                .applicant1Represented(YesOrNo.NO)
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .orderType(OrderType.DECIDE_DAMAGES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_CLAIMANT1.name()).build()
            ).build();

            handler.handle(params);

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.CP.StrikeOut.HearingFeeUnpaid.TrialReady.Claimant",
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
