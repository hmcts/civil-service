package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.service.flowstate.predicate.util.PredicateUtil.nullSafe;

@SuppressWarnings("java:S1214")
public non-sealed interface MediationPredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Mediation",
        summary = "Claimant agreed to mediation",
        description = "Claimant has opted into free mediation"
    )
    Predicate<CaseData> agreedToMediation =
        CaseDataPredicate.Claimant.agreedToMediation;

    @BusinessRule(
        group = "Mediation",
        summary = "Claimant declined mediation",
        description = "Claimant has opted out of free mediation"
    )
    Predicate<CaseData> declinedMediation =
        CaseDataPredicate.Claimant.declinedMediation;

    @BusinessRule(
        group = "Mediation",
        summary = "Case is Carm enabled",
        description = "Case has applicant or respondent mediation contact information"
    )
    Predicate<CaseData> isCarmEnabledForCase =
        CaseDataPredicate.Mediation.hasContactInfoApplicant1
        .or(CaseDataPredicate.Mediation.hasContactInfoRespondent1)
        .or(CaseDataPredicate.Mediation.hasContactInfoRespondent2);

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
        summary = "Case is Carm mediation",
        description = "Case is Carm applicable"
    )
    Predicate<CaseData> isCarmMediation =
        CaseDataPredicate.Claimant.isNotSettlePartAdmit
            .and(CaseDataPredicate.Claimant.agreedToMediation.negate())
            .and(
                isCarmApplicableCase.or(isCarmApplicableCaseLiP)
            )
            .and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate());

    @BusinessRule(
        group = "Mediation",
        summary = "All LR parties agreed to mediation (Spec small claim)",
        description = "In a SPEC small claim, all represented parties have agreed to legal representative mediation"
    )
    Predicate<CaseData> allAgreedToLrMediationSpec =
        CaseDataPredicate.Claim.isSpecClaim
            .and(CaseDataPredicate.Claim.isSmallClaim)
            .and(CaseDataPredicate.Mediation.isRequiredRespondent1Spec)
            .and(
                CaseDataPredicate.Respondent.hasRespondent2
                    .and(CaseDataPredicate.Respondent.isNotSameLegalRepresentative)
                    .and(CaseDataPredicate.Mediation.isNotRequiredRespondent2Spec)
                    .negate()
            )
            .and(CaseDataPredicate.Mediation.isNotAgreedFreeMediationApplicant1Spec.negate())
            .and(CaseDataPredicate.Mediation.isNotRequiredApplicantMPSpec.negate())
            .and(CaseDataPredicate.Claimant.agreedToMediation.negate());

    @BusinessRule(
        group = "Mediation",
        summary = "todo",
        description = "todo"
    )
    Predicate<CaseData> beforeUnsuccessful =
        Mediation.hasReasonUnsuccessful.negate()
            .and(Mediation.hasReasonUnsuccessfulMultiSelect.negate());

    @BusinessRule(
        group = "Mediation",
        summary = "todo",
        description = "todo"
    )
    Predicate<CaseData> unsuccessful =
        Mediation.hasReasonUnsuccessful
            .or(
                Mediation.hasReasonUnsuccessfulMultiSelect
                    .and(Mediation.hasReasonUnsuccessfulMultiSelectValue)
            );

}
