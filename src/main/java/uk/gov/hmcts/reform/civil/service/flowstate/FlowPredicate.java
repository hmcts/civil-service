package uk.gov.hmcts.reform.civil.service.flowstate;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseDataParent;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeDecisionOption.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeDecisionOption.LIST_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeDecisionOption.MAKE_AN_ORDER;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeDecisionOption.REQUEST_MORE_INFO;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting.LISTING;

public class FlowPredicate {

    private FlowPredicate() {
        //Utility classes require a private constructor for checkstyle
    }

    public static final Predicate<CaseData> hasNotifiedClaimDetailsToBoth = caseData ->
        Optional.ofNullable(caseData.getDefendantSolicitorNotifyClaimDetailsOptions())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse("")
            .equalsIgnoreCase("Both");

    public static final Predicate<CaseData> paymentSuccessful = caseData ->
        !caseData.isApplicantNotRepresented()
            && (caseData.getPaymentSuccessfulDate() != null
            || (caseData.getClaimIssuedPaymentDetails() != null
            && caseData.getClaimIssuedPaymentDetails().getStatus() == SUCCESS));

    public static final Predicate<CaseData> claimIssued = caseData ->
        caseData.getClaimNotificationDeadline() != null;

    public static final Predicate<CaseData> claimDetailsNotifiedTimeExtension = caseData ->
        caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null;

    public static final Predicate<CaseData> takenOfflineAfterClaimDetailsNotified = caseData ->
        caseData.getClaimDetailsNotificationDate() != null
            && caseData.getDefendantSolicitorNotifyClaimDetailsOptions() != null
            && !hasNotifiedClaimDetailsToBoth.test(caseData);

    public static final Predicate<CaseData> notificationAcknowledged = FlowPredicate::getPredicateForNotificationAcknowledged;

    private static boolean getPredicateForNotificationAcknowledged(CaseData caseData) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        boolean respondent1Acknowledged = caseData.getRespondent1AcknowledgeNotificationDate() != null;
        boolean respondent2Acknowledged = caseData.getRespondent2AcknowledgeNotificationDate() != null;

