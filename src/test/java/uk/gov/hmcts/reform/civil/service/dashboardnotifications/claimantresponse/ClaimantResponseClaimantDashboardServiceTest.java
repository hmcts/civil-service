package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT;
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

    private static final String AUTH_TOKEN = "BEARER_TOKEN";
    private static final String CASE_REFERENCE = "1234";

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
    void setup() {
        service = new ClaimantResponseClaimantDashboardService(
            dashboardScenariosService,
            mapper,
            featureToggleService,
            dashboardNotificationService,
            taskListService
        );
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    private CaseData.CaseDataBuilder<?, ?> baseCaseDataBuilder() {
        return CaseData.builder()
            .ccdCaseReference(Long.valueOf(CASE_REFERENCE))
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().type(Party.Type.COMPANY).build());
    }

    @Test
    void shouldRecordCaseSettledScenarioAndClearClaimantTasks() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CASE_SETTLED)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(CASE_REFERENCE, "CLAIMANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(CASE_REFERENCE, "CLAIMANT");
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIM_SETTLED_CLAIMANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordGeneralApplicationScenarioWhenProceedingInHeritageSystem() {
        ClaimantLiPResponse response = ClaimantLiPResponse.builder()
            .claimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE)
            .build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1LiPResponse(response)
            .build();

        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_JUDGE_PLAN_REQUESTED_CCJ_CLAIMANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
        verifyNoInteractions(dashboardNotificationService, taskListService);
    }

    @Test
    void shouldSkipScenarioWhenApplicantRepresented() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CASE_SETTLED)
            .applicant1Represented(YesOrNo.YES)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService, dashboardNotificationService, taskListService);
    }

    @ParameterizedTest
    @EnumSource(value = AllocatedTrack.class, names = {"MULTI_CLAIM", "INTERMEDIATE_CLAIM"})
    void shouldRecordMintiScenarioWhenAwaitingApplicantIntention(AllocatedTrack track) {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .responseClaimTrack(track.name())
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_MULTI_INT_CLAIMANT_INTENT_CLAIMANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioWhenNoMatchingConditionsAndInstallmentForIndividual() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_STAYED)
            .responseClaimTrack(AllocatedTrack.MULTI_CLAIM.name())
            .defenceRouteRequired(DISPUTES_THE_CLAIM)
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarioWhenPayBySetDateButRepaymentPlanAccepted() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
            .build();
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordJudicialReferralScenario() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_GO_TO_HEARING.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordMediationScenarioWhenCarmDisabled() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.IN_MEDIATION)
            .build();
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_MEDIATION.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordCarmMediationScenarioWhenEnabled() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.IN_MEDIATION)
            .build();
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_CLAIMANT_CARM.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordSettlementAgreementScenario() {
        ClaimantLiPResponse response = ClaimantLiPResponse.builder()
            .applicant1SignedSettlementAgreement(YesOrNo.YES)
            .build();
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1LiPResponse(response)
            .build();

        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .caseDataLiP(caseDataLiP)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_AGREEMENT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordClaimantEndsClaimScenario() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_STAYED)
            .defenceRouteRequired(DISPUTES_THE_CLAIM)
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CLAIMANT_ENDS_CLAIM_CLAIMANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldRecordRejectRepaymentPlanScenarioBasedOnJudgmentOnline(boolean judgmentOnlineLive) {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(judgmentOnlineLive);

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        String expectedScenario = judgmentOnlineLive
            ? SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_JO_CLAIMANT.getScenario()
            : SCENARIO_AAA6_CLAIMANT_INTENT_REJECT_REPAYMENT_ORG_LTD_CO_CLAIMANT.getScenario();

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(expectedScenario),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordPartAdmitImmediatePaymentScenario() {
        CaseData caseData = baseCaseDataBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .build();

        service.notifyClaimantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIM_PART_ADMIT_CLAIMANT.getScenario()),
            eq(CASE_REFERENCE),
            any(ScenarioRequestParams.class)
        );
    }
}
