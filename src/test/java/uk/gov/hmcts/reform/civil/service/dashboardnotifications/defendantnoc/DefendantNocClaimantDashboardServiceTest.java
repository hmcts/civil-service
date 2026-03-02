package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantnoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_JBA_CLAIM_MOVES_OFFLINE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class DefendantNocClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private SdoCaseClassificationService sdoCaseClassificationService;

    @InjectMocks
    private DefendantNocClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordOfflineScenarioWhenNocOnlineDisabled() {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.SUCCESS);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setHearingFeePaymentDetails(paymentDetails);

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(false);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "CLAIMANT");
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordJbaScenarioWhenOnlineAndJudgmentOnline() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.SUCCESS);

        JudgmentDetails judgmentDetails = new JudgmentDetails();

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setPreviousCCDState(CaseState.All_FINAL_ORDERS_ISSUED);
        caseData.setActiveJudgment(judgmentDetails);
        caseData.setHearingFeePaymentDetails(paymentDetails);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService, never()).deleteByReferenceAndCitizenRole(any(), any());
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_JBA_CLAIM_MOVES_OFFLINE_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordMoveOfflineScenarioWhenOnlineEnabledWithoutActiveJudgment() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.SUCCESS);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setPreviousCCDState(CaseState.All_FINAL_ORDERS_ISSUED);
        caseData.setHearingFeePaymentDetails(paymentDetails);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService, never()).deleteByReferenceAndCitizenRole(any(), any());
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordMoveOfflineScenarioWhenPreviousStateNotFinalOrdersIssued() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.SUCCESS);

        JudgmentDetails judgmentDetails = new JudgmentDetails();

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setPreviousCCDState(CaseState.AWAITING_APPLICANT_INTENTION);
        caseData.setActiveJudgment(judgmentDetails);
        caseData.setHearingFeePaymentDetails(paymentDetails);
        caseData.setTrialReadyApplicant(YesOrNo.YES);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordWhenNotProceedingOffline() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardNotificationService, dashboardScenariosService);
    }

    @Test
    void shouldRecordHwfHearingFeeScenarioWhenOutcomePending() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(false);

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.FAILED);

        FeePaymentOutcomeDetails feeOutcome = new FeePaymentOutcomeDetails();

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setHwfFeeType(FeeType.HEARING);
        caseData.setHearingFeePaymentDetails(paymentDetails);
        caseData.setFeePaymentOutcomeDetails(feeOutcome);
        caseData.setTrialReadyApplicant(YesOrNo.YES);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordExtraScenariosWhenFeePaidAndTrialReadySet() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(false);

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.SUCCESS);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setHearingFeePaymentDetails(paymentDetails);
        caseData.setTrialReadyApplicant(YesOrNo.YES);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordAdditionalScenariosWhenEligible() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(false);
        when(sdoCaseClassificationService.isFastTrack(caseData)).thenReturn(true);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordTrialArrangementsWhenNotFastTrack() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(false);

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.SUCCESS);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
        caseData.setHearingFeePaymentDetails(paymentDetails);
        when(sdoCaseClassificationService.isFastTrack(caseData)).thenReturn(false);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordHearingFeeScenarioWhenHwfRemissionGranted() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(false);

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setStatus(PaymentStatus.FAILED);

        FeePaymentOutcomeDetails feeOutcome = new FeePaymentOutcomeDetails();
        feeOutcome.setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setHwfFeeType(FeeType.HEARING);
        caseData.setHearingFeePaymentDetails(paymentDetails);
        caseData.setFeePaymentOutcomeDetails(feeOutcome);
        caseData.setTrialReadyApplicant(YesOrNo.YES);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordWhenLipVlipsDisabled() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardNotificationService, dashboardScenariosService);
    }
}
