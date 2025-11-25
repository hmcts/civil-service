package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

public final class LipPredicate {

    @BusinessRule(
        group = "LiP",
        summary = "Case contains LiP participant",
        description = "At least one party on the case is a litigant-in-person (LiP)")
    public static final Predicate<CaseData> isLipCase = CaseDataPredicate.Lip.isLiPCase;

    @BusinessRule(
        group = "LiP",
        summary = "Claimant agreed to mediation",
        description = "Claimant has opted into free mediation")
    public static final Predicate<CaseData> agreedToMediation = CaseDataPredicate.Claimant.agreedToMediation;
    @BusinessRule(
        group = "LiP",
        summary = "Translated response document uploaded",
        description = "A translated response document has been uploaded")
    public static final Predicate<CaseData> isTranslatedDocumentUploaded =
        CaseDataPredicate.Lip.translatedResponseDocumentUploaded;

    @BusinessRule(
        group = "LiP",
        summary = "CCJ requested by admission",
        description = "Applicant has requested a CCJ by admission")
    public static final Predicate<CaseData> ccjRequestJudgmentByAdmission =
        CaseDataPredicate.Lip.ccjRequestByAdmissionFlag;

    @BusinessRule(
        group = "LiP",
        summary = "Respondent signed settlement agreement",
        description = "Respondent has signed the digital settlement agreement")
    public static final Predicate<CaseData> isRespondentSignSettlementAgreement =
        CaseDataPredicate.Lip.respondentSignedSettlementAgreement;

    @BusinessRule(
        group = "LiP",
        summary = "NOC submitted for LiP defendant before offline",
        description = "A Notice of Change for a LiP defendant was submitted prior to the case being taken offline")
    public static final Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline =
        CaseDataPredicate.Lip.nocSubmittedForLiPDefendantBeforeOffline;

    @BusinessRule(
        group = "LiP",
        summary = "NOC submitted for LiP defendant",
        description = "A Notice of Change for a LiP defendant was submitted")
    public static final Predicate<CaseData> nocSubmittedForLiPDefendant =
        CaseDataPredicate.Lip.nocSubmittedForLiPDefendant;

    @BusinessRule(
        group = "LiP",
        summary = "Case contains LiP (helper)",
        description = "At least one party is a LiP (applicant or respondent)")
    public static final Predicate<CaseData> caseContainsLiP = CaseDataPredicate.Lip.caseContainsLiP;

    private LipPredicate() {
    }
}
