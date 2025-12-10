package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.service.flowstate.predicate.util.PredicateUtil.nullSafe;

@SuppressWarnings("java:S1214")
public non-sealed interface ClaimPredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Claim",
        summary = "SPEC claim",
        description = "Case is in the SPEC (damages) service based on case access category"
    )
    Predicate<CaseData> isSpec = CaseDataPredicate.Claim.isSpecClaim;

    @BusinessRule(
        group = "Claim",
        summary = "UNSPEC claim",
        description = "Case is in the UNSPEC service based on case access category"
    )
    Predicate<CaseData> isUnspec = CaseDataPredicate.Claim.isUnspecClaim;

    @BusinessRule(
        group = "Claim",
        summary = "Small claim",
        description = "Case is Small Claim track"
    )
    Predicate<CaseData> isSmall = CaseDataPredicate.Claim.isSmallClaim;

    @BusinessRule(
        group = "Claim",
        summary = "Fast claim",
        description = "Case is Fast Claim track"
    )
    Predicate<CaseData> isFast = CaseDataPredicate.Claim.isFastClaim;

    @BusinessRule(
        group = "Claim",
        summary = "Multi claim",
        description = "Case is Multi Claim track"
    )
    Predicate<CaseData> isMulti = CaseDataPredicate.Claim.isMultiClaim;

    @BusinessRule(
        group = "Claim",
        summary = "Claim is full defence not paid",
        description = "Claim is defendant not paid full defence"
    )
    Predicate<CaseData> isFullDefenceNotPaid =
        nullSafe(CaseDataPredicate.Claim.isFullDefenceNotPaid);

    @BusinessRule(
        group = "Claim",
        summary = "One V One claim",
        description = "Case is One V One based on case party scenario"
    )
    Predicate<CaseData> isOneVOne = CaseDataPredicate.MultiParty.isOneVOne;

    @BusinessRule(
        group = "Claim",
        summary = "Multi party claim",
        description = "Case is multi party based on two applicants or respondents"
    )
    Predicate<CaseData> isMultiParty = CaseDataPredicate.Claim.isMultiParty;

    @BusinessRule(
        group = "Claim",
        summary = "Claim notified",
        description = "Acknowledgement deadline exists - claim notification has been sent (State Flow: claim notified)"
    )
    Predicate<CaseData> issued = CaseDataPredicate.Claim.hasNotificationDeadline;

    @BusinessRule(
        group = "Claim",
        summary = "Claim submitted",
        description = "Claim has a submitted date (claim has been submitted)"
    )
    Predicate<CaseData> submitted = CaseDataPredicate.Claim.hasSubmittedDate;

    @BusinessRule(
        group = "Claim",
        summary = "Case change of representation",
        description = "A change of legal representation has been recorded on the case"
    )
    Predicate<CaseData> changeOfRepresentation = CaseDataPredicate.Claim.hasChangeOfRepresentation;

    @BusinessRule(
        group = "Claim",
        summary = "Respondent have same legal representative",
        description = "Same legal representation for both defendants"
    )
    Predicate<CaseData> sameRepresentationBoth = CaseDataPredicate.Respondent.isSameLegalRepresentative;

    @BusinessRule(
        group = "Claim",
        summary = "todo",
        description = "Issue date is set and respondent 1 is unrepresented"
    )
    Predicate<CaseData> issuedRespondent1Unrepresented =
        CaseDataPredicate.Claim.hasIssueDate.and(CaseDataPredicate.Respondent.isUnrepresentedRespondent1);

    @BusinessRule(
        group = "Claim",
        summary = "todo",
        description = "Issue date is set and respondent 2 is unrepresented"
    )
    Predicate<CaseData> issuedRespondent2Unrepresented =
        CaseDataPredicate.Claim.hasIssueDate.and(CaseDataPredicate.Respondent.isUnrepresentedRespondent2);

    @BusinessRule(
        group = "Claim",
        summary = "Issued - respondent 1 is represented with unregistered org",
        description = "Issue date is set where defendant is represented but their organisation is not registered"
    )
    Predicate<CaseData> issuedRespondent1OrgNotRegistered =
        CaseDataPredicate.Claim.hasIssueDate
            .and(CaseDataPredicate.Respondent.isRepresentedRespondent1)
            .and(CaseDataPredicate.Respondent.isNotOrgRegisteredRespondent1);

    @BusinessRule(
        group = "Claim",
        summary = "Issued - respondent 2 is represented with unregistered org",
        description = "Issue date is set where defendant is represented but their organisation is not registered"
    )
    Predicate<CaseData> issuedRespondent2OrgNotRegistered =
        CaseDataPredicate.Claim.hasIssueDate
            .and(CaseDataPredicate.Respondent.isRepresentedRespondent2)
            .and(CaseDataPredicate.Respondent.isNotOrgRegisteredRespondent2);

    @BusinessRule(
        group = "Claim",
        summary = "Submitted - one respondent representative",
        description = "Submitted claim where respondent 1 is represented and either there is a single defendant, or there are two defendants sharing the same legal representative"
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
        summary = "Submitted - two respondent representatives (both orgs registered)",
        description = "Submitted claim with two defendants, each represented by different solicitors and both defendant organisations are registered"
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
        summary = "Submitted - two respondent representatives (one org unregistered)",
        description = "Submitted claim with two defendants, each represented by different solicitors where exactly one defendant organisation is not registered"
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
        summary = "Submitted - two respondent representatives (both orgs unregistered)",
        description = "Submitted claim with two defendants, each represented by different solicitors and both defendant organisations are not registered"
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
        summary = "Submitted - 1v1 represented defendant with unregistered org",
        description = "Submitted 1v1 claim where defendant is represented but their organisation is not registered"
    )
    Predicate<CaseData> submitted1v1RespondentOneUnregistered =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isNotAddRespondent2)
            .and(CaseDataPredicate.Respondent.isRepresentedRespondent1)
            .and(CaseDataPredicate.Respondent.isNotOrgRegisteredRespondent1);

    @BusinessRule(
        group = "Claim",
        summary = "Submitted - unrepresented defendant only",
        description = "Submitted claim with a single unrepresented defendant (no second defendant)"
    )
    Predicate<CaseData> submittedOneUnrepresentedDefendantOnly =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent1)
            .and(CaseDataPredicate.Respondent.isAddRespondent2.negate());

    @BusinessRule(
        group = "Claim",
        summary = "Submitted - respondent 1 unrepresented",
        description = "Submitted claim where respondent 1 is unrepresented"
    )
    Predicate<CaseData> submittedRespondent1Unrepresented =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent1);

    @BusinessRule(
        group = "Claim",
        summary = "Submitted - respondent 2 unrepresented",
        description = "Submitted claim where respondent 2 is unrepresented"
    )
    Predicate<CaseData> submittedRespondent2Unrepresented =
        CaseDataPredicate.Claim.hasSubmittedDate
            .and(CaseDataPredicate.Respondent.isAddRespondent2)
            .and(CaseDataPredicate.Respondent.isUnrepresentedRespondent2);

    @BusinessRule(
        group = "Claim",
        summary = "Pending claim issue (registered defendants)",
        description = "Issue date is set and all represented defendants have registered organisations " +
            "(second defendant absent or registered/same solicitor). Used for moving to pending issue"
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
        summary = "Pending claim issue - unrepresented defendant(s)",
        description = "Issue date is set and at least one defendant is unrepresented. Applies to all UNSPEC, and to SPEC only for multi‑party scenarios"
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
        summary = "After issue - no AoS/response, before claim details",
        description = "No claim‑details notification yet; respondent has not acknowledged service or responded; " +
            "claim notification deadline is in the future. For SPEC, a notification date exists; for UNSPEC, it does not"
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
