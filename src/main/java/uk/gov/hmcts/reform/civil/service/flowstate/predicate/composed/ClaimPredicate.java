package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public interface ClaimPredicate {

    @BusinessRule(
        group = "Claim",
        summary = "Spec claim",
        description = "Case is a SPEC (damages) claim as per case access category"
    )
    Predicate<CaseData> isSpec = CaseDataPredicate.Claim.isSpecClaim;

    @BusinessRule(
        group = "Claim",
        summary = "Claim submitted",
        description = "Claim has a submitted date (claim has been submitted)"
    )
    Predicate<CaseData> submitted = CaseDataPredicate.Claim.hasSubmittedDate;

    @BusinessRule(
        group = "Claim",
        summary = "Case change-of-representation",
        description = "Change-of-representation recorded"
    )
    Predicate<CaseData> changeOfRepresentation = CaseDataPredicate.Claim.hasChangeOfRepresentation;

    @BusinessRule(
        group = "Claim",
        summary = "Claim submitted one legal representative",
        description = "Claim has a submitted date and one legal representative"
    )
    Predicate<CaseData> submittedOneRespondentRepresentative =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent1.negate())
            .and(
                CaseDataPredicate.Respondent.hasAddRespondent2.negate().or(CaseDataPredicate.Respondent.isNotAddRespondent2)
                    .or(CaseDataPredicate.Respondent.isAddRespondent2
                        .and(CaseDataPredicate.Respondent.isSameLegalRepresentative)
                    )
            );

    @BusinessRule(
        group = "Claim",
        summary = "Claim submitted two registered representatives",
        description = "Claim has a submitted date and two registered representatives"
    )
    Predicate<CaseData> submittedTwoRegisteredRespondentRepresentatives =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isAddRespondent2)
            .and(CaseDataPredicate.Respondent.isNotSameLegalRepresentative)
            .and(CaseDataPredicate.Respondent.isRepresentedRespondent1)
            .and(CaseDataPredicate.Respondent.isRepresentedRespondent2)
            .and(CaseDataPredicate.Respondent.isOrgRegisteredRespondent1)
            .and(CaseDataPredicate.Respondent.isOrgRegisteredRespondent2);

    @BusinessRule(
        group = "Claim",
        summary = "Claim submitted two representatives one unregistered",
        description = "Claim has a submitted date and two representatives one unregistered"
    )
    Predicate<CaseData> submittedTwoRespondentRepresentativesOneUnregistered =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isAddRespondent2)
            .and(CaseDataPredicate.Respondent.isNotSameLegalRepresentative)
            .and(CaseDataPredicate.Respondent.isRepresentedRespondent1)
            .and(CaseDataPredicate.Respondent.isRepresentedRespondent2)
            .and(
                (CaseDataPredicate.Respondent.isOrgRegisteredRespondent1
                    .and(CaseDataPredicate.Respondent.isNotOrgRegisteredRespondent2))
                    .or(CaseDataPredicate.Respondent.isOrgRegisteredRespondent2
                        .and(CaseDataPredicate.Respondent.isNotOrgRegisteredRespondent1)
                    )
            );

    @BusinessRule(
        group = "Claim",
        summary = "Claim submitted two representatives both unregistered",
        description = "Claim has a submitted date and two representatives both unregistered"
    )
    Predicate<CaseData> submittedBothUnregisteredSolicitors =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isNotOrgRegisteredRespondent1)
            .and(CaseDataPredicate.Respondent.isAddRespondent2)
            .and(CaseDataPredicate.Respondent.isNotOrgRegisteredRespondent2)
            .and(CaseDataPredicate.Respondent.isNotSameLegalRepresentative
                 .or(CaseDataPredicate.Respondent.hasSameLegalRepresentative.negate())
            );

    @BusinessRule(
        group = "Claim",
        summary = "Claim submitted one representative unregistered",
        description = "Claim has a submitted date and one representative unregistered"
    )
    Predicate<CaseData> submitted1v1RespondentOneUnregistered =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isNotAddRespondent2)
            .and(CaseDataPredicate.Respondent.isRepresentedRespondent1)
            .and(CaseDataPredicate.Respondent.isNotOrgRegisteredRespondent1);

    @BusinessRule(
        group = "Claim",
        summary = "Claim submitted one unrepresented defendant only",
        description = "Claim has a submitted date and unrepresented defendant only"
    )
    Predicate<CaseData> submittedOneUnrepresentedDefendantOnly =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent1)
            .and(CaseDataPredicate.Respondent.isAddRespondent2.negate());

    @BusinessRule(
        group = "Claim",
        summary = "Claim submitted respondent1 unrepresented",
        description = "Claim has a submitted date and respondent unrepresented"
    )
    Predicate<CaseData> submittedRespondent1Unrepresented =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent1);

    @BusinessRule(
        group = "Claim",
        summary = "Claim submitted respondent2 unrepresented",
        description = "Claim has a submitted date and respondent unrepresented"
    )
    Predicate<CaseData> submittedRespondent2Unrepresented =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isAddRespondent2)
            .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent2);

    @BusinessRule(
        group = "Claim",
        summary = "Claim issued",
        description = "Claim has a claim notification deadline (claim has been issued/notified)"
    )
    Predicate<CaseData> issued = CaseDataPredicate.Claim.hasNotificationDeadline;

    @BusinessRule(
        group = "Claim",
        summary = "Claim pending issued",
        description = ""
    )
    Predicate<CaseData> pendingIssued =
        CaseDataPredicate.Claim.hasIssueDate
        .and(CaseDataPredicate.Respondent.isRepresentedRespondent1)
        .and(CaseDataPredicate.Respondent.isOrgRegisteredRespondent1)
        .and(
            CaseDataPredicate.Respondent.hasRespondent2.negate()
             .or(
                 CaseDataPredicate.Respondent.isRepresentedRespondent2
                .and(
                    CaseDataPredicate.Respondent.isOrgRegisteredRespondent2
                    .or(CaseDataPredicate.Respondent.isSameLegalRepresentative)
                )
            )
        );

    @BusinessRule(
        group = "Claim",
        summary = "Claim pending issued unrepresented defendant",
        description = "The claim has been issued and is pending service to an unrepresented defendant. This applies to all non-SPEC claims with an unrepresented defendant, and to multi-party SPEC claims that also have an unrepresented defendant."
    )
   Predicate<CaseData> pendingIssuedUnrepresented =
        CaseDataPredicate.Claim.hasIssueDate
            .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent1)
            .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent2)
            .or(CaseDataPredicate.Respondent.isUnrepresentedRespondent1
                .and(CaseDataPredicate.Respondent.isRepresentedNotOrgRegisteredRespondent2.negate())
            )
            .or(CaseDataPredicate.Respondent.isRepresentedNotOrgRegisteredRespondent1.negate()
                .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent2))
            .and(CaseDataPredicate.Claim.isSpecClaim.negate())
            .or(CaseDataPredicate.Claim.isMultiParty
                .and(
                    CaseDataPredicate.Respondent.isUnrepresentedRespondent1
                        .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent2)
                        .or(CaseDataPredicate.Respondent.isUnrepresentedRespondent1
                            .and(CaseDataPredicate.Respondent.isRepresentedNotOrgRegisteredRespondent2.negate()))
                        .or(CaseDataPredicate.Respondent.isRepresentedNotOrgRegisteredRespondent1.negate()
                            .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent2)))
                .and(CaseDataPredicate.Claim.isSpecClaim)
            );

    @BusinessRule(
        group = "Claim",
        summary = "Claim after issued",
        description = "The claim has been issued and is pending acknowledgement or response from the defendant. This state is active before the notification deadline. For a specified claim, a notification date must have been set, whereas for an unspecified claim, it should not."
    )
    Predicate<CaseData> afterIssued =
        CaseDataPredicate.ClaimDetails.hasNotificationDate.negate()
            .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.Claim.hasFutureNotificationDeadline)
            .and(CaseDataPredicate.Claim.isSpecClaim.and(CaseDataPredicate.Claim.hasNotificationDate)
                 .or(CaseDataPredicate.Claim.isSpecClaim.negate()
                     .and(CaseDataPredicate.Claim.hasNotificationDate.negate()))
            );

}
