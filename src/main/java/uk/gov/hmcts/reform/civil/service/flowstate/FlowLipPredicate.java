package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

public class FlowLipPredicate {

    private FlowLipPredicate() {

    }
  
    public static final Predicate<CaseData> isLipCase = CaseData::isLipvLipOneVOne;

    public static final Predicate<CaseData> nocSubmittedForLiPApplicant = CaseData::nocApplyForLiPClaimant;

    public static final Predicate<CaseData> isLiPvLRCase = CaseData::isLipvLROneVOne;

    public static final Predicate<CaseData> agreedToMediation = CaseData::hasClaimantAgreedToFreeMediation;

    public static final Predicate<CaseData> declinedMediation = CaseData::hasClaimantNotAgreedToFreeMediation;

    public static final Predicate<CaseData> isTranslatedDocumentUploaded = CaseData::isTranslatedDocumentUploaded;

    public static final Predicate<CaseData> partAdmitPayImmediately = CaseData::isPartAdmitPayImmediatelyAccepted;

    public static final Predicate<CaseData> ccjRequestJudgmentByAdmission =
        CaseData::isCcjRequestJudgmentByAdmission;

    public static final Predicate<CaseData> isRespondentSignSettlementAgreement =
        CaseData::isRespondentRespondedToSettlementAgreement;

    public static final Predicate<CaseData> isClaimantNotSettleFullDefenceClaim =
            CaseData::isClaimantIntentionNotSettlePartAdmit;

    public static final Predicate<CaseData> isClaimantSettleTheClaim =
            CaseData::isClaimantIntentionSettlePartAdmit;
}
