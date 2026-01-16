package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public non-sealed interface LipPredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Lip",
        summary = "Case is LiP v LiP",
        description = "Both applicant and respondent are unrepresented (litigants in person)"
    )
    Predicate<CaseData> isLiPvLiPCase = CaseDataPredicate.Lip.isLiPvLipCase;

    @BusinessRule(
        group = "Lip",
        summary = "Case is LiP v represented",
        description = "One party is a litigant-in-person (LiP) and the other is legally represented"
    )
    Predicate<CaseData> isLiPvLRCase = CaseDataPredicate.Lip.isLiPvLRCase;

    @BusinessRule(
        group = "Lip",
        summary = "Help With Fees (LiP)",
        description = "A litigant-in-person has an active Help With Fees application"
    )
    Predicate<CaseData> isHelpWithFees = CaseDataPredicate.Lip.isHelpWithFees;

    @BusinessRule(
        group = "Lip",
        summary = "Translated response document uploaded",
        description = "A translated response document has been uploaded"
    )
    Predicate<CaseData> isTranslatedDocumentUploaded =
        CaseDataPredicate.Lip.translatedDocumentUploaded;

    @BusinessRule(
        group = "Lip",
        summary = "CCJ requested by admission",
        description = "Applicant has requested a County Court Judgment (CCJ) by admission"
    )
    Predicate<CaseData> ccjRequestJudgmentByAdmission =
        CaseDataPredicate.Lip.ccjRequestByAdmissionFlag;

    @BusinessRule(
        group = "Lip",
        summary = "Respondent signed settlement agreement",
        description = "Respondent has signed the digital settlement agreement"
    )
    Predicate<CaseData> isRespondentSignSettlementAgreement =
        CaseDataPredicate.Lip.respondentSignedSettlementAgreement;

    @BusinessRule(
        group = "Lip",
        summary = "Notice of Change submitted for LiP claimant",
        description = "A Notice of Change was submitted for an unrepresented claimant (LiP)"
    )
    Predicate<CaseData> nocApplyForLiPClaimant =
        CaseDataPredicate.Lip.nocApplyForLiPClaimant;

    @BusinessRule(
        group = "Lip",
        summary = "Notice of Change submitted for LiP defendant (pre-offline)",
        description = "A Notice of Change was submitted for an unrepresented defendant (LiP) before the case was taken offline"
    )
    Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline =
        CaseDataPredicate.Lip.nocSubmittedForLiPDefendantBeforeOffline;

    @BusinessRule(
        group = "Lip",
        summary = "Notice of Change submitted for LiP defendant",
        description = "A Notice of Change was submitted for an unrepresented defendant (LiP)"
    )
    Predicate<CaseData> nocSubmittedForLiPDefendant =
        CaseDataPredicate.Lip.nocSubmittedForLiPDefendant;

    @BusinessRule(
        group = "Lip",
        summary = "Case contains a LiP party",
        description = "At least one party (applicant or respondent) is a litigant-in-person"
    )
    Predicate<CaseData> caseContainsLiP = CaseDataPredicate.Lip.caseContainsLiP;

    @BusinessRule(
        group = "Lip",
        summary = "Certificate of service enabled",
        description = "True when at least one litigant-in-person (LiP) defendant is flagged as 'at claim issued' (either " +
            "`defendant1LIPAtClaimIssued` or `defendant2LIPAtClaimIssued` = Yes)."
    )
    Predicate<CaseData> certificateOfServiceEnabled = CaseDataPredicate.Lip.isClaimIssued;

    @BusinessRule(
        group = "Lip",
        summary = "PIN-in-post enabled",
        description = "PIN-in-post service is enabled for this case"
    )
    Predicate<CaseData> pinInPostEnabled = CaseDataPredicate.Lip.hasPinInPost;

    @BusinessRule(
        group = "Lip",
        summary = "Full defence proceed (SPEC)",
        description = "SPEC claim and applicant 1 has not settled the claim"
    )
    Predicate<CaseData> fullDefenceProceed =
        CaseDataPredicate.Claim.isSpecClaim
            .and(CaseDataPredicate.Lip.isNotSettleClaimApplicant1);

}
