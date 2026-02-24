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
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardTasksHelper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalOrderDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardNotificationHelper dashboardDecisionHelper;
    @Mock
    private DashboardTasksHelper dashboardTasksHelper;

    @InjectMocks
    private FinalOrderDefendantDashboardService finalOrderDefendantDashboardService;

    @Test
    void shouldRecordScenarioDefendantFinalOrderFastTrackNotReadyTrial_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);

        when(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);

        finalOrderDefendantDashboardService.notifyFinalOrder(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.Update.TaskList.TrialReady.FinalOrders.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verify(dashboardTasksHelper).deleteNotificationAndInactiveTasksForDefendant(caseData);
    }

    @Test
    void shouldRecordScenarioDefendantFinalOrderFastTrackTrialReady_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setTrialReadyRespondent1(YesOrNo.YES);

        when(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);

        finalOrderDefendantDashboardService.notifyFinalOrder(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.Update.Defendant.TaskList.UploadDocuments.FinalOrders",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verify(dashboardTasksHelper).deleteNotificationAndInactiveTasksForDefendant(caseData);
    }

    @Test
    void shouldNotRecordScenario_whenRespondentRepresented() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setTrialReadyRespondent1(YesOrNo.YES);

        when(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)).thenReturn(false);

        finalOrderDefendantDashboardService.notifyFinalOrder(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verify(dashboardTasksHelper).deleteNotificationAndInactiveTasksForDefendant(caseData);
    }

    @Test
    void shouldNotRecordScenario_whenLipVLipDisabled() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setTrialReadyRespondent1(YesOrNo.YES);

        when(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        finalOrderDefendantDashboardService.notifyFinalOrder(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verify(dashboardTasksHelper).deleteNotificationAndInactiveTasksForDefendant(caseData);
    }

    @Test
    void shouldNotRecordScenario_whenDashBoardDisabled() {
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setTrialReadyRespondent1(YesOrNo.YES);

        when(dashboardDecisionHelper.isOrderMadeFastTrackTrialNotResponded(caseData)).thenReturn(false);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(false);

        finalOrderDefendantDashboardService.notifyFinalOrder(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verify(dashboardTasksHelper).deleteNotificationAndInactiveTasksForDefendant(caseData);
    }
}
