package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public non-sealed interface MediationPredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Mediation",
        summary = "Claimant agreed to free mediation",
        description = "Claimant has opted into free mediation (delegates to Claimant.agreedToMediation)."
    )
    Predicate<CaseData> agreedToMediation =
        CaseDataPredicate.Claimant.agreedToMediation;

    @BusinessRule(
        group = "Mediation",
        summary = "Claimant declined free mediation",
        description = "Claimant has opted out of free mediation (delegates to Claimant.declinedMediation)."
    )
    Predicate<CaseData> declinedMediation =
        CaseDataPredicate.Claimant.declinedMediation;

    @BusinessRule(
        group = "Mediation",
        summary = "Case is CARM-enabled (LR)",
        description = "Mediation contact information is present for any party: applicant 1, respondent 1, or respondent 2."
    )
    Predicate<CaseData> isCarmEnabledForCase =
        CaseDataPredicate.Mediation.hasContactInfoApplicant1
        .or(CaseDataPredicate.Mediation.hasContactInfoRespondent1)
        .or(CaseDataPredicate.Mediation.hasContactInfoRespondent2);

    @BusinessRule(
        group = "Mediation",
        summary = "Case is CARM-enabled (LiP)",
        description = "A LiP CARM response is present for applicant 1 or respondent 1."
    )
    Predicate<CaseData> isCarmEnabledForCaseLiP =
        CaseDataPredicate.Mediation.hasResponseCarmLiPApplicant1
        .or(CaseDataPredicate.Mediation.hasResponseCarmLiPRespondent1);

    @BusinessRule(
        group = "Mediation",
        summary = "CARM applicable (LR • SPEC small claims)",
        description = "Case is SPEC and small claims track; CARM is enabled via mediation contact info; respondent 1 is represented; applicant 1 is not marked as unrepresented."
    )
    Predicate<CaseData> isCarmApplicableCase =
        isCarmEnabledForCase
            .and(CaseDataPredicate.Claim.isSpecClaim)
            .and(CaseDataPredicate.Claim.isSmallClaim)
            .and(CaseDataPredicate.Respondent.isRepresentedRespondent1)
            .and(CaseDataPredicate.Applicant.isUnrepresentedApplicant1.negate());

    @BusinessRule(
        group = "Mediation",
        summary = "CARM applicable (LiP • 1v1 • SPEC small claims)",
        description = "Case is SPEC and small claims track; CARM is LiP-enabled (applicant 1 or respondent 1 LiP CARM response present); there is no respondent 2; at least one of applicant 1 or respondent 1 is unrepresented."
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
        summary = "Route to CARM mediation",
        description = "The claimant indicates they will not settle (part admission); the claimant has not agreed to free mediation; the case is CARM applicable (LR or LiP criteria met); and the case has not been taken offline by staff."
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
        summary = "All LR parties agreed to mediation (SPEC small claims)",
        description = "SPEC small claims when: respondent 1 has agreed to free mediation (SPEC); if respondent 2 is present with a different legal rep, they have not declined (SPEC); applicant 1 has agreed to free mediation (SPEC); any MP applicant (if present) has agreed (SPEC); and the general claimant 'agreed to mediation' flag is not set."
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
        summary = "Before unsuccessful mediation recorded",
        description = "No unsuccessful mediation reason has been recorded: neither the single reason nor the multi-select list is present."
    )
    Predicate<CaseData> beforeUnsuccessful =
        Mediation.hasReasonUnsuccessful.negate()
            .and(Mediation.hasReasonUnsuccessfulMultiSelect.negate());

    @BusinessRule(
        group = "Mediation",
        summary = "Unsuccessful mediation recorded",
        description = "An unsuccessful mediation reason is recorded: either the single reason is present, or the multi-select list exists and contains at least one value."
    )
    Predicate<CaseData> unsuccessful =
        Mediation.hasReasonUnsuccessful
            .or(
                Mediation.hasReasonUnsuccessfulMultiSelect
                    .and(Mediation.hasReasonUnsuccessfulMultiSelectValue)
            );

}
