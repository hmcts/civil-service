package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_JUDGEMENTS_ONLINE_ISSUED_CCJ_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class JudgmentByAdmissionIssuedClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private JudgmentByAdmissionIssuedClaimantDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioWhenIndividualJudgmentIssued() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);

        JudgmentDetails activeJudgment = new JudgmentDetails();
        activeJudgment.setState(JudgmentState.ISSUED);
        activeJudgment.setType(JudgmentType.JUDGMENT_BY_ADMISSION);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1(respondent1);
        caseData.setActiveJudgment(activeJudgment);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_UPDATE_JUDGEMENTS_ONLINE_ISSUED_CCJ_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioWhenCompanyAcceptedRepaymentPlan() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        Party respondent1 = new Party();
        respondent1.setType(Party.Type.COMPANY);

        JudgmentDetails activeJudgment = new JudgmentDetails();
        activeJudgment.setState(JudgmentState.ISSUED);
        activeJudgment.setType(JudgmentType.JUDGMENT_BY_ADMISSION);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setRespondent1(respondent1);
        caseData.setActiveJudgment(activeJudgment);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_UPDATE_JUDGEMENTS_ONLINE_ISSUED_CCJ_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordWhenToggleDisabled() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);

        JudgmentDetails activeJudgment = new JudgmentDetails();
        activeJudgment.setState(JudgmentState.ISSUED);
        activeJudgment.setType(JudgmentType.JUDGMENT_BY_ADMISSION);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1(respondent1);
        caseData.setActiveJudgment(activeJudgment);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenJudgmentNotIssued() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);

        JudgmentDetails activeJudgment = new JudgmentDetails();
        activeJudgment.setState(JudgmentState.REQUESTED);
        activeJudgment.setType(JudgmentType.JUDGMENT_BY_ADMISSION);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1(respondent1);
        caseData.setActiveJudgment(activeJudgment);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenApplicantIsRepresented() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);

        JudgmentDetails activeJudgment = new JudgmentDetails();
        activeJudgment.setState(JudgmentState.ISSUED);
        activeJudgment.setType(JudgmentType.JUDGMENT_BY_ADMISSION);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1(respondent1);
        caseData.setActiveJudgment(activeJudgment);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenJudgmentTypeIsNotByAdmission() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);

        JudgmentDetails activeJudgment = new JudgmentDetails();
        activeJudgment.setState(JudgmentState.ISSUED);
        activeJudgment.setType(JudgmentType.DEFAULT_JUDGMENT);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1(respondent1);
        caseData.setActiveJudgment(activeJudgment);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenActiveJudgmentMissing() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1(respondent1);
        caseData.setActiveJudgment(null);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenCompanyWithoutAcceptedPlan() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.COMPANY);

        JudgmentDetails activeJudgment = new JudgmentDetails();
        activeJudgment.setState(JudgmentState.ISSUED);
        activeJudgment.setType(JudgmentType.JUDGMENT_BY_ADMISSION);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);
        caseData.setRespondent1(respondent1);
        caseData.setActiveJudgment(activeJudgment);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
