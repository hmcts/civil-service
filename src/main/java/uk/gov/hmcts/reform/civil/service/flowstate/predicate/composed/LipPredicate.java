package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public interface LipPredicate {

    @BusinessRule(
        group = "Lip",
        summary = "Case contains Lip v Lip participant",
        description = "Both parties on the case are litigant-in-person (LiP)")
    Predicate<CaseData> isLiPvLiPCase = CaseDataPredicate.Lip.isLiPvLipCase;

    @BusinessRule(
        group = "Lip",
        summary = "Case contains Lip v LR participant",
        description = "At least one party on the case is a litigant-in-person (LiP)")
    Predicate<CaseData> isLiPvLRCase = CaseDataPredicate.Lip.isLiPvLRCase;

    @BusinessRule(
        group = "Lip",
        summary = "Case Lip is Help With Fee",
        description = "A litigant-in-person with Help With Fee")
    Predicate<CaseData> isHelpWithFees = CaseDataPredicate.Lip.isHelpWithFees;

    @BusinessRule(
        group = "Lip",
        summary = "Claimant agreed to mediation",
        description = "Claimant has opted into free mediation")
    Predicate<CaseData> agreedToMediation = CaseDataPredicate.Claimant.agreedToMediation;

    @BusinessRule(
        group = "Lip",
        summary = "Translated response document uploaded",
        description = "A translated response document has been uploaded")
    Predicate<CaseData> isTranslatedDocumentUploaded =
        CaseDataPredicate.Lip.translatedDocumentUploaded;

    @BusinessRule(
        group = "Lip",
        summary = "CCJ requested by admission",
        description = "Applicant has requested a CCJ by admission")
    Predicate<CaseData> ccjRequestJudgmentByAdmission =
        CaseDataPredicate.Lip.ccjRequestByAdmissionFlag;

    @BusinessRule(
        group = "Lip",
        summary = "Respondent signed settlement agreement",
        description = "Respondent has signed the digital settlement agreement")
    Predicate<CaseData> isRespondentSignSettlementAgreement =
        CaseDataPredicate.Lip.respondentSignedSettlementAgreement;

    @BusinessRule(
        group = "Lip",
        summary = "NOC submitted for Lip claimant",
        description = "A Notice of Change for a Lip claimant was submitted")
    Predicate<CaseData> nocApplyForLiPClaimant =
        CaseDataPredicate.Lip.nocApplyForLiPClaimant;

    @BusinessRule(
        group = "Lip",
        summary = "NOC submitted for Lip defendant before offline",
        description = "A Notice of Change for a Lip defendant was submitted prior to the case being taken offline")
    Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline =
        CaseDataPredicate.Lip.nocSubmittedForLiPDefendantBeforeOffline;

    @BusinessRule(
        group = "Lip",
        summary = "NOC submitted for Lip defendant",
        description = "A Notice of Change for a Lip defendant was submitted")
    Predicate<CaseData> nocSubmittedForLiPDefendant =
        CaseDataPredicate.Lip.nocSubmittedForLiPDefendant;

    @BusinessRule(
        group = "Lip",
        summary = "Case contains Lip",
        description = "At least one party is a Lip (applicant or respondent)")
    Predicate<CaseData> caseContainsLiP = CaseDataPredicate.Lip.caseContainsLiP;

    @BusinessRule(
        group = "Lip",
        summary = "Pin in post enabled",
        description = "Pin in post enabled")
    Predicate<CaseData> pinInPostEnabled = CaseDataPredicate.Lip.hasPinInPost;

    @BusinessRule(
        group = "Lip",
        summary = "Full defence proceed (SPEC)",
        description = "Spec claim and applicant 1 has not settled claim")
    Predicate<CaseData> fullDefenceProceed =
        CaseDataPredicate.Claim.isSpecClaim
            .and(CaseDataPredicate.Lip.isNotSettleClaimApplicant1);

}