        return switch (scenario) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> respondent1Acknowledged || respondent2Acknowledged;
            default -> respondent1Acknowledged;
        };
    }

    public static final Predicate<CaseData> respondentTimeExtension = FlowPredicate::getPredicateForTimeExtension;

    private static boolean getPredicateForTimeExtension(CaseData caseData) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        boolean respondent1TimeExtension = caseData.getRespondent1TimeExtensionDate() != null;
        boolean respondent2TimeExtension = caseData.getRespondent2TimeExtensionDate() != null;

        return scenario == ONE_V_TWO_TWO_LEGAL_REP
            ? respondent1TimeExtension || respondent2TimeExtension
            : respondent1TimeExtension;
    }

    private static boolean getPredicateForResponseType(CaseData caseData, RespondentResponseType responseType) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        boolean respondent1Matches = caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1ClaimResponseType() == responseType;

        return switch (scenario) {
            case ONE_V_TWO_ONE_LEGAL_REP -> respondent1Matches
                && (YES.equals(caseData.getRespondentResponseIsSame()) || caseData.getRespondent2ClaimResponseType() == responseType);
            case ONE_V_TWO_TWO_LEGAL_REP -> respondent1Matches && caseData.getRespondent2ClaimResponseType() == responseType;
            case ONE_V_ONE -> respondent1Matches;
            case TWO_V_ONE -> respondent1Matches && caseData.getRespondent1ClaimResponseTypeToApplicant2() == responseType;
        };
    }

    public static final Predicate<CaseData> divergentRespondWithDQAndGoOffline = FlowPredicate::isDivergentResponsesWithDQAndGoOffline;

    private static boolean isDivergentResponsesWithDQAndGoOffline(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP ->
                //scenario: either of them have submitted full defence response
                !caseData.getRespondent1ClaimResponseType().equals(caseData.getRespondent2ClaimResponseType())
                    && (caseData.getRespondent1ClaimResponseType().equals(FULL_DEFENCE)
                    || caseData.getRespondent2ClaimResponseType().equals(FULL_DEFENCE));
            case ONE_V_TWO_TWO_LEGAL_REP ->
                //scenario: latest response is full defence
                !Objects.equals(
                    caseData.getRespondent1ClaimResponseType(),
                    caseData.getRespondent2ClaimResponseType()
                )
                    && ((caseData.getRespondent2ClaimResponseType().equals(FULL_DEFENCE)
                    && caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()))
                    || (caseData.getRespondent1ClaimResponseType().equals(FULL_DEFENCE)
                    && caseData.getRespondent1ResponseDate().isAfter(caseData.getRespondent2ResponseDate())));
            case TWO_V_ONE -> (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
                || FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeToApplicant2()))
                && !(FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
                && FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeToApplicant2()));
            default -> false;
        };
    }

    public static final Predicate<CaseData> divergentRespondGoOffline = FlowPredicate::isDivergentResponsesGoOffline;

    private static boolean isDivergentResponsesGoOffline(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP -> !Objects.equals(
                caseData.getRespondent1ClaimResponseType(),
                caseData.getRespondent2ClaimResponseType()
            )
                //scenario: latest response is not full defence
                && (((!caseData.getRespondent2ClaimResponseType().equals(FULL_DEFENCE)
                && caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate())
                || !caseData.getRespondent1ClaimResponseType().equals(FULL_DEFENCE)
                && caseData.getRespondent1ResponseDate().isAfter(caseData.getRespondent2ResponseDate())))
                //scenario: neither responses are full defence
                || (!caseData.getRespondent1ClaimResponseType().equals(FULL_DEFENCE)
                && !caseData.getRespondent2ClaimResponseType().equals(FULL_DEFENCE)));
            case ONE_V_TWO_ONE_LEGAL_REP ->
                !caseData.getRespondent1ClaimResponseType().equals(caseData.getRespondent2ClaimResponseType())
                    && (!caseData.getRespondent1ClaimResponseType().equals(FULL_DEFENCE)
                    && !caseData.getRespondent2ClaimResponseType().equals(FULL_DEFENCE));
            case TWO_V_ONE -> !(FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType()) || FULL_DEFENCE
                .equals(caseData.getRespondent1ClaimResponseTypeToApplicant2()));
            default -> false;
        };
    }

    public static final Predicate<CaseData> allResponsesReceived = FlowPredicate::getPredicateForResponses;

    private static boolean getPredicateForResponses(CaseData caseData) {
        MultiPartyScenario scenario = Objects.requireNonNull(getMultiPartyScenario(caseData));
        boolean respondent1ResponseReceived = caseData.getRespondent1ResponseDate() != null;

        if (scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            return respondent1ResponseReceived && caseData.getRespondent2ResponseDate() != null;
        }
        return respondent1ResponseReceived;
    }

    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceived = FlowPredicate::getPredicateForAwaitingResponsesFullDefenceReceived;

    private static boolean getPredicateForAwaitingResponsesFullDefenceReceived(CaseData caseData) {
        MultiPartyScenario scenario = Objects.requireNonNull(getMultiPartyScenario(caseData));

        if (scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            boolean respondent1FullDefence = caseData.getRespondent1ClaimResponseType() != null
                && caseData.getRespondent2ClaimResponseType() == null
                && FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType());
            boolean respondent2FullDefence = caseData.getRespondent1ClaimResponseType() == null
                && caseData.getRespondent2ClaimResponseType() != null
                && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType());
            return respondent1FullDefence || respondent2FullDefence;
        }

        return false;
    }

    public static final Predicate<CaseData> awaitingResponsesFullAdmitReceived = FlowPredicate::getPredicateForAwaitingResponsesFullAdmitReceived;

    private static boolean getPredicateForAwaitingResponsesFullAdmitReceived(CaseData caseData) {
        MultiPartyScenario scenario = Objects.requireNonNull(getMultiPartyScenario(caseData));

        if (scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            boolean respondent1FullDefence = caseData.getRespondent1ClaimResponseType() != null
                && caseData.getRespondent2ClaimResponseType() == null
                && FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseType());
            boolean respondent2FullDefence = caseData.getRespondent1ClaimResponseType() == null
                && caseData.getRespondent2ClaimResponseType() != null
                && FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseType());
            return respondent1FullDefence || respondent2FullDefence;
        }

        return false;
    }

    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceOrFullAdmitReceived
        = FlowPredicate::getPredicateForAwaitingResponsesNonFullDefenceOrFullAdmitReceived;

    private static boolean getPredicateForAwaitingResponsesNonFullDefenceOrFullAdmitReceived(CaseData caseData) {
        MultiPartyScenario scenario = Objects.requireNonNull(getMultiPartyScenario(caseData));

        if (scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            boolean respondent1NonFullDefenceOrFullAdmit = caseData.getRespondent1ClaimResponseType() != null
                && caseData.getRespondent2ClaimResponseType() == null
                && !FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
                && !FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseType());
            boolean respondent2NonFullDefenceOrFullAdmit = caseData.getRespondent1ClaimResponseType() == null
                && caseData.getRespondent2ClaimResponseType() != null
                && !FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType())
                && !FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseType());
            return respondent1NonFullDefenceOrFullAdmit || respondent2NonFullDefenceOrFullAdmit;
        }

        return false;
    }

    public static final Predicate<CaseData> counterClaim = caseData ->
        getPredicateForResponseType(caseData, COUNTER_CLAIM);

    public static final Predicate<CaseData> fullDefenceProceed = FlowPredicate::getPredicateForClaimantIntentionProceed;

    public static final Predicate<CaseData> takenOfflineSDONotDrawn = caseData ->
        caseData.getReasonNotSuitableSDO() != null
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
            && caseData.getTakenOfflineDate() != null
            && caseData.getTakenOfflineByStaffDate() == null;

    public static final Predicate<CaseData> fullDefenceNotProceed = FlowPredicate::getPredicateForClaimantIntentionNotProceed;

    public static final Predicate<CaseData> takenOfflineBySystem = caseData ->
        caseData.getTakenOfflineDate() != null && caseData.getChangeOfRepresentation() == null;

    public static final Predicate<CaseData> takenOfflineAfterSDO = caseData ->
        caseData.getDrawDirectionsOrderRequired() != null
            && caseData.getReasonNotSuitableSDO() == null
            && caseData.getTakenOfflineDate() != null
            && caseData.getTakenOfflineByStaffDate() == null;

    public static final Predicate<CaseData> takenOfflineByStaff = caseData ->
        caseData.getTakenOfflineByStaffDate() != null;

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimNotified = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getClaimDetailsNotificationDate() == null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getClaimDetailsNotificationDeadline() != null
            && caseData.getClaimDetailsNotificationDeadline().isAfter(LocalDateTime.now());

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimDetailsNotified = FlowPredicate::getPredicateTakenOfflineByStaffAfterClaimDetailsNotified;

    public static boolean getPredicateTakenOfflineByStaffAfterClaimDetailsNotified(CaseData caseData) {
        boolean commonConditions = caseData.getTakenOfflineByStaffDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getClaimDismissedDate() == null;

        if (getMultiPartyScenario(caseData) == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP
            || getMultiPartyScenario(caseData) == MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP) {
            return commonConditions
                && caseData.getRespondent2ResponseDate() == null
                && caseData.getRespondent2AcknowledgeNotificationDate() == null
                && caseData.getRespondent2TimeExtensionDate() == null;
        } else {
            return commonConditions;
        }
    }

    public static final Predicate<CaseData> caseDismissedAfterDetailNotified = FlowPredicate::getPredicateForCaseDismissedAfterDetailNotified;

    private static boolean getPredicateForCaseDismissedAfterDetailNotified(CaseData caseData) {
        boolean commonConditions = caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1ClaimResponseIntentionType() == null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getTakenOfflineByStaffDate() == null;

        MultiPartyScenario scenario = getMultiPartyScenario(caseData);

        if (scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP || scenario == MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP) {
            return commonConditions
                && caseData.getRespondent2AcknowledgeNotificationDate() == null
                && caseData.getRespondent2TimeExtensionDate() == null
                && caseData.getRespondent2ClaimResponseIntentionType() == null
                && caseData.getRespondent2ResponseDate() == null;
        }

        return commonConditions;
    }

    public static final Predicate<CaseData> applicantOutOfTimeNotBeingTakenOffline = caseData ->
        caseData.getApplicant1ResponseDeadline() != null
            && caseData.getApplicant1ResponseDeadline().isBefore(LocalDateTime.now())
            && caseData.getApplicant1ResponseDate() == null
            && caseData.getTakenOfflineByStaffDate() == null;

    public static final Predicate<CaseData> applicantOutOfTimeProcessedByCamunda = caseData ->
        caseData.getTakenOfflineDate() != null;

    public static final Predicate<CaseData> pastClaimDetailsNotificationDeadline = caseData ->
        caseData.getClaimDetailsNotificationDeadline() != null
            && caseData.getClaimDetailsNotificationDeadline().isBefore(LocalDateTime.now())
            && caseData.getClaimDetailsNotificationDate() == null
            && caseData.getClaimNotificationDate() != null;

    public static final Predicate<CaseData> claimDismissedByCamunda = caseData ->
        caseData.getClaimDismissedDate() != null;

    public static final Predicate<CaseData> caseDismissedPastHearingFeeDue = caseData ->
        caseData.getCaseDismissedHearingFeeDueDate() != null;

    public static final Predicate<CaseData> fullAdmissionSpec = caseData ->
        getPredicateForResponseTypeSpec(caseData, RespondentResponseTypeSpec.FULL_ADMISSION);

    public static final Predicate<CaseData> partAdmissionSpec = caseData ->
        getPredicateForResponseTypeSpec(caseData, RespondentResponseTypeSpec.PART_ADMISSION);

    public static final Predicate<CaseData> counterClaimSpec = caseData ->
        getPredicateForResponseTypeSpec(caseData, RespondentResponseTypeSpec.COUNTER_CLAIM);

    public static final Predicate<CaseData> fullDefenceSpec = caseData ->
        getPredicateForResponseTypeSpec(caseData, RespondentResponseTypeSpec.FULL_DEFENCE);

    private static boolean getPredicateForResponseTypeSpec(CaseData caseData, RespondentResponseTypeSpec responseType) {
        // If not a SPEC claim, return false
        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return false;
        }

        boolean basePredicate = caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() == responseType;

        MultiPartyScenario scenario = getMultiPartyScenario(caseData);

        return switch (scenario) {
            case ONE_V_TWO_ONE_LEGAL_REP -> basePredicate && (YES.equals(caseData.getRespondentResponseIsSame())
                || caseData.getRespondent2ClaimResponseTypeForSpec() == responseType);
            case ONE_V_TWO_TWO_LEGAL_REP -> basePredicate
                && caseData.getRespondent2ClaimResponseTypeForSpec() == responseType
                // For the time being, even if the response is the same, 1v2ds only deals with full defence
                && responseType == RespondentResponseTypeSpec.FULL_DEFENCE;
            case ONE_V_ONE -> basePredicate;
            case TWO_V_ONE -> {
                if (YES.equals(caseData.getDefendantSingleResponseToBothClaimants())) {
                    yield responseType.equals(caseData.getRespondent1ClaimResponseTypeForSpec());
                } else {
                    yield responseType.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                        && responseType.equals(caseData.getClaimant2ClaimResponseTypeForSpec());
                }
            }
        };
    }

    public static final Predicate<CaseData> divergentRespondWithDQAndGoOfflineSpec = FlowPredicate::isDivergentResponsesWithDQAndGoOfflineSpec;

    private static boolean isDivergentResponsesWithDQAndGoOfflineSpec(CaseData caseData) {
        // If not a SPEC claim, return false
        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return false;
        }

        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP ->
                // scenario: only one of them has submitted a full defence response
                caseData.getRespondent1ClaimResponseTypeForSpec() != null
                    && !caseData.getRespondent1ClaimResponseTypeForSpec()
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                    && NO.equals(caseData.getRespondentResponseIsSame())
                    && (RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    || RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
            case ONE_V_TWO_TWO_LEGAL_REP ->
                // scenario: latest response is full defence
                caseData.getRespondent1ClaimResponseTypeForSpec() != null
                    && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                    && caseData.getRespondent1ResponseDate() != null
                    && caseData.getRespondent2ResponseDate() != null
                    && (!caseData.getRespondent1ClaimResponseTypeForSpec()
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                    // for the time being, 1v2ds not full defence goes offline
                    || caseData.getRespondent1ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_DEFENCE);
            case TWO_V_ONE ->
                (RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                    || RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec()))
                    && !(RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                    && RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec()));
            default -> false;
        };
    }

    public static final Predicate<CaseData> divergentRespondGoOfflineSpec = FlowPredicate::isDivergentResponsesGoOfflineSpec;

    private static boolean isDivergentResponsesGoOfflineSpec(CaseData caseData) {
        // If not a SPEC claim, return false
        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return false;
        }

        return switch (getMultiPartyScenario(caseData)) {
            // 1v2 different solicitors, DQ is always created for both defendants
            case ONE_V_TWO_ONE_LEGAL_REP ->
                caseData.getRespondent1ClaimResponseTypeForSpec() != null
                    && !caseData.getRespondent1ClaimResponseTypeForSpec()
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                    && caseData.getRespondentResponseIsSame() != YES
                    && (!RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    && !RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
            case TWO_V_ONE ->
                (!RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                    && !RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))
                    && (caseData.getClaimant1ClaimResponseTypeForSpec() != null
                    && caseData.getClaimant2ClaimResponseTypeForSpec() != null)
                    && !caseData.getClaimant1ClaimResponseTypeForSpec()
                    .equals(caseData.getClaimant2ClaimResponseTypeForSpec());
            default -> false;
        };
    }

    public static final Predicate<CaseData> specClaim = caseData ->
        SPEC_CLAIM.equals(caseData.getCaseAccessCategory());

    private static boolean getPredicateForClaimantIntentionProceed(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP, ONE_V_ONE ->
                    YES.equals(caseData.getApplicant1ProceedWithClaim());
                case TWO_V_ONE -> YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1());
                default -> false;
            };
        } else {
            return switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        || YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2());
                case ONE_V_ONE -> YES.equals(caseData.getApplicant1ProceedWithClaim());
                case TWO_V_ONE -> YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                    || YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1());
            };
        }
    }

    private static boolean getPredicateForClaimantIntentionNotProceed(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP, ONE_V_ONE ->
                    NO.equals(caseData.getApplicant1ProceedWithClaim());
                case TWO_V_ONE -> NO.equals(caseData.getApplicant1ProceedWithClaimSpec2v1());
                default -> false;
            };
        } else {
            return switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        && NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2());
                case ONE_V_ONE -> NO.equals(caseData.getApplicant1ProceedWithClaim());
                case TWO_V_ONE -> NO.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                    && NO.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1());
            };
        }
    }

    public static final Predicate<CaseData> acceptRepaymentPlan = caseData ->
        caseData.isLipvLipOneVOne()
                ? caseData.hasApplicantAcceptedRepaymentPlan() && caseData.getTakenOfflineByStaffDate() == null
                : caseData.hasApplicantAcceptedRepaymentPlan();

    public static final Predicate<CaseData> rejectRepaymentPlan = caseData ->
        caseData.isLipvLipOneVOne()
                ? caseData.hasApplicantRejectedRepaymentPlan() && caseData.getTakenOfflineByStaffDate() == null
                : caseData.hasApplicantRejectedRepaymentPlan();

    public static final Predicate<CaseData> isRespondentResponseLangIsBilingual =
        CaseDataParent::isRespondentResponseBilingual;

    public static final Predicate<CaseData> onlyInitialRespondentResponseLangIsBilingual = caseData ->
        // dependent lip selected bilingual during response
        caseData.getChangeLanguagePreference() == null && caseData.isRespondentResponseBilingual();

    // This field is used in LR ITP, prevent going another path in preview
    public static final Predicate<CaseData> isOneVOneResponseFlagSpec = caseData ->
        caseData.getShowResponseOneVOneFlag() != null;

    public static final Predicate<CaseData> isInHearingReadiness = caseData ->
        caseData.getHearingReferenceNumber() != null
        && caseData.getListingOrRelisting() != null
        && caseData.getListingOrRelisting().equals(LISTING)
        && caseData.getCaseDismissedHearingFeeDueDate() == null
        && caseData.getTakenOfflineDate() == null
        && caseData.getTakenOfflineByStaffDate() == null;

    public static final Predicate<CaseData> caseContainsLiP = caseData ->
        caseData.isRespondent1LiP()
            || caseData.isRespondent2LiP()
            || caseData.isApplicantNotRepresented();

    public static final Predicate<CaseData> isDefendantNoCOnlineForCaseAfterJBA = caseData ->
        caseData.isLipCase()
            && caseData.getActiveJudgment() != null
            && JudgmentType.JUDGMENT_BY_ADMISSION.equals(caseData.getActiveJudgment().getType())
            && caseData.getTakenOfflineDate() != null
            && caseData.getChangeOfRepresentation() != null;

    public static final Predicate<CaseData> withOutNoticeApplication = caseData ->
            caseData.getGeneralAppInformOtherParty() != null
                    && caseData.getGeneralAppRespondentAgreement().getHasAgreed() == YES
                    || (caseData.getGeneralAppInformOtherParty() != null
                    && caseData.getGeneralAppInformOtherParty().getIsWithNotice() == NO);

    public static final Predicate<CaseData> withNoticeApplication = caseData ->
            caseData.getGeneralAppInformOtherParty() != null
                    && caseData.getGeneralAppRespondentAgreement().getHasAgreed() == NO
                    || (caseData.getGeneralAppInformOtherParty() != null
                    && caseData.getGeneralAppInformOtherParty().getIsWithNotice() == YES);

    public static final Predicate<CaseData> paymentSuccess = caseData ->
            caseData.getGeneralAppPBADetails() != null
                    && caseData.getGeneralAppPBADetails().getPaymentDetails() != null
                    && caseData.getGeneralAppPBADetails().getPaymentDetails().getStatus() == SUCCESS;

    public static final Predicate<CaseData> judgeMadeDecision = caseData ->
            caseData.getJudicialDecision() != null;

    public static final Predicate<CaseData> judgeMadeListingForHearing = caseData ->
            caseData.getJudicialDecision() != null
                    && caseData.getJudicialDecision().getDecision().equals(LIST_FOR_A_HEARING)
                    && caseData.getJudicialListForHearing() != null;

    public static final Predicate<CaseData> judgeRequestAdditionalInfo = caseData ->
            caseData.getJudicialDecision() != null
                    && caseData.getJudicialDecision().getDecision().equals(REQUEST_MORE_INFO);

    public static final Predicate<CaseData> judgeMadeDirections = caseData ->
            caseData.getJudicialDecision() != null
                    && caseData.getJudicialDecision().getDecision().equals(MAKE_AN_ORDER)
                    && caseData.getJudicialDecisionMakeOrder().getMakeAnOrder().equals(GIVE_DIRECTIONS_WITHOUT_HEARING);

    public static final Predicate<CaseData> judgeMadeOrder = caseData ->
            caseData.getJudicialDecision() != null
                    && (caseData.getJudicialDecision().getDecision().equals(MAKE_AN_ORDER)
                    && caseData.getJudicialDecisionMakeOrder().getMakeAnOrder().equals(APPROVE_OR_EDIT))
                    || (caseData.getJudicialDecision().getDecision().equals(FREE_FORM_ORDER));

    public static final Predicate<CaseData> judgeMadeWrittenRep = caseData ->
            caseData.getJudicialDecision() != null
                    && caseData.getJudicialDecision().getDecision().equals(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS);

    public static final Predicate<CaseData> judgeMadeDismissalOrder = caseData ->
            caseData.getJudicialDecision() != null
                    && caseData.getJudicialDecision().getDecision().equals(MAKE_AN_ORDER)
                    && caseData.getJudicialDecisionMakeOrder().getMakeAnOrder().equals(DISMISS_THE_APPLICATION);

    public static final Predicate<CaseData> isLipApplication = caseData -> caseData.getIsGaApplicantLip() == YES;
    public static final Predicate<CaseData> isLipRespondent = caseData -> caseData.getIsGaRespondentOneLip() == YES;

    public static final Predicate<CaseData> gaCaseContainsLiP = caseData ->
            YesOrNo.YES.equals(caseData.getIsGaApplicantLip())
                    || YesOrNo.YES.equals(caseData.getIsGaRespondentOneLip());

    public static final Predicate<CaseData> isVaryJudgementAppByResp = caseData -> caseData.getParentClaimantIsApplicant().equals(NO)
            && caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT);

    public static final Predicate<CaseData> isWelshApplicant =
            caseData -> (caseData.isApplicationBilingual());

    public static final Predicate<CaseData> judgeRequestForMoreInfo = caseData ->
            caseData.getJudicialDecision() != null
                    && caseData.getJudicialDecision().getDecision().equals(REQUEST_MORE_INFO)
                    && (caseData.getJudicialDecisionRequestMoreInfo() != null
                    && caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption() != GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY);

    public static final Predicate<CaseData> isWelshJudgeDecision =
            caseData -> isWelshApplicant.test(caseData)
                    && (judgeMadeWrittenRep.test(caseData) || judgeMadeDirections.test(caseData)
                    || judgeRequestForMoreInfo.test(caseData) || judgeMadeOrder.test(caseData)
                    || judgeMadeDismissalOrder.test(caseData) || judgeMadeListingForHearing.test(caseData));

    public static final Predicate<CaseData> isFreeFeeWelshApplication = caseData ->
            isWelshApplicant.test(caseData) && (caseData.getGeneralAppPBADetails() != null
                    && (caseData.getGeneralAppPBADetails().getFee().getCode().equals("FREE")) && caseData.getGeneralAppType().getTypes()
                    .contains(GeneralApplicationTypes.ADJOURN_HEARING));
}
