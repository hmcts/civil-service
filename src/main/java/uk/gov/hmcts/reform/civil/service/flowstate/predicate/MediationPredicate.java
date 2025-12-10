package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public non-sealed interface MediationPredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Mediation",
        summary = "Claimant agreed to mediation",
        description = "Claimant has opted into free mediation"
    )
    Predicate<CaseData> agreedToMediation = CaseDataPredicate.Claimant.agreedToMediation;

    @BusinessRule(
        group = "Mediation",
        summary = "Claimant declined mediation",
        description = "Claimant has opted out of free mediation"
    )
    Predicate<CaseData> declinedMediation = CaseDataPredicate.Claimant.declinedMediation;

    @BusinessRule(
        group = "Mediation",
        summary = "Applicant 1 has not agreed to free mediation",
        description = "Applicant 1 has not agreed to free mediation"
    )
    Predicate<CaseData> isNotAgreedFreeMediationSpec =
        CaseDataPredicate.Mediation.isNotAgreedFreeMediationApplicant1Spec;

    @BusinessRule(
        group = "Mediation",
        summary = "Response claim mediation required (Spec)",
        description = "Response claim mediation required (Spec)"
    )
    Predicate<CaseData> isResponseMediationRequiredSpec =
        CaseDataPredicate.Mediation.isMediationRequiredRespondent1Spec;

    @BusinessRule(
        group = "Mediation",
        summary = "Respondent 2 claim mediation not required (Spec)",
        description = "Response claim mediation not required (Spec)"
    )
    Predicate<CaseData> isMediationNotRequiredRespondent2Spec = CaseDataPredicate.Mediation.isMediationNotRequiredRespondent2Spec;

    @BusinessRule(
        group = "Mediation",
        summary = "Applicant MP is not claim mediation required (Spec)",
        description = "Applicant MP is not claim mediation required (Spec)"
    )
    Predicate<CaseData> isNotMediationRequiredApplicantMPSpec = CaseDataPredicate.Mediation.isNotMediationRequiredApplicantMPSpec;

    @BusinessRule(
        group = "Mediation",
        summary = "Case is Carm enabled",
        description = "Case has applicant or respondent mediation contact information"
    )
    Predicate<CaseData> isCarmEnabledForCase =
        CaseDataPredicate.Mediation.hasMediationContactInfoApplicant1
        .or(CaseDataPredicate.Mediation.hasMediationContactInfoRespondent1)
        .or(CaseDataPredicate.Mediation.hasMediationContactInfoRespondent2);

    @BusinessRule(
        group = "Mediation",
        summary = "Case is Carm enabled (LiP)",
        description = "Case is LiP and has applicant or respondent response Carm"
    )
    Predicate<CaseData> isCarmEnabledForCaseLiP =
        CaseDataPredicate.Mediation.hasResponseCarmLiPApplicant1
        .or(CaseDataPredicate.Mediation.hasResponseCarmLiPRespondent1);

    @BusinessRule(
        group = "Mediation",
        summary = "Case is Carm applicable",
        description = "Case is LiP and has applicant or respondent response Carm"
    )
    Predicate<CaseData> isCarmApplicableCase =
        isCarmEnabledForCase
            .and(CaseDataPredicate.Claim.isSpecClaim)
            .and(CaseDataPredicate.Claim.isSmallClaim)
            .and(CaseDataPredicate.Respondent.isRepresentedRespondent1)
            .and(CaseDataPredicate.Applicant.isUnrepresentedApplicant1.negate());

    @BusinessRule(
        group = "Mediation",
        summary = "Case is Carm applicable (LiP)",
        description = "Case is LiP and has applicant or respondent response Carm"
    )
    Predicate<CaseData> isCarmApplicableCaseLiP =
        isCarmEnabledForCaseLiP
            .and(CaseDataPredicate.Claim.isSpecClaim)
            .and(CaseDataPredicate.Claim.isSmallClaim)
            .and(CaseDataPredicate.Respondent.hasRespondent2.negate())
            .and(
                CaseDataPredicate.Applicant.isUnrepresentedApplicant1
                    .or(CaseDataPredicate.Respondent.isUnrepresentedRespondent1)
            );

    @BusinessRule(
        group = "Mediation",
        summary = "All LR parties agreed to mediation (Spec small claim)",
        description = "In a SPEC small claim, all represented parties have agreed to legal representative mediation"
    )
    Predicate<CaseData> allAgreedToLrMediationSpec =
        CaseDataPredicate.Claim.isSpecClaim
            .and(CaseDataPredicate.Claim.isSmallClaim)
            .and(MediationPredicate.isResponseMediationRequiredSpec)
            .and(
                ResponsePredicate.hasRespondent2
                    .and(ResponsePredicate.isNotSameLegalRepresentative)
                    .and(MediationPredicate.isMediationNotRequiredRespondent2Spec)
                    .negate()
            )
            .and(MediationPredicate.isNotAgreedFreeMediationSpec.negate())
            .and(MediationPredicate.isNotMediationRequiredApplicantMPSpec.negate())
            .and(MediationPredicate.agreedToMediation.negate());
}
