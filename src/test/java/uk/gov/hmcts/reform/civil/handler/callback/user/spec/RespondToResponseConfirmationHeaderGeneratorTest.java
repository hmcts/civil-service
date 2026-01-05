package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.AcceptPartAdmitAndPaidConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.AdmitNotProceedConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.JudgmentByAdmissionConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.PayImmediatelyHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.AdmitProceedConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.DefendNotProceedConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.DefendProceedConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.JudgmentSubmittedConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.ProposePaymentPlanConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.RejectWithMediationConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.RejectWithoutMediationConfHeader;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;

public class RespondToResponseConfirmationHeaderGeneratorTest implements CaseDataToTextGeneratorTest
    .CaseDataToTextGeneratorIntentionConfig<RespondToResponseConfirmationHeaderGenerator> {

    @Override
    public Class<RespondToResponseConfirmationHeaderGenerator> getIntentionInterface() {
        return RespondToResponseConfirmationHeaderGenerator.class;
    }

    @Override
    public List<Pair<CaseData,
        Class<? extends RespondToResponseConfirmationHeaderGenerator>>>
        getCasesToExpectedImplementation() {
        return List.of(
            Pair.of(buildFullAdmitPayImmediatelyProceedCaseData(), PayImmediatelyHeader.class),
            Pair.of(buildPartAdmitPayImmediatelyProceedCaseData(), PayImmediatelyHeader.class),
            Pair.of(buildFullAdmitProceedCaseData(), AdmitProceedConfHeader.class),
            Pair.of(buildFullAdmitNotProceedCaseData(), AdmitNotProceedConfHeader.class),
            Pair.of(buildPartAdmitProceedCaseData(), AdmitProceedConfHeader.class),
            Pair.of(buildPartAdmitNotProceedCaseData(), AdmitNotProceedConfHeader.class),
            Pair.of(buildFullDefenceProceedCaseData(), DefendProceedConfHeader.class),
            Pair.of(buildFullDefenceNotProceedCaseData(), DefendNotProceedConfHeader.class),
            Pair.of(buildJudgmentSubmitProceedCaseData(), JudgmentSubmittedConfHeader.class),
            Pair.of(buildProposePaymentPlanCaseData(), ProposePaymentPlanConfHeader.class),
            Pair.of(buildProposePaymentPlanCaseData_PartAdmit(), ProposePaymentPlanConfHeader.class),
            Pair.of(buildCaseWithMediation(), RejectWithMediationConfHeader.class),
            Pair.of(buildAcceptPartAdmitAndPaidCaseData(), AcceptPartAdmitAndPaidConfHeader.class),
            Pair.of(buildCaseDefendantWithOutMediationData(), RejectWithoutMediationConfHeader.class),
            Pair.of(buildCaseWithOutMediationFastTrackData(), RejectWithoutMediationConfHeader.class),
            Pair.of(buildCaseClaimantWithOutMediationData(), RejectWithoutMediationConfHeader.class),
            Pair.of(buildJudgmentSubmitProceedCaseDataAllFoi(), JudgmentByAdmissionConfHeader.class)
        );
    }

    public static CaseData buildFullDefenceProceedCaseData() {
        return CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();
    }

    public static CaseData buildFullDefenceNotProceedCaseData() {
        return CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();
    }

    public static CaseData buildFullAdmitProceedCaseData() {
        return CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();
    }

    public static CaseData buildFullAdmitPayImmediatelyProceedCaseData() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY).build();
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(LocalDate.now().plusDays(5));
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        return caseData;
    }

    public static CaseData buildPartAdmitPayImmediatelyProceedCaseData() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY).build();
        caseData.setRespondForImmediateOption(YesOrNo.YES);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES);
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(LocalDate.now().plusDays(5));
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        return caseData;
    }

    public static CaseData buildFullAdmitNotProceedCaseData() {
        return CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();
    }

    public static CaseData buildPartAdmitProceedCaseData() {
        return CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();
    }

    public static CaseData buildPartAdmitNotProceedCaseData() {
        return CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .build();
    }

    public static CaseData buildJudgmentSubmitProceedCaseData() {
        return CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .build();
    }

    public static CaseData buildJudgmentSubmitProceedCaseDataAllFoi() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .build();
        caseData.setCcdState(CaseState.All_FINAL_ORDERS_ISSUED);
        return caseData;
    }

    public static CaseData buildProposePaymentPlanCaseData() {
        return CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .build();
    }

    public static CaseData buildCaseWithMediation() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        ClaimantMediationLip claimantMediationLip = new ClaimantMediationLip();
        claimantMediationLip.setHasAgreedFreeMediation(MediationDecision.Yes);
        caseDataLiP.setApplicant1ClaimMediationSpecRequiredLip(claimantMediationLip);
        caseData.setCaseDataLiP(caseDataLiP);
        return caseData;
    }

    public static CaseData buildAcceptPartAdmitAndPaidCaseData() {
        return CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent1(PartyBuilder.builder().company().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .applicant1ProceedWithClaim(null)
            .build();
    }

    public static CaseData buildCaseDefendantWithOutMediationData() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent1(PartyBuilder.builder().company().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .applicant1ProceedWithClaim(null)
            .responseClaimTrack(SMALL_CLAIM.name())
            .build();
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
        return caseData;
    }

    public static CaseData buildCaseWithOutMediationFastTrackData() {
        return CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent1(PartyBuilder.builder().company().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .responseClaimTrack(FAST_CLAIM.name())
            .build();
    }

    public static CaseData buildCaseClaimantWithOutMediationData() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent1(PartyBuilder.builder().company().build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO).build();
        caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
        ClaimantMediationLip claimantMediationLip = new ClaimantMediationLip();
        claimantMediationLip.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP  caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1ClaimMediationSpecRequiredLip(
            claimantMediationLip);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setResponseClaimTrack(SMALL_CLAIM.name());
        return caseData;
    }

    public static CaseData buildProposePaymentPlanCaseData_PartAdmit() {
        return CaseDataBuilder.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .build();
    }
}
