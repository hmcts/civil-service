package uk.gov.hmcts.reform.civil.service.dashboardnotifications.finalorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.utils.DashboardDecisionHelper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_DASHBOARD_CLAIMANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class FinalOrderClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardDecisionHelper dashboardDecisionHelper;

    @InjectMocks
    private FinalOrderClaimantDashboardService finalOrderClaimantDashboardService;

    @Test
    void shouldRecordScenarioClaimantFinalOrderFastTrackNotReadyTrial_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);

        when(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);

        finalOrderClaimantDashboardService.notifyFinalOrder(caseData, AUTH_TOKEN);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldRecordScenarioClaimantFinalOrderFastTrackTrialReady_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setTrialReadyApplicant(YesOrNo.YES);

        when(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);

        finalOrderClaimantDashboardService.notifyFinalOrder(caseData, AUTH_TOKEN);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_UPDATE_DASHBOARD_CLAIMANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldNotRecordScenario_whenApplicant1Represented() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);

        when(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)).thenReturn(true);

        finalOrderClaimantDashboardService.notifyFinalOrder(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenario_whenLipVLipDisabled() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);

        when(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        finalOrderClaimantDashboardService.notifyFinalOrder(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenario_whenDashBoardDisabled() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);

        when(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(false);

        finalOrderClaimantDashboardService.notifyFinalOrder(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
