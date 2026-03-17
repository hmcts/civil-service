package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class ClaimantCcjResponseClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimantCcjResponseClaimantDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioWhenEligible() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordWhenToggleDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenClaimantDidNotRequestCcj() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.SIGN_A_SETTLEMENT_AGREEMENT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenApplicantIsRepresented() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse claimantResponse = new ClaimantLiPResponse();
        claimantResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenCaseDataLipIsMissing() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(null);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
