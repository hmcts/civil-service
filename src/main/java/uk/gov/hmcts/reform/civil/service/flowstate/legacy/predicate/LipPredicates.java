package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

public final class LipPredicates {

    private LipPredicates() {
        // Utility class
    }

    public static final Predicate<CaseData> isLipCase = CaseData::isLipvLipOneVOne;

    public static final Predicate<CaseData> agreedToMediation = CaseData::hasClaimantAgreedToFreeMediation;

    public static final Predicate<CaseData> isTranslatedDocumentUploaded = CaseData::isTranslatedDocumentUploaded;

    public static final Predicate<CaseData> ccjRequestJudgmentByAdmission =
        CaseData::isCcjRequestJudgmentByAdmission;

    public static final Predicate<CaseData> isRespondentSignSettlementAgreement =
        CaseData::isRespondentRespondedToSettlementAgreement;

    public static final Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline = CaseData::nocApplyForLiPDefendantBeforeOffline;

    public static final Predicate<CaseData> nocSubmittedForLiPDefendant = CaseData::nocApplyForLiPDefendant;

    public static final Predicate<CaseData> caseContainsLiP = caseData ->
        caseData.isRespondent1LiP()
            || caseData.isRespondent2LiP()
            || caseData.isApplicantNotRepresented();
}
