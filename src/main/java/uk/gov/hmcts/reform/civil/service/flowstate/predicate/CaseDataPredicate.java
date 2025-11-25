package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
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
 * are intentionally atomic and should be composed in `Composer` or other higher-level
 * compositions.
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
public class CaseDataPredicate {

    private CaseDataPredicate() {
    }

    public static class Applicant {

        @BusinessRule(
            group = "Applicant",
            summary = "Applicant is represented",
            description = "Applicant is legally represented (applicant not marked as self-represented)"
        )
        public static final Predicate<CaseData> isRepresented =
            nullSafe(c -> !c.isApplicantNotRepresented());
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant response date present",
            description = "Applicant's initial response date exists on the case"
        )
        public static final Predicate<CaseData> hasResponseDate =
            nullSafe(c -> c.getApplicant1ResponseDate() != null);
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant response deadline passed",
            description = "Applicant response deadline exists and is before now (deadline expired)"
        )
        public static final Predicate<CaseData> hasPassedResponseDeadline =
            nullSafe(c -> c.getApplicant1ResponseDeadline() != null
                && c.getApplicant1ResponseDeadline().isBefore(LocalDateTime.now()));
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant proceed decision present",
            description = "There is a recorded proceed / not-proceed decision for the applicant"
        )
        public static final Predicate<CaseData> hasProceedDecision =
            nullSafe(c -> c.getApplicant1ProceedWithClaim() != null);
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant will proceed",
            description = "Applicant has indicated they will proceed with the claim (Yes)"
        )
        public static final Predicate<CaseData> willProceed =
            nullSafe(c -> YES.equals(c.getApplicant1ProceedWithClaim()));
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant proceed decision present (SPEC 2v1)",
            description = "Proceed-decision field present for SPEC 2v1 scenario"
        )
        public static final Predicate<CaseData> hasProceedDecisionSpec2v1 =
            nullSafe(c -> c.getApplicant1ProceedWithClaimSpec2v1() != null);
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant will proceed (SPEC 2v1)",
            description = "Applicant indicated they will proceed in SPEC 2v1 scenario (Yes)"
        )
        public static final Predicate<CaseData> willProceedSpec2v1 =
            nullSafe(c -> YES.equals(c.getApplicant1ProceedWithClaimSpec2v1()));
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant proceed decision against respondent1 (1v2)",
            description = "Proceed-decision field exists for applicant against respondent 1 in 1v2 scenario"
        )
        public static final Predicate<CaseData> hasProceedAgainstRespondent1_1v2 =
            nullSafe(c -> c.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2() != null);
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant will proceed against respondent1 (1v2)",
            description = "Applicant indicated they will proceed against respondent 1 in 1v2 scenario (Yes)"
        )
        public static final Predicate<CaseData> willProceedAgainstRespondent1_1v2 =
            nullSafe(c -> YES.equals(c.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()));
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant proceed decision against respondent2 (1v2)",
            description = "Proceed-decision field exists for applicant against respondent 2 in 1v2 scenario"
        )
        public static final Predicate<CaseData> hasProceedAgainstRespondent2_1v2 =
            nullSafe(c -> c.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2() != null);
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant will proceed against respondent2 (1v2)",
            description = "Applicant indicated they will proceed against respondent 2 in 1v2 scenario (Yes)"
        )
        public static final Predicate<CaseData> willProceedAgainstRespondent2_1v2 =
            nullSafe(c -> YES.equals(c.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()));
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant proceed decision present (2v1)",
            description = "Proceed-decision field present for applicant in 2v1 multi-party scenario"
        )
        public static final Predicate<CaseData> hasProceedMulti_2v1 =
            nullSafe(c -> c.getApplicant1ProceedWithClaimMultiParty2v1() != null);
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant will proceed (2v1)",
            description = "Applicant indicated they will proceed in 2v1 multi-party scenario (Yes)"
        )
        public static final Predicate<CaseData> willProceedMulti_2v1 =
            nullSafe(c -> YES.equals(c.getApplicant1ProceedWithClaimMultiParty2v1()));
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant 2 proceed decision present (2v1)",
            description = "Proceed-decision field present for applicant 2 in 2v1 multi-party scenario"
        )
        public static final Predicate<CaseData> hasProceedApplicant2Multi_2v1 =
            nullSafe(c -> c.getApplicant2ProceedWithClaimMultiParty2v1() != null);
        @BusinessRule(
            group = "Applicant",
            summary = "Applicant 2 will proceed (2v1)",
            description = "Applicant 2 indicated they will proceed in 2v1 multi-party scenario (Yes)"
        )
        public static final Predicate<CaseData> willProceedApplicant2Multi_2v1 =
            nullSafe(c -> YES.equals(c.getApplicant2ProceedWithClaimMultiParty2v1()));

        private Applicant() {
        }

    }

    public static class Claim {

        @BusinessRule(
            group = "Claim",
            summary = "Case is SPEC (damages)",
            description = "Case access category indicates SPEC (damages) service"
        )
        public static final Predicate<CaseData> isSpecClaim =
            nullSafe(c -> c.getCaseAccessCategory() == SPEC_CLAIM);
        @BusinessRule(
            group = "Claim",
            summary = "Claim notification deadline present",
            description = "A claim notification / issue deadline exists (claim has been notified)"
        )
        public static final Predicate<CaseData> hasNotificationDeadline =
            nullSafe(c -> c.getClaimNotificationDeadline() != null);
        @BusinessRule(
            group = "Claim",
            summary = "Claim notification date present",
            description = "A claim notification date has been recorded"
        )
        public static final Predicate<CaseData> hasNotificationDate =
            nullSafe(c -> c.getClaimNotificationDate() != null);
        @BusinessRule(
            group = "Claim",
            summary = "Claim dismissed date present",
            description = "A claim dismissed date has been recorded (claim dismissed)"
        )
        public static final Predicate<CaseData> hasDismissedDate =
            nullSafe(c -> c.getClaimDismissedDate() != null);
        @BusinessRule(
            group = "Claim",
            summary = "Claim dismissal deadline present",
            description = "A claim dismissal deadline has been recorded"
        )
        public static final Predicate<CaseData> hasDismissalDeadline =
            nullSafe(c -> c.getClaimDismissedDeadline() != null);
        @BusinessRule(
            group = "Claim",
            summary = "Claim dismissal deadline passed",
            description = "Claim dismissal deadline exists and is before now (deadline expired)"
        )
        public static final Predicate<CaseData> hasPassedDismissalDeadline =
            hasDismissalDeadline.and(c -> c.getClaimDismissedDeadline().isBefore(LocalDateTime.now()));
        @BusinessRule(
            group = "Claim",
            summary = "1v1 response flag present",
            description = "Flag used to indicate a one-v-one response was provided (LR ITP update)"
        )
        public static final Predicate<CaseData> hasOneVOneResponseFlag =
            nullSafe(c -> c.getShowResponseOneVOneFlag() != null);

        private Claim() {
        }

    }

    public static class ClaimDetails {

        @BusinessRule(
            group = "ClaimDetails",
            summary = "Respondent 1 time extension present",
            description = "Respondent 1 has been granted a time extension to respond"
        )
        public static final Predicate<CaseData> hasTimeExtensionRespondent1 =
            nullSafe(c -> c.getRespondent1TimeExtensionDate() != null);
        @BusinessRule(
            group = "ClaimDetails",
            summary = "Respondent 1 acknowledged notification",
            description = "Respondent 1 has acknowledged service of claim details"
        )
        public static final Predicate<CaseData> acknowledgedNotificationRespondent1 =
            nullSafe(c -> c.getRespondent1AcknowledgeNotificationDate() != null);
        @BusinessRule(
            group = "ClaimDetails",
            summary = "Respondent 1 response date present",
            description = "Respondent 1 has submitted a response (response date recorded)"
        )
        public static final Predicate<CaseData> hasResponseDateRespondent1 =
            nullSafe(c -> c.getRespondent1ResponseDate() != null);
        @BusinessRule(
            group = "ClaimDetails",
            summary = "Respondent 2 response date present",
            description = "Respondent 2 has submitted a response (response date recorded)"
        )
        public static final Predicate<CaseData> hasResponseDateRespondent2 =
            nullSafe(c -> c.getRespondent2ResponseDate() != null);
        @BusinessRule(
            group = "ClaimDetails",
            summary = "Respondent 2 acknowledged notification",
            description = "Respondent 2 has acknowledged service of claim details"
        )
        public static final Predicate<CaseData> acknowledgedNotificationRespondent2 =
            nullSafe(c -> c.getRespondent2AcknowledgeNotificationDate() != null);
        @BusinessRule(
            group = "ClaimDetails",
            summary = "Respondent 2 time extension present",
            description = "Respondent 2 has been granted a time extension to respond"
        )
        public static final Predicate<CaseData> hasTimeExtensionRespondent2 =
            nullSafe(c -> c.getRespondent2TimeExtensionDate() != null);
        @BusinessRule(
            group = "ClaimDetails",
            summary = "Claim details notification date present",
            description = "Claim details notification date exists on the case"
        )
        public static final Predicate<CaseData> hasNotificationDate =
            nullSafe(c -> c.getClaimDetailsNotificationDate() != null);
        @BusinessRule(
            group = "ClaimDetails",
            summary = "Claim details notification deadline passed",
            description = "Claim details notification deadline exists and is before now (deadline expired)"
        )
        public static final Predicate<CaseData> hasPassedNotificationDeadline =
            nullSafe(c -> c.getClaimDetailsNotificationDeadline() != null
                && c.getClaimDetailsNotificationDeadline().isBefore(LocalDateTime.now()));
        @BusinessRule(
            group = "ClaimDetails",
            summary = "Claim details notification deadline in future",
            description = "Claim details notification deadline exists and is after now"
        )
        public static final Predicate<CaseData> hasFutureNotificationDeadline =
            nullSafe(c -> c.getClaimDetailsNotificationDeadline() != null
                && c.getClaimDetailsNotificationDeadline().isAfter(LocalDateTime.now()));
        @BusinessRule(
            group = "ClaimDetails",
            summary = "Claim details notification options present",
            description = "Notification options for claim details were sent to defendant solicitor"
        )
        public static final Predicate<CaseData> hasNotifyOptions =
            nullSafe(c -> c.getDefendantSolicitorNotifyClaimDetailsOptions() != null);
        @BusinessRule(
            group = "ClaimDetails",
            summary = "Claim details notify option is 'Both'",
            description = "The dynamic list for claim details notify options was set to 'Both'"
        )
        public static final Predicate<CaseData> hasNotifyOptionsBoth =
            nullSafe(c -> Optional.ofNullable(c.getDefendantSolicitorNotifyClaimDetailsOptions())
                .map(DynamicList::getValue)
                .map(DynamicListElement::getLabel)
                .orElse("")
                .equalsIgnoreCase("Both"));

        private ClaimDetails() {
        }
    }

    public static class Claimant {

        @BusinessRule(
            group = "Claimant",
            summary = "Defendant single response covers both claimants",
            description = "Defendant indicated a single response applies to both claimants"
        )
        public static final Predicate<CaseData> defendantSingleResponseToBothClaimants =
            nullSafe(c -> YES.equals(c.getDefendantSingleResponseToBothClaimants()));
        @BusinessRule(
            group = "Claimant",
            summary = "Claimants' SPEC responses differ",
            description = "Both claimant SPEC response enums are present and not equal"
        )
        public static final Predicate<CaseData> responsesDifferSpec =
            nullSafe(c -> c.getClaimant1ClaimResponseTypeForSpec() != null
                && c.getClaimant2ClaimResponseTypeForSpec() != null
                && !c.getClaimant1ClaimResponseTypeForSpec().equals(c.getClaimant2ClaimResponseTypeForSpec()));
        @BusinessRule(
            group = "Claimant",
            summary = "Claimant agreed to mediation",
            description = "Claimant has opted into free mediation"
        )
        public static final Predicate<CaseData> agreedToMediation =
            nullSafe(CaseData::hasClaimantAgreedToFreeMediation);

        private Claimant() {
        }

        @BusinessRule(
            group = "Claimant",
            summary = "Claimant 1 SPEC response type",
            description = "Checks claimant 1's SPEC response enum equals the provided type"
        )
        public static Predicate<CaseData> responseTypeSpecClaimant1(RespondentResponseTypeSpec responseType) {
            return nullSafe(c -> responseType.equals(c.getClaimant1ClaimResponseTypeForSpec()));
        }

        @BusinessRule(
            group = "Claimant",
            summary = "Claimant 2 SPEC response type",
            description = "Checks claimant 2's SPEC response enum equals the provided type"
        )
        public static Predicate<CaseData> responseTypeSpecClaimant2(RespondentResponseTypeSpec responseType) {
            return nullSafe(c -> responseType.equals(c.getClaimant2ClaimResponseTypeForSpec()));
        }

    }

    public static class Hearing {

        @BusinessRule(
            group = "Hearing",
            summary = "Hearing reference present",
            description = "A hearing reference number is recorded for the case"
        )
        public static final Predicate<CaseData> hasReference =
            nullSafe(c -> c.getHearingReferenceNumber() != null);
        @BusinessRule(
            group = "Hearing",
            summary = "Hearing is listed",
            description = "The case has a hearing listing (listing status is LISTING)"
        )
        public static final Predicate<CaseData> isListed =
            nullSafe(c -> c.getListingOrRelisting() != null
                && c.getListingOrRelisting().equals(LISTING));
        @BusinessRule(
            group = "Hearing",
            summary = "Hearing dismissed fee due date present",
            description = "A hearing fee due date (dismissed) is recorded on the case"
        )
        public static final Predicate<CaseData> hasDismissedFeeDueDate =
            nullSafe(c -> c.getCaseDismissedHearingFeeDueDate() != null);

        private Hearing() {
        }

    }

    public static class Judgment {

        @BusinessRule(
            group = "Judgment",
            summary = "Active judgment is by admission",
            description = "An active judgment exists and its type is 'judgment by admission'"
        )
        public static final Predicate<CaseData> isByAdmission =
            nullSafe(c -> c.getActiveJudgment() != null
                && JudgmentType.JUDGMENT_BY_ADMISSION.equals(c.getActiveJudgment().getType()));

        private Judgment() {
        }

    }

    public static class MultiParty {

        @BusinessRule(
            group = "MultiParty",
            summary = "Multi-party scenario: 1v2 (one legal rep)",
            description = "Case multi-party scenario equals ONE_V_TWO_ONE_LEGAL_REP"
        )
        public static final Predicate<CaseData> isOneVTwoOneLegalRep =
            nullSafe(c -> MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(c)));
        @BusinessRule(
            group = "MultiParty",
            summary = "Multi-party scenario: 1v2 (two legal reps)",
            description = "Case multi-party scenario equals ONE_V_TWO_TWO_LEGAL_REP"
        )
        public static final Predicate<CaseData> isOneVTwoTwoLegalRep =
            nullSafe(c -> MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(c)));

        private MultiParty() {
        }

    }

    public static class RepaymentPlan {

        @BusinessRule(
            group = "Repayment",
            summary = "Repayment plan accepted",
            description = "Applicant has accepted the proposed repayment plan"
        )
        public static final Predicate<CaseData> accepted =
            nullSafe(CaseData::hasApplicantAcceptedRepaymentPlan);
        @BusinessRule(
            group = "Repayment",
            summary = "Repayment plan rejected",
            description = "Applicant has rejected the proposed repayment plan"
        )
        public static final Predicate<CaseData> rejected =
            nullSafe(CaseData::hasApplicantRejectedRepaymentPlan);

        private RepaymentPlan() {
        }

    }

    public static class Respondent {

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 response date present",
            description = "Respondent 1 has submitted a response (response date recorded)"
        )
        public static final Predicate<CaseData> hasResponseDateRespondent1 =
            nullSafe(c -> c.getRespondent1ResponseDate() != null);
        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 response date present",
            description = "Respondent 2 has submitted a response (response date recorded)"
        )
        public static final Predicate<CaseData> hasResponseDateRespondent2 =
            nullSafe(c -> c.getRespondent2ResponseDate() != null);
        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 has response type",
            description = "Respondent 1 has a non-null response type enum"
        )
        public static final Predicate<CaseData> hasResponseTypeRespondent1 =
            nullSafe(c -> c.getRespondent1ClaimResponseType() != null);
        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 has response type",
            description = "Respondent 2 has a non-null response type enum"
        )
        public static final Predicate<CaseData> hasResponseTypeRespondent2 =
            nullSafe(c -> c.getRespondent2ClaimResponseType() != null);
        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 intention to proceed present",
            description = "Respondent 1 has indicated an intention to proceed (intention field present)"
        )
        public static final Predicate<CaseData> hasIntentionToProceedRespondent1 =
            nullSafe(c -> c.getRespondent1ClaimResponseIntentionType() != null);
        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 intention to proceed present",
            description = "Respondent 2 has indicated an intention to proceed (intention field present)"
        )
        public static final Predicate<CaseData> hasIntentionToProceedRespondent2 =
            nullSafe(c -> c.getRespondent2ClaimResponseIntentionType() != null);
        @BusinessRule(
            group = "Respondent",
            summary = "Respondents have same-response flag",
            description = "Yes/No flag indicating respondents marked their responses as the same"
        )
        public static final Predicate<CaseData> respondentsHaveSameResponseFlag =
            nullSafe(c -> YES.equals(c.getRespondentResponseIsSame()));
        @BusinessRule(
            group = "Respondent",
            summary = "Respondents' non-SPEC responses differ",
            description = "Both respondents have non-SPEC response enums and they are different"
        )
        public static final Predicate<CaseData> responsesDiffer =
            nullSafe(c -> c.getRespondent1ClaimResponseType() != null
                && c.getRespondent2ClaimResponseType() != null
                && !c.getRespondent1ClaimResponseType().equals(c.getRespondent2ClaimResponseType()));
        @BusinessRule(
            group = "Respondent",
            summary = "Respondents' SPEC responses differ",
            description = "Both respondents have SPEC response enums and they are different"
        )
        public static final Predicate<CaseData> responsesDifferSpec =
            nullSafe(c -> c.getRespondent1ClaimResponseTypeForSpec() != null
                && c.getRespondent2ClaimResponseTypeForSpec() != null
                && !c.getRespondent1ClaimResponseTypeForSpec().equals(c.getRespondent2ClaimResponseTypeForSpec()));
        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 response after respondent 2",
            description = "Respondent 1 response timestamp is after respondent 2 response timestamp"
        )
        public static final Predicate<CaseData> respondent1ResponseAfterRespondent2 =
            nullSafe(c -> c.getRespondent1ResponseDate().isAfter(c.getRespondent2ResponseDate()));
        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 response after respondent 1",
            description = "Respondent 2 response timestamp is after respondent 1 response timestamp"
        )
        public static final Predicate<CaseData> respondent2ResponseAfterRespondent1 =
            nullSafe(c -> c.getRespondent2ResponseDate().isAfter(c.getRespondent1ResponseDate()));

        private Respondent() {
        }

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 response is of type",
            description = "Factory: checks respondent 1's non-SPEC response enum equals the provided RespondentResponseType"
        )
        public static Predicate<CaseData> isTypeRespondent1(RespondentResponseType responseType) {
            return nullSafe(c -> c.getRespondent1ClaimResponseType() != null
                && responseType.equals(c.getRespondent1ClaimResponseType()));
        }

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 response is of type",
            description = "Factory: checks respondent 2's non-SPEC response enum equals the provided RespondentResponseType"
        )
        public static Predicate<CaseData> isTypeRespondent2(RespondentResponseType responseType) {
            return nullSafe(c -> c.getRespondent2ClaimResponseType() != null
                && responseType.equals(c.getRespondent2ClaimResponseType()));
        }

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 response to applicant 2 is of type",
            description = "Factory: checks respondent 1's response-to-applicant2 enum equals the provided RespondentResponseType"
        )
        public static Predicate<CaseData> isTypeRespondent1ToApplicant2(RespondentResponseType responseType) {
            return nullSafe(c -> responseType.equals(c.getRespondent1ClaimResponseTypeToApplicant2()));
        }

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 1 SPEC response is of type",
            description = "Factory: checks respondent 1's SPEC response enum equals the provided RespondentResponseTypeSpec"
        )
        public static Predicate<CaseData> isTypeSpecRespondent1(RespondentResponseTypeSpec responseType) {
            return nullSafe(c -> responseType.equals(c.getRespondent1ClaimResponseTypeForSpec()));
        }

        @BusinessRule(
            group = "Respondent",
            summary = "Respondent 2 SPEC response is of type",
            description = "Factory: checks respondent 2's SPEC response enum equals the provided RespondentResponseTypeSpec"
        )
        public static Predicate<CaseData> isTypeSpecRespondent2(RespondentResponseTypeSpec responseType) {
            return nullSafe(c -> responseType.equals(c.getRespondent2ClaimResponseTypeForSpec()));
        }

    }

    public static class Payment {

        @BusinessRule(
            group = "Payment",
            summary = "Payment successful (date present)",
            description = "A successful payment timestamp exists on the case"
        )
        public static final Predicate<CaseData> hasPaymentSuccessfulDate =
            nullSafe(c -> c.getPaymentSuccessfulDate() != null);
        @BusinessRule(
            group = "Payment",
            summary = "Claim-issue payment succeeded",
            description = "Claim-issue payment details exist and payment status equals SUCCESS"
        )
        public static final Predicate<CaseData> claimIssuedPaymentSucceeded =
            nullSafe(c -> c.getClaimIssuedPaymentDetails() != null
                && SUCCESS.equals(c.getClaimIssuedPaymentDetails().getStatus()));

        private Payment() {
        }

    }

    public static class TakenOffline {

        @BusinessRule(
            group = "TakenOffline",
            summary = "Not suitable for SDO reason provided",
            description = "A reason explaining why the case is not suitable for a standard directions order (SDO) was provided"
        )
        public static final Predicate<CaseData> hasSdoReasonNotSuitable =
            nullSafe(c -> c.getReasonNotSuitableSDO() != null
                && StringUtils.isNotBlank(c.getReasonNotSuitableSDO().getInput()));
        @BusinessRule(
            group = "TakenOffline",
            summary = "Case taken offline (date present)",
            description = "The case has been marked as taken offline (offline date recorded)"
        )
        public static final Predicate<CaseData> dateExists =
            nullSafe(c -> c.getTakenOfflineDate() != null);
        @BusinessRule(
            group = "TakenOffline",
            summary = "Case taken offline by staff",
            description = "HMCTS staff recorded that the case was taken offline"
        )
        public static final Predicate<CaseData> byStaffDateExists =
            nullSafe(c -> c.getTakenOfflineByStaffDate() != null);
        @BusinessRule(
            group = "TakenOffline",
            summary = "Change of representation present",
            description = "A change of representation record exists on the case"
        )
        public static final Predicate<CaseData> hasChangeOfRepresentation =
            nullSafe(c -> c.getChangeOfRepresentation() != null);
        @BusinessRule(
            group = "TakenOffline",
            summary = "Draw directions order required",
            description = "The case has been marked as requiring a 'draw directions order'"
        )
        public static final Predicate<CaseData> hasDrawDirectionsOrderRequired =
            nullSafe(c -> c.getDrawDirectionsOrderRequired() != null);

        private TakenOffline() {
        }

    }

    public static class Language {

        @BusinessRule(
            group = "Language",
            summary = "Change of language preference present",
            description = "The case records a change in language preference"
        )
        public static final Predicate<CaseData> hasChangePreference =
            nullSafe(c -> c.getChangeLanguagePreference() != null);
        @BusinessRule(
            group = "Language",
            summary = "Respondent response marked bilingual",
            description = "Respondent indicated their response is bilingual (translated documents may be present)"
        )
        public static final Predicate<CaseData> isBilingualFlag =
            nullSafe(CaseData::isRespondentResponseBilingual);

        private Language() {
        }

    }

    public static class Lip {

        @BusinessRule(
            group = "Lip",
            summary = "Party is unrepresented",
            description = "Case has at least one Litigant-in-Person (LiP) participant"
        )
        public static final Predicate<CaseData> partyIsUnrepresented =
            nullSafe(CaseData::isLipCase);
        @BusinessRule(
            group = "Lip",
            summary = "LiP one-v-one variant",
            description = "Case is a LiP one-v-one variant (helper predicate)"
        )
        public static final Predicate<CaseData> isLiPCase =
            nullSafe(CaseData::isLipvLipOneVOne);
        @BusinessRule(
            group = "Lip",
            summary = "Translated response document uploaded",
            description = "A translated response document has been uploaded to system documents"
        )
        public static final Predicate<CaseData> translatedResponseDocumentUploaded =
            nullSafe(CaseData::isTranslatedDocumentUploaded);
        @BusinessRule(
            group = "Lip",
            summary = "CCJ requested by admission",
            description = "Applicant has requested a County Court Judgment (CCJ) by admission"
        )
        public static final Predicate<CaseData> ccjRequestByAdmissionFlag =
            nullSafe(CaseData::isCcjRequestJudgmentByAdmission);
        @BusinessRule(
            group = "Lip",
            summary = "Respondent signed settlement agreement",
            description = "Respondent has signed the digital settlement agreement"
        )
        public static final Predicate<CaseData> respondentSignedSettlementAgreement =
            nullSafe(CaseData::isRespondentRespondedToSettlementAgreement);
        @BusinessRule(
            group = "Lip",
            summary = "NOC submitted for LiP defendant before offline",
            description = "A Notice of Change for a LiP defendant was submitted prior to the case being taken offline"
        )
        public static final Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline =
            nullSafe(CaseData::nocApplyForLiPDefendantBeforeOffline);
        @BusinessRule(
            group = "Lip",
            summary = "NOC submitted for LiP defendant",
            description = "A Notice of Change for a LiP defendant was submitted"
        )
        public static final Predicate<CaseData> nocSubmittedForLiPDefendant =
            nullSafe(CaseData::nocApplyForLiPDefendant);
        @BusinessRule(
            group = "Lip",
            summary = "Case contains LiP participant",
            description = "At least one party on the case is a LiP (applicant or respondent)"
        )
        public static final Predicate<CaseData> caseContainsLiP =
            nullSafe(c -> c.isRespondent1LiP()
                || c.isRespondent2LiP()
                || c.isApplicantNotRepresented());

        private Lip() {
        }

    }

}
