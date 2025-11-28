package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public interface FlowLipPredicate {
    Predicate<CaseData> isLipCase = LipPredicate.isLiPvLiPCase;
    Predicate<CaseData> agreedToMediation = LipPredicate.agreedToMediation;
    Predicate<CaseData> isTranslatedDocumentUploaded = LipPredicate.isTranslatedDocumentUploaded;
    Predicate<CaseData> ccjRequestJudgmentByAdmission = LipPredicate.ccjRequestJudgmentByAdmission;
    Predicate<CaseData> isRespondentSignSettlementAgreement = LipPredicate.isRespondentSignSettlementAgreement;
    Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline = LipPredicate.nocSubmittedForLiPDefendantBeforeOffline;
    Predicate<CaseData> nocSubmittedForLiPDefendant = LipPredicate.nocSubmittedForLiPDefendant;
}
