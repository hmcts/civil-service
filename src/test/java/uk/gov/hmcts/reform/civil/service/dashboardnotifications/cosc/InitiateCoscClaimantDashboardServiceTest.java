package uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class InitiateCoscClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CoscDashboardHelper coscDashboardHelper;

    @InjectMocks
    private InitiateCoscClaimantDashboardService initiateCoscClaimantDashboardService;

    private CaseData parentCaseData;
    private GeneralApplicationCaseData gaCaseData;

    @BeforeEach
    void setUp() {
        gaCaseData = new GeneralApplicationCaseData().parentCaseReference("123456");
        parentCaseData = new CaseData().ccdCaseReference(123456L).build();

        when(coscDashboardHelper.getParentCaseData(gaCaseData)).thenReturn(parentCaseData);
    }

    @Test
    void shouldRecordScenario_whenInvokedCoScCaseNotMarkedPaidInFull() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(coscDashboardHelper.isMarkedPaidInFull(any())).thenReturn(false);

        parentCaseData.setApplicant1Represented(YesOrNo.NO);

        initiateCoscClaimantDashboardService.notifyInitiateCosc(gaCaseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_CLAIMANT.getScenario(),
            parentCaseData.getCcdCaseReference().toString(),
            new ScenarioRequestParams(scenarioParams)
        );
    }

    @Test
    void shouldNotRecordScenario_whenCoScCaseMarkedPaidInFull() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(coscDashboardHelper.isMarkedPaidInFull(any())).thenReturn(true);

        parentCaseData.setApplicant1Represented(YesOrNo.NO);

        initiateCoscClaimantDashboardService.notifyInitiateCosc(gaCaseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenario_whenApplicantRepresented() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(coscDashboardHelper.isMarkedPaidInFull(any())).thenReturn(false);

        parentCaseData.setApplicant1Represented(YesOrNo.YES);

        initiateCoscClaimantDashboardService.notifyInitiateCosc(gaCaseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenario_whenLipVLipDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        initiateCoscClaimantDashboardService.notifyInitiateCosc(gaCaseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
