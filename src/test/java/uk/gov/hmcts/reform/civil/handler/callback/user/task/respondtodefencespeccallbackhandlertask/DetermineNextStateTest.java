package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.INTERMEDIATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class DetermineNextStateTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private JudgmentByAdmissionOnlineMapper judgmentByAdmissionOnlineMapper;

    @InjectMocks
    private DetermineNextState determineNextState;

    @Test
    void shouldDetermineNextStateWhenCallbackIsVersion1() {

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();
        BusinessProcess businessProcess = BusinessProcess.builder().build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateLipClaimantDoesNotSettle()
            .applicant1ProceedWithClaim(YES)
            .responseClaimTrack(SMALL_CLAIM)
            .build();

        CallbackParams params = callbackParams(caseData)
            .builder()
            .version(CallbackVersion.V_1)
            .build();

        when(featureToggleService.isCarmEnabledForCase(any(CaseData.class))).thenReturn(true);

        String resultState = determineNextState.determineNextState(caseData, params,
                                                                 builder, "", businessProcess);

        CaseData builtCaseData = builder.build();

        assertEquals(IN_MEDIATION.name(), resultState);
        assertEquals(CLAIMANT_RESPONSE_SPEC.name(), builtCaseData.getBusinessProcess().getCamundaEvent());
    }

    @Test
    void shouldSetStateInMediationWhenClaimantAgreeToFreeMediation() {

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();
        BusinessProcess businessProcess = BusinessProcess.builder().build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessful(MultiPartyScenario.ONE_V_ONE)
            .build();

        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

        String resultState = determineNextState.determineNextState(caseData, callbackParams(caseData),
                                                                   builder, "", businessProcess);
        assertEquals(IN_MEDIATION.name(), resultState);
    }

    @Test
    void shouldSetStateAllFinalOrdersIssuedWhenApplicantAcceptedRepaymentPlan() {

        CaseData.CaseDataBuilder<?, ?> builder = mock(CaseData.CaseDataBuilder.class);
        BusinessProcess businessProcess = BusinessProcess.builder().build();

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1AcceptPartAdmitPaymentPlanSpec(YES)
            .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1Represented(NO)
            .applicant1Represented(YES)
            .build();

        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        JudgmentDetails activeJudgment = mock(JudgmentDetails.class);

        when(activeJudgment.getTotalAmount()).thenReturn("1000");
        when(activeJudgment.getOrderedAmount()).thenReturn("500");
        when(activeJudgment.getCosts()).thenReturn("150");
        when(activeJudgment.getClaimFeeAmount()).thenReturn("50");
        when(activeJudgment.getAmountAlreadyPaid()).thenReturn("200");

        when(judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData)).thenReturn(activeJudgment);

        String resultState = determineNextState.determineNextState(caseData, callbackParams(caseData),
                                                                   builder, "", businessProcess);

        assertEquals(All_FINAL_ORDERS_ISSUED.name(), resultState);
        verify(builder).activeJudgment(activeJudgment);
        verify(builder).joIsLiveJudgmentExists(YesOrNo.YES);
    }

    @Test
    void shouldSetProceedsInHeritageSystemWhenApplicantRejectedRepaymentPlan() {

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();
        BusinessProcess businessProcess = BusinessProcess.builder().build();

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1AcceptPartAdmitPaymentPlanSpec(NO)
            .build();

        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

        String resultState = determineNextState.determineNextState(caseData, callbackParams(caseData),
                                                                   builder, "", businessProcess);

        assertNotNull(resultState);
        assertEquals(PROCEEDS_IN_HERITAGE_SYSTEM.name(), resultState);
    }

    @Test
    void shouldSetStateJudicialReferralWhenClaimIsNotSettled() {

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();
        BusinessProcess businessProcess = BusinessProcess.builder().build();

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1AcceptAdmitAmountPaidSpec(NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .responseClaimTrack(FAST_CLAIM.name())
            .build();

        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

        String resultState = determineNextState.determineNextState(caseData, callbackParams(caseData),
                                                                   builder, "", businessProcess);

        assertNotNull(resultState);
        assertEquals(JUDICIAL_REFERRAL.name(), resultState);
    }

    @Test
    void shouldNotSetStateWhenMultiClaimIsNotSettled() {

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();
        BusinessProcess businessProcess = BusinessProcess.builder().build();

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1AcceptAdmitAmountPaidSpec(NO)
            .applicant1ProceedWithClaim(YES)
            .respondent1Represented(NO)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .responseClaimTrack(MULTI_CLAIM.name())
            .build();

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        String resultState = determineNextState.determineNextState(caseData, callbackParams(caseData),
                                                                   builder, "", businessProcess);

        assertNotNull(resultState);
        assertEquals(AWAITING_APPLICANT_INTENTION.name(), resultState);
    }

    @Test
    void shouldNotSetStateWhenIntermediateClaimIsNotSettled() {

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();
        BusinessProcess businessProcess = BusinessProcess.builder().build();

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1ProceedWithClaim(YES)
            .applicant1AcceptAdmitAmountPaidSpec(NO)
            .respondent1Represented(NO)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .responseClaimTrack(INTERMEDIATE_CLAIM.name())
            .build();

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        String resultState = determineNextState.determineNextState(caseData, callbackParams(caseData),
                                                                   builder, "", businessProcess);

        assertNotNull(resultState);
        assertEquals(AWAITING_APPLICANT_INTENTION.name(), resultState);
    }

    @Test
    void shouldSetStateCaseSettledWhenClaimIsPartAdmitSettled() {

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();
        BusinessProcess businessProcess = BusinessProcess.builder().build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YES)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
            .build();

        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

        String resultState = determineNextState.determineNextState(caseData, callbackParams(caseData),
                                                                   builder, "", businessProcess);

        assertNotNull(resultState);
        assertEquals(CASE_SETTLED.name(), resultState);
    }

    @Test
    void shouldSetStateCaseStayedWhenItsLipVLipOneVOne() {

        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder();
        BusinessProcess businessProcess = BusinessProcess.builder().build();

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build()
            .toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent1Represented(NO)
            .applicant1Represented(YES)
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .applicant1ProceedWithClaim(NO)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM)
            .build();

        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        String resultState = determineNextState.determineNextState(caseData, callbackParams(caseData),
                                                                   builder, "", businessProcess);

        assertNotNull(resultState);
        assertEquals(CASE_STAYED.name(), resultState);
    }

    @Test
    void shouldSetProceedsInHeritageSystemWhenApplicantAcceptedRepaymentPlanAndNotLrVLip() {

        CaseData.CaseDataBuilder<?, ?> builder = mock(CaseData.CaseDataBuilder.class);
        BusinessProcess businessProcess = BusinessProcess.builder().build();

        CaseData caseData = CaseDataBuilder.builder()
            .applicant1AcceptPartAdmitPaymentPlanSpec(YES)
            .respondent1Represented(YES)
            .applicant1Represented(YES)
            .build();
        JudgmentDetails activeJudgment = mock(JudgmentDetails.class);
        
        when(activeJudgment.getTotalAmount()).thenReturn("1000");
        when(activeJudgment.getOrderedAmount()).thenReturn("500");
        when(activeJudgment.getCosts()).thenReturn("150");
        when(activeJudgment.getClaimFeeAmount()).thenReturn("50");
        when(activeJudgment.getAmountAlreadyPaid()).thenReturn("200");
        when(judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData)).thenReturn(activeJudgment);
        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        String resultState = determineNextState.determineNextState(caseData, callbackParams(caseData),
                                                                   builder, "", businessProcess);

        assertNotNull(resultState);
        assertEquals(PROCEEDS_IN_HERITAGE_SYSTEM.name(), resultState);
        verify(builder).activeJudgment(activeJudgment);
        verify(builder).joIsLiveJudgmentExists(YesOrNo.YES);
    }

    private CallbackParams callbackParams(CaseData caseData) {

        return CallbackParams.builder()
            .caseData(caseData)
            .version(CallbackVersion.V_2)
            .params(Map.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }
}
