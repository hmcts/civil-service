package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

public class FlowLipPredicate {

    private FlowLipPredicate() {

    }

    public static final Predicate<CaseData> isLipCase = CaseData::isLipvLipOneVOne;

    //TODO Remove after DTSCCI-244
    public static final Predicate<CaseData> nocSubmittedForLiPApplicant = CaseData::nocApplyForLiPClaimant;

    //TODO Remove after DTSCCI-244
    public static final Predicate<CaseData> isLiPvLRCase = CaseData::isLipvLROneVOne;

    public static final Predicate<CaseData> agreedToMediation = CaseData::hasClaimantAgreedToFreeMediation;

    //TODO Remove after DTSCCI-244
    public static final Predicate<CaseData> declinedMediation = CaseData::hasClaimantNotAgreedToFreeMediation;

    public static final Predicate<CaseData> isTranslatedDocumentUploaded = CaseData::isTranslatedDocumentUploaded;

    //TODO Remove after DTSCCI-244
    public static final Predicate<CaseData> partAdmitPayImmediately = CaseData::isPartAdmitPayImmediatelyAccepted;

    public static final Predicate<CaseData> ccjRequestJudgmentByAdmission =
        CaseData::isCcjRequestJudgmentByAdmission;

    public static final Predicate<CaseData> isRespondentSignSettlementAgreement =
        CaseData::isRespondentRespondedToSettlementAgreement;

    //TODO Remove after DTSCCI-244
    public static final Predicate<CaseData> isClaimantNotSettleFullDefenceClaim =
        CaseData::isClaimantIntentionNotSettlePartAdmit;

    //TODO Remove after DTSCCI-244
    public static final Predicate<CaseData> isClaimantSettleTheClaim =
        CaseData::isClaimantIntentionSettlePartAdmit;

    //TODO Remove after DTSCCI-244
    public static final Predicate<CaseData> isDefendantNotPaidFullDefenceClaim =
        CaseData::isFullDefenceNotPaid;

    public static final Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline = CaseData::nocApplyForLiPDefendantBeforeOffline;

    public static final Predicate<CaseData> nocSubmittedForLiPDefendant = CaseData::nocApplyForLiPDefendant;

    public  static final Predicate<CaseData> isContainsLip = CaseData::isLipCase;

}
