package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting.LISTING;
import static uk.gov.hmcts.reform.civil.service.flowstate.predicate.util.PredicateUtil.nullSafe;

/**
 * Atomic, domain-prefixed predicates operating on CaseData.
 *
 * <p>
 * Summary: Small, null-safe predicates used as building blocks for flow guards.
 * Description: This class exposes concise, discoverable predicates grouped by domain
 * (Applicant, Claim, ClaimDetails, Respondent, Payment, Hearing, Lip, etc.). Predicates
 * are intentionally atomic and should be composed in other higher-level compositions.
 *
 * <p>
 * Naming and BusinessRule conventions:
 * - Group names (inner classes) reflect domain areas (e.g. Applicant, Claim, Respondent).
 * - Predicate names are short, present-tense and start with a verb or 'has/is' where clear
 * (e.g. hasApplicantResponseDate, isApplicantResponseDeadlinePassed).
 * - `@BusinessRule` annotations should provide a one-line summary and a concise description
 * stating exactly what the predicate checks (fields, dates, enum values). Use catalogue
 * phrasing where authoritative (State Flow Transition Catalogue).
 */
@SuppressWarnings("java:S1214")
sealed interface CaseDataPredicate permits ClaimantPredicate, ClaimPredicate, DismissedPredicate, DivergencePredicate,
    HearingPredicate, LanguagePredicate, LipPredicate, MediationPredicate, NotificationPredicate, OutOfTimePredicate, PaymentPredicate,
    RepaymentPredicate, ResponsePredicate, TakenOfflinePredicate {

    interface Applicant {

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant is represented",
            description = "Applicant is legally represented (applicant not marked as self-represented)"
        )
        Predicate<CaseData> isRepresented =
            nullSafe(c -> !c.isApplicantNotRepresented());

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant 1 is unrepresented",
            description = "Returns true when applicant 1 is marked as not legally represented / self-represented (the applicant1Represented field is set to No)."
        )
        Predicate<CaseData> isUnrepresentedApplicant1 =
            nullSafe(c -> c.getApplicant1Represented() != null
                && NO.equals(c.getApplicant1Represented()));

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant 1 response date present",
            description = "Applicant 1 initial response date exists on the case"
        )
        Predicate<CaseData> hasResponseDateApplicant1 =
            nullSafe(c -> c.getApplicant1ResponseDate() != null);

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant 2 response date present",
            description = "Applicant 2 initial response date exists on the case"
        )
        Predicate<CaseData> hasResponseDateApplicant2 =
            nullSafe(c -> c.getApplicant2ResponseDate() != null);

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant response deadline passed",
            description = "Applicant response deadline exists and is before now (deadline expired)"
        )
        Predicate<CaseData> hasPassedResponseDeadline =
            nullSafe(c -> c.getApplicant1ResponseDeadline() != null
                && c.getApplicant1ResponseDeadline().isBefore(LocalDateTime.now()));

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant proceed decision present",
            description = "There is a recorded proceed / not-proceed decision for the applicant"
        )
        Predicate<CaseData> hasProceedDecision =
            nullSafe(c -> c.getApplicant1ProceedWithClaim() != null);

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant will proceed",
            description = "Applicant has indicated they will proceed with the claim (Yes)"
        )
        Predicate<CaseData> willProceed =
            nullSafe(c -> YES.equals(c.getApplicant1ProceedWithClaim()));

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant proceed decision present (SPEC 2v1)",
            description = "Proceed-decision field present for SPEC 2v1 scenario"
        )
        Predicate<CaseData> hasProceedDecisionSpec2v1 =
            nullSafe(c -> c.getApplicant1ProceedWithClaimSpec2v1() != null);

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant will proceed (SPEC 2v1)",
            description = "Applicant indicated they will proceed in SPEC 2v1 scenario (Yes)"
        )
        Predicate<CaseData> willProceedSpec2v1 =
            nullSafe(c -> YES.equals(c.getApplicant1ProceedWithClaimSpec2v1()));

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant proceed decision against respondent1 (1v2)",
            description = "Proceed-decision field exists for applicant against respondent 1 in 1v2 scenario"
        )
        Predicate<CaseData> hasProceedAgainstRespondent1_1v2 =
            nullSafe(c -> c.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2() != null);

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant will proceed against respondent1 (1v2)",
            description = "Applicant indicated they will proceed against respondent 1 in 1v2 scenario (Yes)"
        )
        Predicate<CaseData> willProceedAgainstRespondent1_1v2 =
            nullSafe(c -> YES.equals(c.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()));

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant proceed decision against respondent2 (1v2)",
            description = "Proceed-decision field exists for applicant against respondent 2 in 1v2 scenario"
        )
        Predicate<CaseData> hasProceedAgainstRespondent2_1v2 =
            nullSafe(c -> c.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2() != null);

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant will proceed against respondent2 (1v2)",
            description = "Applicant indicated they will proceed against respondent 2 in 1v2 scenario (Yes)"
        )
        Predicate<CaseData> willProceedAgainstRespondent2_1v2 =
            nullSafe(c -> YES.equals(c.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()));

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant proceed decision present (2v1)",
            description = "Proceed-decision field present for applicant in 2v1 multi-party scenario"
        )
        Predicate<CaseData> hasProceedMulti_2v1 =
            nullSafe(c -> c.getApplicant1ProceedWithClaimMultiParty2v1() != null);

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant will proceed (2v1)",
            description = "Applicant indicated they will proceed in 2v1 multi-party scenario (Yes)"
        )
        Predicate<CaseData> willProceedMulti_2v1 =
            nullSafe(c -> YES.equals(c.getApplicant1ProceedWithClaimMultiParty2v1()));

        @BusinessRule(
            group = "Applicant",
            summary = "Add applicant 2 (YES)",
            description = "Checks if add applicant 2 is equal to YES"
        )
        Predicate<CaseData> isAddApplicant2 =
            nullSafe(c -> YES.equals(c.getAddApplicant2()));

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant 2 proceed decision present (2v1)",
            description = "Proceed-decision field present for applicant 2 in 2v1 multi-party scenario"
        )
        Predicate<CaseData> hasProceedApplicant2Multi_2v1 =
            nullSafe(c -> c.getApplicant2ProceedWithClaimMultiParty2v1() != null);

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant 2 will proceed (2v1)",
            description = "Applicant 2 indicated they will proceed in 2v1 multi-party scenario (Yes)"
        )
        Predicate<CaseData> willProceedApplicant2Multi_2v1 =
            nullSafe(c -> YES.equals(c.getApplicant2ProceedWithClaimMultiParty2v1()));

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant correspondence address not required (Spec)",
            description = "True when applicant correspondence address is not required (Spec) — field `specAoSApplicantCorrespondenceAddressRequired` = No."
        )
        Predicate<CaseData> isNotCorrespondenceAddressRequiredSpec =
            nullSafe(c -> NO.equals(c.getSpecAoSApplicantCorrespondenceAddressRequired()));

    }

    interface Claim {

        @BusinessRule(
            group = "Claim",
            summary = "Case is SPEC (damages)",
            description = "Case access category indicates SPEC (damages)"
        )
        Predicate<CaseData> isSpecClaim =
            nullSafe(c -> SPEC_CLAIM.equals(c.getCaseAccessCategory()));

        @BusinessRule(
            group = "Claim",
            summary = "Case is UNSPEC",
            description = "Case access category indicates UNSPEC"
        )
        Predicate<CaseData> isUnspecClaim =
            nullSafe(c -> UNSPEC_CLAIM.equals(c.getCaseAccessCategory()));

        @BusinessRule(
            group = "Claim",
            summary = "Case is small claims track",
            description = "True when the case is on the small claims track (field `responseClaimTrack` indicates small claims)."
        )
        Predicate<CaseData> isSmallClaim =
            nullSafe(c -> SMALL_CLAIM.name().equals(c.getResponseClaimTrack()));

        @BusinessRule(
            group = "Claim",
            summary = "Allocated track is multi-track",
            description = "True when the case is allocated to the multi-track (field `allocatedTrack` = MULTI_CLAIM)."
        )
        Predicate<CaseData> isMultiClaim =
            nullSafe(c -> MULTI_CLAIM.equals(c.getAllocatedTrack()));

        @BusinessRule(
            group = "Claim",
            summary = "Defendant has not paid (full defence)",
            description = "True when the case is in a 'full defence not paid' situation (case data indicates the " +
                "defendant has not paid as required following a full defence outcome)."
        )
        Predicate<CaseData> isFullDefenceNotPaid =
            nullSafe(CaseData::isFullDefenceNotPaid);

        @BusinessRule(
            group = "Claim",
            summary = "Case is Multi Party Scenario",
            description = "True when the case has multiple parties (either `applicant2` or `respondent2` is present)."
        )
        Predicate<CaseData> isMultiParty =
            nullSafe(c -> c.getApplicant2() != null
            || c.getRespondent2() != null);

        @BusinessRule(
            group = "Claim",
            summary = "Claimant indicates the claim has been settled (part admission)",
            description = "True when a part admission case is treated as settled based on the recorded settlement " +
                "indicators (e.g. claimant intention to settle and, where applicable, confirmation of payment)."
        )
        Predicate<CaseData> isPartAdmitSettled =
            nullSafe(CaseData::isPartAdmitClaimSettled);

        @BusinessRule(
            group = "Claim",
            summary = "Claim submitted",
            description = "A claim has been submitted"
        )
        Predicate<CaseData> hasSubmittedDate =
            nullSafe(c -> c.getSubmittedDate() != null);

        @BusinessRule(
            group = "Claim",
            summary = "Claim issue date present",
            description = "A claim issue date has been recorded"
        )
        Predicate<CaseData> hasIssueDate =
            nullSafe(c -> c.getIssueDate() != null);

        @BusinessRule(
            group = "Claim",
            summary = "Claim notification deadline present",
            description = "A claim notification / issue deadline exists (claim has been notified)"
        )
        Predicate<CaseData> hasNotificationDeadline =
            nullSafe(c -> c.getClaimNotificationDeadline() != null);

        @BusinessRule(
            group = "Claim",
            summary = "Claim notification deadline passed",
            description = "Claim notification deadline exists and is before now (deadline expired)"
        )
        Predicate<CaseData> hasPassedNotificationDeadline =
            nullSafe(c -> c.getClaimNotificationDeadline() != null
                && c.getClaimNotificationDeadline().isBefore(LocalDateTime.now()));

        @BusinessRule(
            group = "Claim",
            summary = "Claim notification deadline in future",
            description = "Claim notification deadline exists and is after now"
        )
        Predicate<CaseData> hasFutureNotificationDeadline =
            nullSafe(c -> c.getClaimNotificationDeadline() != null
                && c.getClaimNotificationDeadline().isAfter(LocalDateTime.now()));

        @BusinessRule(
            group = "Claim",
            summary = "Claim notification date present",
            description = "A claim notification date has been recorded"
        )
        Predicate<CaseData> hasNotificationDate =
            nullSafe(c -> c.getClaimNotificationDate() != null);

        @BusinessRule(
            group = "Claim",
            summary = "Claim notification options present",
            description = "True when defendant claim-notification options are present (dynamic list non-null)."
        )
        Predicate<CaseData> hasNotifyOptions =
            nullSafe(c -> c.getDefendantSolicitorNotifyClaimOptions() != null);

        @BusinessRule(
            group = "Claim",
            summary = "Claim notify option is 'Both'",
            description = "Defendant claim-notification option value equals 'Both' (compared against the dynamic list label)."
        )
        Predicate<CaseData> isNotifyOptionsBoth =
            nullSafe(c -> "Both".equals(c.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel()));

        @BusinessRule(
            group = "Claim",
            summary = "Claim dismissed date present",
            description = "A claim dismissed date has been recorded (claim dismissed)"
        )
        Predicate<CaseData> hasDismissedDate =
            nullSafe(c -> c.getClaimDismissedDate() != null);

        @BusinessRule(
            group = "Claim",
            summary = "Claim dismissal deadline passed",
            description = "Claim dismissal deadline exists and is before now (deadline expired)"
        )
        Predicate<CaseData> hasPassedDismissalDeadline =
            nullSafe(c -> c.getClaimDismissedDeadline() != null
                && c.getClaimDismissedDeadline().isBefore(LocalDateTime.now()));

        @BusinessRule(
            group = "Claim",
            summary = "Change of representation present",
            description = "A change of representation record exists on the case"
        )
        Predicate<CaseData> hasChangeOfRepresentation =
            nullSafe(c -> c.getChangeOfRepresentation() != null);

    }

    interface ClaimDetails {

        @BusinessRule(
            group = "ClaimDetails",
            summary = "Claim details notification date present",
            description = "Claim details notification date exists on the case"
        )
        Predicate<CaseData> hasNotificationDate =
            nullSafe(c -> c.getClaimDetailsNotificationDate() != null);

        @BusinessRule(
            group = "ClaimDetails",
            summary = "Claim details notification deadline passed",
            description = "Claim details notification deadline exists and is before now (deadline expired)"
        )
        Predicate<CaseData> passedNotificationDeadline =
            nullSafe(c -> c.getClaimDetailsNotificationDeadline() != null
                && c.getClaimDetailsNotificationDeadline().isBefore(LocalDateTime.now()));

        @BusinessRule(
            group = "ClaimDetails",
            summary = "Claim details notification deadline in future",
            description = "Claim details notification deadline exists and is after now"
        )
        Predicate<CaseData> futureNotificationDeadline =
            nullSafe(c -> c.getClaimDetailsNotificationDeadline() != null
                && c.getClaimDetailsNotificationDeadline().isAfter(LocalDateTime.now()));

        @BusinessRule(
            group = "ClaimDetails",
            summary = "Claim details notification options present",
            description = "True when claim-details notification options are present for the defendant solicitor (dynamic list non-null)."
        )
        Predicate<CaseData> hasNotifyOptions =
            nullSafe(c -> c.getDefendantSolicitorNotifyClaimDetailsOptions() != null);

        @BusinessRule(
            group = "ClaimDetails",
            summary = "Claim details notify option is 'Both'",
            description = "Claim details notify option equals 'Both' (compared against the dynamic list label)."
        )
        Predicate<CaseData> isNotifyOptionsBoth =
            nullSafe(c -> Optional.ofNullable(c.getDefendantSolicitorNotifyClaimDetailsOptions())
                .map(DynamicList::getValue)
                .map(DynamicListElement::getLabel)
                .orElse("")
                .equalsIgnoreCase("Both"));

    }

    interface Claimant {

        @BusinessRule(
            group = "Claimant",
            summary = "Defendant single response covers both claimants",
            description = "Defendant indicated a single response applies to both claimants"
        )
        Predicate<CaseData> defendantSingleResponseToBothClaimants =
            nullSafe(c -> YES.equals(c.getDefendantSingleResponseToBothClaimants()));

        @BusinessRule(
            group = "Claimant",
            summary = "Claimants' SPEC responses differ",
            description = "Both claimant SPEC response enums are present and not equal"
        )
        Predicate<CaseData> responsesDifferSpec =
            nullSafe(c -> c.getClaimant1ClaimResponseTypeForSpec() != null
                && c.getClaimant2ClaimResponseTypeForSpec() != null
                && !c.getClaimant1ClaimResponseTypeForSpec().equals(c.getClaimant2ClaimResponseTypeForSpec()));

        @BusinessRule(
            group = "Claimant",
            summary = "Claimant agreed to mediation",
            description = "True when the claimant has opted into free mediation."
        )
        Predicate<CaseData> agreedToMediation =
            nullSafe(CaseData::hasClaimantAgreedToFreeMediation);

        @BusinessRule(
            group = "Claimant",
            summary = "Claimant has declined free mediation",
            description = "True when the claimant has opted out of free mediation (field indicates the claimant has not " +
                "agreed to free mediation)."
        )
        Predicate<CaseData> declinedMediation =
            nullSafe(CaseData::hasClaimantNotAgreedToFreeMediation);

        @BusinessRule(
            group = "Claimant",
            summary = "Claimant indicates they will not settle (part admission)",
            description = "True when, in a part admission scenario, the claimant's recorded decision indicates they will " +
                "not settle the claim on the defendant's part-admission terms."
        )
        Predicate<CaseData> isNotSettlePartAdmit =
            nullSafe(CaseData::isClaimantNotSettlePartAdmitClaim);

        @BusinessRule(
            group = "Claimant",
            summary = "Claimant intends to settle (part admission)",
            description = "True when, in a part admission scenario, the claimant's recorded intention is to settle the " +
                "claim (intention-to-settle flag is Yes)."
        )
        Predicate<CaseData> isIntentionSettlePartAdmit =
            nullSafe(CaseData::isClaimantIntentionSettlePartAdmit);

        @BusinessRule(
            group = "Claimant",
            summary = "Claimant intends not to settle (part admission)",
            description = "True when, in a part admission scenario, the claimant's recorded intention is not to settle " +
                "the claim (intention-to-settle flag is No)."
        )
        Predicate<CaseData> isIntentionNotSettlePartAdmit =
            nullSafe(CaseData::isClaimantIntentionNotSettlePartAdmit);

        @BusinessRule(
            group = "Claimant",
            summary = "Claimant 1 SPEC response type",
            description = "Checks claimant 1's SPEC response enum equals the provided type"
        )
        static Predicate<CaseData> responseTypeSpecClaimant1(RespondentResponseTypeSpec responseType) {
            return nullSafe(c -> responseType.equals(c.getClaimant1ClaimResponseTypeForSpec()));
        }

        @BusinessRule(
            group = "Claimant",
            summary = "Claimant 2 SPEC response type",
            description = "Checks claimant 2's SPEC response enum equals the provided type"
        )
        static Predicate<CaseData> responseTypeSpecClaimant2(RespondentResponseTypeSpec responseType) {
            return nullSafe(c -> responseType.equals(c.getClaimant2ClaimResponseTypeForSpec()));
        }

    }

    interface Hearing {

        @BusinessRule(
            group = "Hearing",
            summary = "Hearing reference present",
            description = "True when a hearing reference number is recorded for the case."
        )
        Predicate<CaseData> hasReference =
            nullSafe(c -> c.getHearingReferenceNumber() != null);

        @BusinessRule(
            group = "Hearing",
            summary = "Hearing is listed",
            description = "True when the case has a hearing listing (listing status is LISTING)."
        )
        Predicate<CaseData> isListed =
            nullSafe(c -> c.getListingOrRelisting() != null
                && LISTING.equals(c.getListingOrRelisting()));

        @BusinessRule(
            group = "Hearing",
            summary = "Hearing dismissed fee due date present",
            description = "True when a hearing fee due date (dismissed) is recorded on the case."
        )
        Predicate<CaseData> hasDismissedFeeDueDate =
            nullSafe(c -> c.getCaseDismissedHearingFeeDueDate() != null);

    }

    interface Judgment {

        @BusinessRule(
            group = "Judgment",
            summary = "Active judgment is by admission",
            description = "True when an active judgment exists and its type is 'judgment by admission'."
        )
        Predicate<CaseData> isByAdmission =
            nullSafe(c -> c.getActiveJudgment() != null
                && JudgmentType.JUDGMENT_BY_ADMISSION.equals(c.getActiveJudgment().getType()));

    }

    interface MultiParty {

        @BusinessRule(
            group = "MultiParty",
            summary = "Multi-party scenario: 1v1",
            description = "True when the multi-party scenario equals ONE_V_ONE."
        )
        Predicate<CaseData> isOneVOne =
            nullSafe(c -> MultiPartyScenario.ONE_V_ONE.equals(getMultiPartyScenario(c)));

        @BusinessRule(
            group = "MultiParty",
            summary = "Multi-party scenario: 1v2 (one legal rep)",
            description = "True when the multi-party scenario equals ONE_V_TWO_ONE_LEGAL_REP."
        )
        Predicate<CaseData> isOneVTwoOneLegalRep =
            nullSafe(c -> MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(c)));

        @BusinessRule(
            group = "MultiParty",
            summary = "Multi-party scenario: 1v2 (two legal reps)",
            description = "True when the multi-party scenario equals ONE_V_TWO_TWO_LEGAL_REP."
        )
        Predicate<CaseData> isOneVTwoTwoLegalRep =
            nullSafe(c -> MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(c)));
    }

    interface RepaymentPlan {

        @BusinessRule(
            group = "Repayment",
            summary = "Repayment plan accepted",
            description = "True when the applicant has accepted the proposed repayment plan."
        )
        Predicate<CaseData> accepted =
            nullSafe(CaseData::hasApplicantAcceptedRepaymentPlan);

        @BusinessRule(
            group = "Repayment",
            summary = "Repayment plan rejected",
            description = "True when the applicant has rejected the proposed repayment plan."
        )
        Predicate<CaseData> rejected =
            nullSafe(CaseData::hasApplicantRejectedRepaymentPlan);

    }

    interface Respondent {

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 is represented",
            description = "True when respondent 1 is legally represented (`respondent1Represented` = Yes)."
        )
        Predicate<CaseData> isRepresentedRespondent1 =
            nullSafe(c -> c.getRespondent1Represented() != null
            && YES.equals(c.getRespondent1Represented()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 is unrepresented",
            description = "True when respondent 1 is not legally represented (`respondent1Represented` = No)."
        )
        Predicate<CaseData> isUnrepresentedRespondent1 =
            nullSafe(c -> c.getRespondent1Represented() != null
                && NO.equals(c.getRespondent1Represented()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 is represented",
            description = "True when respondent 2 is legally represented (`respondent2Represented` = Yes)."
        )
        Predicate<CaseData> isRepresentedRespondent2 =
            nullSafe(c -> c.getRespondent2Represented() != null
                && YES.equals(c.getRespondent2Represented()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 is unrepresented",
            description = "True when respondent 2 is not legally represented (`respondent2Represented` = No)."
        )
        Predicate<CaseData> isUnrepresentedRespondent2 =
            nullSafe(c -> c.getRespondent2Represented() != null
                && NO.equals(c.getRespondent2Represented()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 is a registered organisation",
            description = "True when respondent 1 is a registered organisation (`respondent1OrgRegistered` = Yes)."
        )
        Predicate<CaseData> isOrgRegisteredRespondent1 =
            nullSafe(c -> c.getRespondent1OrgRegistered() != null
                && YES.equals(c.getRespondent1OrgRegistered()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 is not a registered organisation",
            description = "True when respondent 1 is not a registered organisation (`respondent1OrgRegistered` = No)."
        )
        Predicate<CaseData> isNotOrgRegisteredRespondent1 =
            nullSafe(c -> c.getRespondent1OrgRegistered() != null
                && NO.equals(c.getRespondent1OrgRegistered()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 represented and not a registered organisation",
            description = "True when respondent 1 is legally represented and is not a registered organisation " +
                "(`respondent1Represented` = Yes AND `respondent1OrgRegistered` = No)."
        )
        Predicate<CaseData> isRepresentedNotOrgRegisteredRespondent1 =
            nullSafe(c -> YES.equals(c.getRespondent1Represented())
                && NO.equals(c.getRespondent1OrgRegistered()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 is a registered organisation",
            description = "True when respondent 2 is a registered organisation (`respondent2OrgRegistered` = Yes)."
        )
        Predicate<CaseData> isOrgRegisteredRespondent2 =
            nullSafe(c -> c.getRespondent2OrgRegistered() != null
                && YES.equals(c.getRespondent2OrgRegistered()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 is not a registered organisation",
            description = "True when respondent 2 is not a registered organisation (`respondent2OrgRegistered` = No)."
        )
        Predicate<CaseData> isNotOrgRegisteredRespondent2 =
            nullSafe(c -> c.getRespondent2OrgRegistered() != null
                && NO.equals(c.getRespondent2OrgRegistered()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 represented and not a registered organisation",
            description = "True when respondent 2 is legally represented and is not a registered organisation " +
                "(`respondent2Represented` = Yes AND `respondent2OrgRegistered` = No or null)."
        )
        Predicate<CaseData> isRepresentedNotOrgRegisteredRespondent2 =
            nullSafe(c -> YES.equals(c.getRespondent2Represented())
                && !YES.equals(c.getRespondent2OrgRegistered()));

        @BusinessRule(
            group = "Respondent",
            summary = "Case has a second respondent",
            description = "Checks if respondent 2 is present in the case data."
        )
        Predicate<CaseData> hasRespondent2 =
            nullSafe(c -> c.getRespondent2() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Add respondent 2 flag present",
            description = "True when field `addRespondent2` is present (non-null)."
        )
        Predicate<CaseData> hasAddRespondent2 =
            nullSafe(c -> c.getAddRespondent2() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Add respondent 2 (YES)",
            description = "True when `addRespondent2` = Yes."
        )
        Predicate<CaseData> isAddRespondent2 =
            nullSafe(c -> YES.equals(c.getAddRespondent2()));

        @BusinessRule(
            group = "Respondent",
            summary = "Add respondent 2 (NO)",
            description = "True when `addRespondent2` = No."
        )
        Predicate<CaseData> isNotAddRespondent2 =
            nullSafe(c -> NO.equals(c.getAddRespondent2()));

        @BusinessRule(
            group = "Respondent",
            summary = "Same legal representative flag present",
            description = "Field `respondent2SameLegalRepresentative` present (non-null)."
        )
        Predicate<CaseData> hasSameLegalRepresentative =
            nullSafe(c -> c.getRespondent2SameLegalRepresentative() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondents have the same legal representative (YES)",
            description = "True when respondents are recorded as having the same legal representative " +
                "(`respondent2SameLegalRepresentative` = Yes)."
        )
        Predicate<CaseData> isSameLegalRepresentative =
            nullSafe(c -> YES.equals(c.getRespondent2SameLegalRepresentative()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondents do not have the same legal representative (NO)",
            description = "True when respondents are recorded as not having the same legal representative " +
                "(`respondent2SameLegalRepresentative` = No)."
        )
        Predicate<CaseData> isNotSameLegalRepresentative =
            nullSafe(c -> NO.equals(c.getRespondent2SameLegalRepresentative()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 time extension present",
            description = "Respondent 1 has been granted a time extension to respond"
        )
        Predicate<CaseData> hasTimeExtensionRespondent1 =
            nullSafe(c -> c.getRespondent1TimeExtensionDate() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 acknowledged notification",
            description = "Respondent 1 has acknowledged service of claim details"
        )
        Predicate<CaseData> hasAcknowledgedNotificationRespondent1 =
            nullSafe(c -> c.getRespondent1AcknowledgeNotificationDate() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 response date present",
            description = "Respondent 1 has submitted a response (response date recorded)"
        )
        Predicate<CaseData> hasResponseDateRespondent1 =
            nullSafe(c -> c.getRespondent1ResponseDate() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 response date present",
            description = "Respondent 2 has submitted a response (response date recorded)"
        )
        Predicate<CaseData> hasResponseDateRespondent2 =
            nullSafe(c -> c.getRespondent2ResponseDate() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 acknowledged notification",
            description = "Respondent 2 has acknowledged service of claim details"
        )
        Predicate<CaseData> hasAcknowledgedNotificationRespondent2 =
            nullSafe(c -> c.getRespondent2AcknowledgeNotificationDate() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 time extension present",
            description = "Respondent 2 has been granted a time extension to respond"
        )
        Predicate<CaseData> hasTimeExtensionRespondent2 =
            nullSafe(c -> c.getRespondent2TimeExtensionDate() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 has response type",
            description = "True when respondent 1 has a non-null response type enum."
        )
        Predicate<CaseData> hasResponseTypeRespondent1 =
            nullSafe(c -> c.getRespondent1ClaimResponseType() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 has response type (SPEC)",
            description = "True when respondent 1 has a non-null SPEC response type enum."
        )
        Predicate<CaseData> hasResponseTypeSpecRespondent1 =
            nullSafe(c -> c.getRespondent1ClaimResponseTypeForSpec() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 has response type",
            description = "True when respondent 2 has a non-null response type enum."
        )
        Predicate<CaseData> hasResponseTypeRespondent2 =
            nullSafe(c -> c.getRespondent2ClaimResponseType() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 has response type (SPEC)",
            description = "True when respondent 2 has a non-null SPEC response type enum."
        )
        Predicate<CaseData> hasResponseTypeSpecRespondent2 =
            nullSafe(c -> c.getRespondent2ClaimResponseTypeForSpec() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 intention to proceed present",
            description = "Respondent 1 has indicated an intention to proceed (intention field present)"
        )
        Predicate<CaseData> hasIntentionToProceedRespondent1 =
            nullSafe(c -> c.getRespondent1ClaimResponseIntentionType() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 intention to proceed present",
            description = "Respondent 2 has indicated an intention to proceed (intention field present)"
        )
        Predicate<CaseData> hasIntentionToProceedRespondent2 =
            nullSafe(c -> c.getRespondent2ClaimResponseIntentionType() != null);

        @BusinessRule(
            group = "Respondent",
            summary = "Respondents have same-response flag",
            description = "True when `respondentResponseIsSame` = Yes (respondents marked their responses as the same)."
        )
        Predicate<CaseData> isSameResponseFlag =
            nullSafe(c -> YES.equals(c.getRespondentResponseIsSame()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondents' non-SPEC responses differ",
            description = "Both respondents have non-SPEC response enums and they are different"
        )
        Predicate<CaseData> responsesDiffer =
            nullSafe(c -> c.getRespondent1ClaimResponseType() != null
                && c.getRespondent2ClaimResponseType() != null
                && !c.getRespondent1ClaimResponseType().equals(c.getRespondent2ClaimResponseType()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondents' SPEC responses differ",
            description = "Both respondents have SPEC response enums and they are different"
        )
        Predicate<CaseData> responsesDifferSpec =
            nullSafe(c -> c.getRespondent1ClaimResponseTypeForSpec() != null
                && c.getRespondent2ClaimResponseTypeForSpec() != null
                && !c.getRespondent1ClaimResponseTypeForSpec().equals(c.getRespondent2ClaimResponseTypeForSpec()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 response after respondent 2",
            description = "True when respondent 1 response timestamp is after respondent 2 response timestamp."
        )
        Predicate<CaseData> respondent1ResponseAfterRespondent2 =
            nullSafe(c -> c.getRespondent1ResponseDate().isAfter(c.getRespondent2ResponseDate()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 response after respondent 1",
            description = "True when respondent 2 response timestamp is after respondent 1 response timestamp."
        )
        Predicate<CaseData> respondent2ResponseAfterRespondent1 =
            nullSafe(c -> c.getRespondent2ResponseDate().isAfter(c.getRespondent1ResponseDate()));

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 response is of type",
            description = "Factory: checks respondent 1's non-SPEC response enum equals the provided RespondentResponseType"
        )
        static Predicate<CaseData> isTypeRespondent1(RespondentResponseType responseType) {
            return nullSafe(c -> c.getRespondent1ClaimResponseType() != null
                && responseType.equals(c.getRespondent1ClaimResponseType()));
        }

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 response is of type",
            description = "Factory: checks respondent 2's non-SPEC response enum equals the provided RespondentResponseType"
        )
        static Predicate<CaseData> isTypeRespondent2(RespondentResponseType responseType) {
            return nullSafe(c -> c.getRespondent2ClaimResponseType() != null
                && responseType.equals(c.getRespondent2ClaimResponseType()));
        }

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 response to applicant 2 is of type",
            description = "Factory: checks respondent 1's response-to-applicant2 enum equals the provided RespondentResponseType"
        )
        static Predicate<CaseData> isTypeRespondent1ToApplicant2(RespondentResponseType responseType) {
            return nullSafe(c -> responseType.equals(c.getRespondent1ClaimResponseTypeToApplicant2()));
        }

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 response is of type (SPEC)",
            description = "Factory: checks respondent 1's SPEC response enum equals the provided RespondentResponseTypeSpec"
        )
        static Predicate<CaseData> isTypeSpecRespondent1(RespondentResponseTypeSpec responseType) {
            return nullSafe(c -> c.getRespondent1ClaimResponseTypeForSpec() != null
                && responseType.equals(c.getRespondent1ClaimResponseTypeForSpec()));
        }

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 response is of type (SPEC)",
            description = "Factory: checks respondent 2's SPEC response enum equals the provided `RespondentResponseTypeSpec`."
        )
        static Predicate<CaseData> isTypeSpecRespondent2(RespondentResponseTypeSpec responseType) {
            return nullSafe(c -> c.getRespondent2ClaimResponseTypeForSpec() != null
                && responseType.equals(c.getRespondent2ClaimResponseTypeForSpec()));
        }

    }

    interface Payment {

        @BusinessRule(
            group = "Payment",
            summary = "Payment Pay Immediately",
            description = "Part admission payment time (IMMEDIATELY)"
        )
        Predicate<CaseData> isPayImmediately =
            nullSafe(CaseData::isPayImmediately);

        @BusinessRule(
            group = "Payment",
            summary = "Defendant offered to pay immediately and claimant accepted (part admission)",
            description = "True when the defendant has proposed to pay the part-admitted amount immediately and the " +
                "claimant has accepted that immediate payment option (determined from the SPEC pay-immediately acceptance flag/tag)."
        )
        Predicate<CaseData> isPartAdmitPayImmediately =
            nullSafe(CaseData::isPartAdmitPayImmediatelyAccepted);

        @BusinessRule(
            group = "Payment",
            summary = "When to be paid (text present)",
            description = "True when the 'When To Be Paid' text exists on the case."
        )
        Predicate<CaseData> hasWhenToBePaid =
            nullSafe(c -> c.getWhenToBePaidText() != null);

        @BusinessRule(
            group = "Payment",
            summary = "Payment successful (date present)",
            description = "True when a successful payment timestamp exists on the case."
        )
        Predicate<CaseData> hasPaymentSuccessfulDate =
            nullSafe(c -> c.getPaymentSuccessfulDate() != null);

        @BusinessRule(
            group = "Payment",
            summary = "Claim-issue payment succeeded",
            description = "True when claim-issue payment details exist and payment status equals SUCCESS."
        )
        Predicate<CaseData> claimIssuedPaymentSucceeded =
            nullSafe(c -> c.getClaimIssuedPaymentDetails() != null
                && SUCCESS.equals(c.getClaimIssuedPaymentDetails().getStatus()));

        @BusinessRule(
            group = "Payment",
            summary = "Payment details failed",
            description = "True when general payment details exist and payment status equals FAILED."
        )
        Predicate<CaseData> paymentDetailsFailed =
            nullSafe(c -> c.getPaymentDetails() != null
                && FAILED.equals(c.getPaymentDetails().getStatus()));

        @BusinessRule(
            group = "Payment",
            summary = "Claim-issue payment failed",
            description = "True when claim-issue payment details exist and payment status equals FAILED."
        )
        Predicate<CaseData> claimIssuedPaymentFailed =
            nullSafe(c -> c.getClaimIssuedPaymentDetails() != null
                && FAILED.equals(c.getClaimIssuedPaymentDetails().getStatus()));

    }

    interface TakenOffline {

        @BusinessRule(
            group = "TakenOffline",
            summary = "Not suitable for SDO reason provided",
            description = "True when a reason explaining why the case is not suitable for a standard directions order " +
                "(SDO) has been provided."
        )
        Predicate<CaseData> hasSdoReasonNotSuitable =
            nullSafe(c -> c.getReasonNotSuitableSDO() != null
                && StringUtils.isNotBlank(c.getReasonNotSuitableSDO().getInput()));

        @BusinessRule(
            group = "TakenOffline",
            summary = "Case taken offline (date present)",
            description = "True when the case has been marked as taken offline (offline date recorded)."
        )
        Predicate<CaseData> dateExists =
            nullSafe(c -> c.getTakenOfflineDate() != null);

        @BusinessRule(
            group = "TakenOffline",
            summary = "Case taken offline by staff",
            description = "True when HMCTS staff recorded that the case was taken offline."
        )
        Predicate<CaseData> byStaffDateExists =
            nullSafe(c -> c.getTakenOfflineByStaffDate() != null);

        @BusinessRule(
            group = "TakenOffline",
            summary = "Draw directions order required",
            description = "True when the case has been marked as requiring a 'draw directions order'."
        )
        Predicate<CaseData> hasDrawDirectionsOrderRequired =
            nullSafe(c -> c.getDrawDirectionsOrderRequired() != null);

    }

    interface Language {

        @BusinessRule(
            group = "Language",
            summary = "Change of language preference present",
            description = "True when the case records a change in language preference."
        )
        Predicate<CaseData> hasChangePreference =
            nullSafe(c -> c.getChangeLanguagePreference() != null);

        @BusinessRule(
            group = "Language",
            summary = "Respondent response marked bilingual",
            description = "True when the respondent indicated their response is bilingual (translated documents may be present)."
        )
        Predicate<CaseData> isRespondentBilingual =
            nullSafe(CaseData::isRespondentResponseBilingual);

        @BusinessRule(
            group = "Language",
            summary = "Claimant marked bilingual",
            description = "True when the claimant has a bilingual language preference."
        )
        Predicate<CaseData> isClaimantBilingual =
            nullSafe(CaseData::isClaimantBilingual);

    }

    interface Lip {

        @BusinessRule(
            group = "Lip",
            summary = "Party is unrepresented",
            description = "Case has at least one Litigant-in-Person (LiP) participant"
        )
        Predicate<CaseData> isPartyUnrepresented =
            nullSafe(CaseData::isLipCase);

        @BusinessRule(
            group = "Lip",
            summary = "Lip v Lip one-v-one variant",
            description = "True when the case is a LiP v LiP one-v-one variant."
        )
        Predicate<CaseData> isLiPvLipCase =
            nullSafe(CaseData::isLipvLipOneVOne);

        @BusinessRule(
            group = "Lip",
            summary = "Lip v LR one-v-one variant",
            description = "True when the case is a LiP v LR one-v-one variant."
        )
        Predicate<CaseData> isLiPvLRCase =
            nullSafe(CaseData::isLipvLROneVOne);

        @BusinessRule(
            group = "Lip",
            summary = "LiP Help With Fee",
            description = "True when the case is a LiP with Help With Fees."
        )
        Predicate<CaseData> isHelpWithFees =
            nullSafe(CaseData::isHelpWithFees);

        @BusinessRule(
            group = "Lip",
            summary = "Translated response document uploaded",
            description = "True when a translated response document has been uploaded to system documents."
        )
        Predicate<CaseData> translatedDocumentUploaded =
            nullSafe(CaseData::isTranslatedDocumentUploaded);

        @BusinessRule(
            group = "Lip",
            summary = "CCJ requested by admission",
            description = "True when the applicant has requested a County Court Judgment (CCJ) by admission."
        )
        Predicate<CaseData> ccjRequestByAdmissionFlag =
            nullSafe(CaseData::isCcjRequestJudgmentByAdmission);

        @BusinessRule(
            group = "Lip",
            summary = "Respondent signed settlement agreement",
            description = "True when the respondent has signed the digital settlement agreement."
        )
        Predicate<CaseData> respondentSignedSettlementAgreement =
            nullSafe(CaseData::isRespondentRespondedToSettlementAgreement);

        @BusinessRule(
            group = "Lip",
            summary = "NOC submitted for LiP claimant",
            description = "True when a Notice of Change for a LiP claimant was submitted."
        )
        Predicate<CaseData> nocApplyForLiPClaimant =
            nullSafe(CaseData::nocApplyForLiPClaimant);

        @BusinessRule(
            group = "Lip",
            summary = "NOC submitted for LiP defendant before offline",
            description = "True when a Notice of Change for a LiP defendant was submitted prior to the case being taken offline."
        )
        Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline =
            nullSafe(CaseData::nocApplyForLiPDefendantBeforeOffline);

        @BusinessRule(
            group = "Lip",
            summary = "NOC submitted for LiP defendant",
            description = "True when a Notice of Change for a LiP defendant was submitted."
        )
        Predicate<CaseData> nocSubmittedForLiPDefendant =
            nullSafe(CaseData::nocApplyForLiPDefendant);

        @BusinessRule(
            group = "Lip",
            summary = "Case contains LiP participant",
            description = "True when at least one party on the case is a LiP (applicant or respondent)."
        )
        Predicate<CaseData> caseContainsLiP =
            nullSafe(c -> c.isRespondent1LiP()
                || c.isRespondent2LiP()
                || c.isApplicantNotRepresented());

        @BusinessRule(
            group = "Lip",
            summary = "At least one LiP defendant is at claim issued",
            description = "True when at least one defendant is a litigant in person and is flagged as being 'at claim " +
                "issued' (either `defendant1LIPAtClaimIssued` or `defendant2LIPAtClaimIssued` = Yes)."
        )
        Predicate<CaseData> isClaimIssued =
            nullSafe(c -> YES.equals(c.getDefendant1LIPAtClaimIssued())
            || YES.equals(c.getDefendant2LIPAtClaimIssued())
            );

        @BusinessRule(
            group = "Lip",
            summary = "Applicant 1 has not settled claim (NO)",
            description = "True when applicant 1 has not indicated they will settle the claim."
        )
        Predicate<CaseData> isNotSettleClaimApplicant1 =
            nullSafe(c -> NO.equals(c.getCaseDataLiP().getApplicant1SettleClaim()));

        @BusinessRule(
            group = "Lip",
            summary = "Respondent 1 has Pin-In-Post enabled (SPEC)",
            description = "True when respondent 1 has PIN-in-post enabled (LR, SPEC)."
        )
        Predicate<CaseData> hasPinInPost =
            nullSafe(c -> c.getRespondent1PinToPostLRspec() != null);

    }

    interface Mediation {

        @BusinessRule(
            group = "Mediation",
            summary = "Applicant MP has not agreed to free mediation",
            description = "True when the multi-party applicant is recorded as not having agreed to free mediation in the " +
                "SPEC small-claims mediation fields (`hasAgreedFreeMediation` = No)."
        )
        Predicate<CaseData> isNotRequiredApplicantMPSpec =
            nullSafe(c -> c.getApplicantMPClaimMediationSpecRequired() != null
                && YesOrNo.NO.equals(c.getApplicantMPClaimMediationSpecRequired().getHasAgreedFreeMediation()));

        @BusinessRule(
            group = "Mediation",
            summary = "Applicant 1 has not agreed to free mediation",
            description = "True when applicant 1 is recorded as not having agreed to free mediation in the SPEC " +
                "small-claims mediation fields (`hasAgreedFreeMediation` = No)."
        )
        Predicate<CaseData> isNotAgreedFreeMediationApplicant1Spec =
            nullSafe(c -> c.getApplicant1ClaimMediationSpecRequired() != null
                && YesOrNo.NO.equals(c.getApplicant1ClaimMediationSpecRequired().getHasAgreedFreeMediation()));

        @BusinessRule(
            group = "Mediation",
            summary = "Respondent 1 has agreed to free mediation",
            description = "True when respondent 1 is recorded as having agreed to free mediation for SPEC " +
                "(`responseClaimMediationSpecRequired` = Yes)."
        )
        Predicate<CaseData> isRequiredRespondent1Spec =
            nullSafe(c -> YES.equals(c.getResponseClaimMediationSpecRequired()));

        @BusinessRule(
            group = "Mediation",
            summary = "Respondent 2 has not agreed to free mediation",
            description = "True when respondent 2 is recorded as not having agreed to free mediation for SPEC " +
                "(`responseClaimMediationSpec2Required` = No)."
        )
        Predicate<CaseData> isNotRequiredRespondent2Spec =
            nullSafe(c -> NO.equals(c.getResponseClaimMediationSpec2Required()));

        @BusinessRule(
            group = "Mediation",
            summary = "Applicant 1 mediation contact information present",
            description = "True when applicant 1's mediation contact information has been provided on the case " +
                "(contact info object present)."
        )
        Predicate<CaseData> hasContactInfoApplicant1 =
            nullSafe(c -> c.getApp1MediationContactInfo() != null);

        @BusinessRule(
            group = "Mediation",
            summary = "Respondent 1 mediation contact information present",
            description = "True when respondent 1's mediation contact information has been provided on the case " +
                "(contact info object present)."
        )
        Predicate<CaseData> hasContactInfoRespondent1 =
            nullSafe(c -> c.getResp1MediationContactInfo() != null);

        @BusinessRule(
            group = "Mediation",
            summary = "Respondent 2 mediation contact information present",
            description = "True when respondent 2's mediation contact information has been provided on the case " +
                "(contact info object present)."
        )
        Predicate<CaseData> hasContactInfoRespondent2 =
            nullSafe(c -> c.getResp2MediationContactInfo() != null);

        @BusinessRule(
            group = "Mediation",
            summary = "LiP applicant 1 CARM response present",
            description = "True when the LiP applicant 1 CARM response value is present in the LiP case data " +
                "(field answered)."
        )
        Predicate<CaseData> hasResponseCarmLiPApplicant1 =
            nullSafe(c -> c.getCaseDataLiP() != null
                     && c.getCaseDataLiP().getApplicant1LiPResponseCarm() != null);

        @BusinessRule(
            group = "Mediation",
            summary = "LiP respondent 1 CARM response present",
            description = "True when the LiP respondent 1 CARM response value is present in the LiP case data " +
                "(field answered)."
        )
        Predicate<CaseData> hasResponseCarmLiPRespondent1 =
            nullSafe(c -> c.getCaseDataLiP() != null
                && c.getCaseDataLiP().getRespondent1MediationLiPResponseCarm() != null);

        @BusinessRule(
            group = "Mediation",
            summary = "Unsuccessful mediation reason present",
            description = "True when an unsuccessful mediation reason has been recorded for the case (single " +
                "'unsuccessful reason' value present)."
        )
        Predicate<CaseData> hasReasonUnsuccessful =
            nullSafe(c -> c.getMediation().getUnsuccessfulMediationReason() != null);

        @BusinessRule(
            group = "Mediation",
            summary = "Unsuccessful mediation reasons selected (multi-select present)",
            description = "True when the multi-select list of unsuccessful mediation reasons is present on the case " +
                "(list field exists, even if nothing is selected yet)."
        )
        Predicate<CaseData> hasReasonUnsuccessfulMultiSelect =
            nullSafe(c -> c.getMediation().getMediationUnsuccessfulReasonsMultiSelect() != null);

        @BusinessRule(
            group = "Mediation",
            summary = "Unsuccessful mediation reasons selected (multi-select has values)",
            description = "True when at least one unsuccessful mediation reason has been selected in the multi-select " +
                "list (list exists and contains one or more values)."
        )
        Predicate<CaseData> hasReasonUnsuccessfulMultiSelectValue =
            nullSafe(c -> c.getMediation().getMediationUnsuccessfulReasonsMultiSelect() != null
            && !c.getMediation().getMediationUnsuccessfulReasonsMultiSelect().isEmpty());
    }

}
