package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed.LipPredicate;

import java.util.function.Predicate;

public class FlowLipPredicate {

    private FlowLipPredicate() {
        //Utility classes require a private constructor for checkstyle
    }

    public static final Predicate<CaseData> isLipCase = LipPredicate.isLiPvLiPCase;
    public static final Predicate<CaseData> agreedToMediation = LipPredicate.agreedToMediation;
    public static final Predicate<CaseData> isTranslatedDocumentUploaded = LipPredicate.isTranslatedDocumentUploaded;
    public static final Predicate<CaseData> ccjRequestJudgmentByAdmission = LipPredicate.ccjRequestJudgmentByAdmission;
    public static final Predicate<CaseData> isRespondentSignSettlementAgreement = LipPredicate.isRespondentSignSettlementAgreement;
    public static final Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline = LipPredicate.nocSubmittedForLiPDefendantBeforeOffline;
    public static final Predicate<CaseData> nocSubmittedForLiPDefendant = LipPredicate.nocSubmittedForLiPDefendant;
}
