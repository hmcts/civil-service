package uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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

    @Test
    void shouldRecordScenario_whenInvoked() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(coscDashboardHelper.isMarkedPaidInFull(any())).thenReturn(false);

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1SettleClaim(YesOrNo.YES);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedSmallClaim()
            .caseDataLip(caseDataLiP)
            .respondent1Represented(YesOrNo.NO).build();

        initiateCoscDefendantDashboardService.notifyInitiateCosc(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
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

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1SettleClaim(YesOrNo.YES);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedSmallClaim()
            .caseDataLip(caseDataLiP)
            .respondent1Represented(YesOrNo.NO).build();
        caseData.setGeneralApplications(gaApplications);

        initiateCoscDefendantDashboardService.notifyInitiateCosc(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            "54326781",
            "APPLICANT"
        );

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_PROOF_OF_DEBT_PAYMENT_APPLICATION_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldNotRecordScenario_whenRespondentRepresented() {
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

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1SettleClaim(YesOrNo.YES);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedSmallClaim()
            .caseDataLip(caseDataLiP)
            .respondent1Represented(YesOrNo.YES).build();
        caseData.setGeneralApplications(gaApplications);

        initiateCoscDefendantDashboardService.notifyInitiateCosc(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verifyNoInteractions(dashboardNotificationService);
    }

    @Test
    void shouldNotRecordScenario_whenIsPaidInFull() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(coscDashboardHelper.isMarkedPaidInFull(any())).thenReturn(true);

        CaseLink caseLink = new CaseLink();
        caseLink.setCaseReference("54326781");

        GeneralApplication generalApplication = new GeneralApplication();
        generalApplication.setCaseLink(caseLink);
        GAApplicationType gaApplicationType = new GAApplicationType();
        gaApplicationType.setTypes(singletonList(CONFIRM_CCJ_DEBT_PAID));
        generalApplication.setGeneralAppType(gaApplicationType);

        List<Element<GeneralApplication>> gaApplications = wrapElements(generalApplication);

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1SettleClaim(YesOrNo.YES);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedSmallClaim()
            .caseDataLip(caseDataLiP)
            .respondent1Represented(YesOrNo.NO).build();
        caseData.setGeneralApplications(gaApplications);

        initiateCoscDefendantDashboardService.notifyInitiateCosc(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verifyNoInteractions(dashboardNotificationService);
    }

    @Test
    void shouldNotRecordScenario_whenLipVLipDisabled() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        CaseLink caseLink = new CaseLink();
        caseLink.setCaseReference("54326781");

        GeneralApplication generalApplication = new GeneralApplication();
        generalApplication.setCaseLink(caseLink);
        GAApplicationType gaApplicationType = new GAApplicationType();
        gaApplicationType.setTypes(singletonList(CONFIRM_CCJ_DEBT_PAID));
        generalApplication.setGeneralAppType(gaApplicationType);

        List<Element<GeneralApplication>> gaApplications = wrapElements(generalApplication);

        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1SettleClaim(YesOrNo.YES);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedSmallClaim()
            .caseDataLip(caseDataLiP)
            .respondent1Represented(YesOrNo.NO).build();
        caseData.setGeneralApplications(gaApplications);

        initiateCoscDefendantDashboardService.notifyInitiateCosc(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verifyNoInteractions(dashboardNotificationService);
    }
}
