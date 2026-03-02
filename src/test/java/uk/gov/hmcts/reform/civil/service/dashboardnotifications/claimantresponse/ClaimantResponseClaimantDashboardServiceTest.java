package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_JO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;

    @InjectMocks
    private ClaimantResponseClaimantDashboardService service;

    @BeforeEach
    void setUp() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioAndCleanupWhenEligible() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.CASE_SETTLED);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "CLAIMANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("1234", "CLAIMANT");
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordGeneralApplicationScenarioWhenProceedInHeritage() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordWhenToggleDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.CASE_SETTLED);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordWhenApplicantIsRepresented() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setCcdState(CaseState.CASE_SETTLED);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldRecordScenarioForMultiTrackAwaitingIntention() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name());
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForMultiClaimAwaitingIntention() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(AllocatedTrack.MULTI_CLAIM.name());
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioForMultiTrackWhenNotAwaitingIntention() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name());
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenTrackNotMultiOrIntermediate() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(AllocatedTrack.FAST_CLAIM.name());
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldRecordScenarioForJudicialReferral() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.JUDICIAL_REFERRAL);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForMediationCarmEnabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.IN_MEDIATION);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForMediationCarmDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.IN_MEDIATION);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_MEDIATION.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForSettlementAgreement() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse response = new ClaimantLiPResponse();
        response.setApplicant1SignedSettlementAgreement(YesOrNo.YES);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(response);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForRejectedCourtDecision() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        ClaimantLiPResponse response = new ClaimantLiPResponse();
        response.setClaimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_PLAN);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(response);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCaseDataLiP(caseDataLiP);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioWhenClaimantEndsClaim() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.CASE_STAYED);
        caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.NO);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioWhenClaimantProceedsAfterCaseStayed() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.CASE_STAYED);
        caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.YES);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldRecordScenarioForRejectRepaymentCompanyWhenJoEnabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        Party respondent = new Party();
        respondent.setType(Party.Type.COMPANY);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);
        caseData.setRespondent1(respondent);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_JO_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioWhenRespondentNotCompanyForRejectedPlan() {
        Party respondent = new Party();
        respondent.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1(respondent);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenCompanyAcceptedPlan() {
        Party respondent = new Party();
        respondent.setType(Party.Type.COMPANY);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1(respondent);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldRecordScenarioForRejectRepaymentCompanyWhenJoDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        Party respondent = new Party();
        respondent.setType(Party.Type.COMPANY);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
        caseData.setApplicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO);
        caseData.setRespondent1(respondent);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordScenarioForPartAdmitImmediatePayment() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioWhenCompanyRejectsPlanButPayImmediately() {
        Party respondent = new Party();
        respondent.setType(Party.Type.COMPANY);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1(respondent);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @Test
    void shouldNotRecordScenarioWhenNoScenarioMatches() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(null);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(null);
        caseData.setRespondent1ClaimResponseTypeForSpec(null);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }
}
