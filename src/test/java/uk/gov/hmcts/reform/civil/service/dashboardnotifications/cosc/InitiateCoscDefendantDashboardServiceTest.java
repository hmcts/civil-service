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
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class InitiateCoscDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private CoscDashboardHelper coscDashboardHelper;

    @InjectMocks
    private InitiateCoscDefendantDashboardService initiateCoscDefendantDashboardService;

    private CaseData parentCaseData;
    private GeneralApplicationCaseData gaCaseData;

    @BeforeEach
    void setUp() {
        gaCaseData = new GeneralApplicationCaseData().parentCaseReference("123456");
        parentCaseData = new CaseData().ccdCaseReference(123456L).build();

        when(coscDashboardHelper.getParentCaseData(gaCaseData)).thenReturn(parentCaseData);
    }

    @Test
    void shouldRecordScenario_whenInvoked() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(coscDashboardHelper.isMarkedPaidInFull(any())).thenReturn(false);

        parentCaseData.setRespondent1Represented(YesOrNo.NO);

        initiateCoscDefendantDashboardService.notifyInitiateCosc(gaCaseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_DEFENDANT.getScenario(),
            parentCaseData.getCcdCaseReference().toString(),
            new ScenarioRequestParams(scenarioParams)
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
            parentCaseData.getCcdCaseReference().toString(),
            new ScenarioRequestParams(scenarioParams)
        );
    }

    @Test
    void shouldRecordScenario_whenPaymentCompleted() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(coscDashboardHelper.isMarkedPaidInFull(any())).thenReturn(false);

        CaseLink caseLink = new CaseLink();
        caseLink.setCaseReference("54326781");

        GeneralApplication generalApplication = new GeneralApplication();
        generalApplication.setCaseLink(caseLink);
        GAApplicationType gaApplicationType = new GAApplicationType();
        gaApplicationType.setTypes(singletonList(CONFIRM_CCJ_DEBT_PAID));
        generalApplication.setGeneralAppType(gaApplicationType);

        List<Element<GeneralApplication>> gaApplications = wrapElements(generalApplication);

        parentCaseData.setGeneralApplications(gaApplications);
        parentCaseData.setRespondent1Represented(YesOrNo.NO);

        initiateCoscDefendantDashboardService.notifyInitiateCosc(gaCaseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            "54326781",
            "APPLICANT"
        );

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_DEFENDANT.getScenario(),
            parentCaseData.getCcdCaseReference().toString(),
            new ScenarioRequestParams(scenarioParams)
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
            parentCaseData.getCcdCaseReference().toString(),
            new ScenarioRequestParams(scenarioParams)
        );
    }

    @Test
    void shouldNotRecordScenario_whenRespondentRepresented() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(coscDashboardHelper.isMarkedPaidInFull(any())).thenReturn(false);

        parentCaseData.setRespondent1Represented(YesOrNo.YES);

        initiateCoscDefendantDashboardService.notifyInitiateCosc(gaCaseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verifyNoInteractions(dashboardNotificationService);
    }

    @Test
    void shouldNotRecordScenario_whenIsPaidInFull() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(coscDashboardHelper.isMarkedPaidInFull(any())).thenReturn(true);

        parentCaseData.setRespondent1Represented(YesOrNo.NO);

        initiateCoscDefendantDashboardService.notifyInitiateCosc(gaCaseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verifyNoInteractions(dashboardNotificationService);
    }

    @Test
    void shouldNotRecordScenario_whenLipVLipDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        initiateCoscDefendantDashboardService.notifyInitiateCosc(gaCaseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verifyNoInteractions(dashboardNotificationService);
    }
}
