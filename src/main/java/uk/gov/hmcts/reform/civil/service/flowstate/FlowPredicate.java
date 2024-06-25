package uk.gov.hmcts.reform.civil.service.flowstate;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseDataParent;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
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

    public static final Predicate<CaseData> claimSubmittedOneRespondentRepresentative = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getRespondent1Represented() != NO
            && (caseData.getAddRespondent2() == null
            || caseData.getAddRespondent2() == NO
            || (caseData.getAddRespondent2() == YES && caseData.getRespondent2SameLegalRepresentative() == YES));

    public static final Predicate<CaseData> claimSubmittedTwoRegisteredRespondentRepresentatives = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getAddRespondent2() == YES
            && caseData.getRespondent2SameLegalRepresentative() == NO
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent2Represented() == YES
            && caseData.getRespondent1OrgRegistered() == YES
            && caseData.getRespondent2OrgRegistered() == YES;

    public static final Predicate<CaseData> claimSubmittedTwoRespondentRepresentativesOneUnregistered = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getAddRespondent2() == YES
            && caseData.getRespondent2SameLegalRepresentative() == NO
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent2Represented() == YES
            && ((caseData.getRespondent1OrgRegistered() == YES && caseData.getRespondent2OrgRegistered() == NO)
            || (caseData.getRespondent2OrgRegistered() == YES && caseData.getRespondent1OrgRegistered() == NO));

    public static final Predicate<CaseData> claimSubmitted1v1RespondentOneUnregistered = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getAddRespondent2() == NO
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent1OrgRegistered() == NO;

    // have to use this for now because cannot use featureToggleService.isNoticeOfChangeEnabled() as predicate
    public static final Predicate<CaseData> noticeOfChangeEnabled = caseData ->
        (caseData.getDefendant1LIPAtClaimIssued() != null
            && caseData.getDefendant1LIPAtClaimIssued() == YES)
            ||
            (caseData.getDefendant2LIPAtClaimIssued() != null
                && caseData.getDefendant2LIPAtClaimIssued() == YES);
    // certificateOfServiceEnabled predicate will be removed when CoS go live.
    public static final Predicate<CaseData> certificateOfServiceEnabled = caseData ->
        (caseData.getDefendant1LIPAtClaimIssued() != null
        && caseData.getDefendant1LIPAtClaimIssued() == YES)
            ||
            (caseData.getDefendant2LIPAtClaimIssued() != null
            && caseData.getDefendant2LIPAtClaimIssued() == YES);

    public static final Predicate<CaseData> claimSubmittedBothRespondentUnrepresented = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getAddRespondent2() == YES
            && caseData.getRespondent1Represented() == NO
            && caseData.getRespondent2Represented() == NO;

    public static final Predicate<CaseData> claimSubmittedOnlyOneRespondentRepresented = caseData ->
        caseData.getSubmittedDate() != null
            && (
            (caseData.getRespondent1Represented() == YES
                && caseData.getAddRespondent2() == YES
                && caseData.getRespondent2Represented() == NO)
                ||
                (caseData.getRespondent1Represented() == NO
                    && caseData.getAddRespondent2() == YES
                    && caseData.getRespondent2Represented() == YES)
        );

    public static final Predicate<CaseData> claimSubmittedOneUnrepresentedDefendantOnly = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getRespondent1Represented() == NO
            && caseData.getAddRespondent2() != YES;

    public static final Predicate<CaseData> claimSubmittedRespondent1Unrepresented = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getRespondent1Represented() == NO;

    public static final Predicate<CaseData> claimSubmittedRespondent2Unrepresented = caseData ->
        caseData.getSubmittedDate() != null
            && caseData.getAddRespondent2() == YES
            && caseData.getRespondent2Represented() == NO;

    public static final Predicate<CaseData> claimSubmittedBothUnregisteredSolicitors = caseData ->
            caseData.getSubmittedDate() != null
                    && caseData.getRespondent1OrgRegistered() == NO
                    && (caseData.getAddRespondent2() == YES && caseData.getRespondent2OrgRegistered() == NO
                    && (caseData.getRespondent2SameLegalRepresentative() == NO
                    || caseData.getRespondent2SameLegalRepresentative() == null));

    public static final Predicate<CaseData> respondent1NotRepresented = caseData ->
        caseData.getIssueDate() != null && caseData.getRespondent1Represented() == NO;

    public static final Predicate<CaseData> respondent1OrgNotRegistered = caseData ->
        caseData.getIssueDate() != null
            && caseData.getRespondent1OrgRegistered() == NO
            && caseData.getRespondent1Represented() == YES;

    public static final Predicate<CaseData> respondent2NotRepresented = caseData ->
        caseData.getIssueDate() != null && caseData.getRespondent2Represented() == NO;

    public static final Predicate<CaseData> respondent2OrgNotRegistered = caseData ->
        caseData.getIssueDate() != null
            && caseData.getRespondent2Represented() == YES
            && caseData.getRespondent2OrgRegistered() != YES;

    public static final Predicate<CaseData> paymentFailed = caseData ->
        !caseData.isApplicantNotRepresented()
            && (caseData.getPaymentSuccessfulDate() == null
            && (caseData.getPaymentDetails() != null
            && caseData.getPaymentDetails().getStatus() == FAILED)
            || (caseData.getClaimIssuedPaymentDetails() != null
            && caseData.getClaimIssuedPaymentDetails().getStatus() == FAILED));

    public static final Predicate<CaseData> paymentSuccessful = caseData ->
        !caseData.isApplicantNotRepresented()
            && (caseData.getPaymentSuccessfulDate() != null
            || (caseData.getClaimIssuedPaymentDetails() != null
            && caseData.getClaimIssuedPaymentDetails().getStatus() == SUCCESS));

    public static final Predicate<CaseData> pendingClaimIssued = caseData ->
        caseData.getIssueDate() != null
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent1OrgRegistered() == YES
            && (caseData.getRespondent2() == null
            || (caseData.getRespondent2Represented() == YES
            && (caseData.getRespondent2OrgRegistered() == YES
            || caseData.getRespondent2SameLegalRepresentative() == YES)));

    public static final Predicate<CaseData> bothDefSameLegalRep = caseData ->
        caseData.getRespondent2SameLegalRepresentative() == YES;

    public static final Predicate<CaseData> claimNotified = caseData ->
        !SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getClaimNotificationDate() != null
            && (caseData.getDefendantSolicitorNotifyClaimOptions() == null
            || Objects.equals(caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel(), "Both"));

    public static final Predicate<CaseData> takenOfflineAfterClaimNotified = caseData ->
        caseData.getClaimNotificationDate() != null
            && caseData.getDefendantSolicitorNotifyClaimOptions() != null
            && !Objects.equals(caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel(), "Both");

    public static final Predicate<CaseData> claimIssued = caseData ->
        caseData.getClaimNotificationDeadline() != null;

    public static final Predicate<CaseData> claimIssueBilingual = CaseDataParent::isClaimantBilingual;

    public static final Predicate<CaseData> claimDetailsNotifiedTimeExtension = caseData ->
        caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null;

    public static final Predicate<CaseData> claimDetailsNotified = caseData ->
        !SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getClaimDetailsNotificationDate() != null
            && (caseData.getDefendantSolicitorNotifyClaimDetailsOptions() == null
            || hasNotifiedClaimDetailsToBoth.test(caseData));

    public static final Predicate<CaseData> takenOfflineAfterClaimDetailsNotified = caseData ->
        caseData.getClaimDetailsNotificationDate() != null
            && caseData.getDefendantSolicitorNotifyClaimDetailsOptions() != null
            && hasNotifiedClaimDetailsToBoth.negate().test(caseData);

    public static final Predicate<CaseData> notificationAcknowledged = FlowPredicate::getPredicateForNotificationAcknowledged;

    public static final Predicate<CaseData> claimIssueHwF = CaseData::isHelpWithFees;

    private static boolean getPredicateForNotificationAcknowledged(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP:
                return (caseData.getRespondent1AcknowledgeNotificationDate() != null
                    || caseData.getRespondent2AcknowledgeNotificationDate() != null);
            default:
                return caseData.getRespondent1AcknowledgeNotificationDate() != null;
        }
    }

    public static final Predicate<CaseData> respondentTimeExtension = FlowPredicate::getPredicateForTimeExtension;

    private static boolean getPredicateForTimeExtension(CaseData caseData) {
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP) {
            return caseData.getRespondent1TimeExtensionDate() != null
                || caseData.getRespondent2TimeExtensionDate() != null;
        }
        return caseData.getRespondent1TimeExtensionDate() != null;
    }

    public static final Predicate<CaseData> fullDefence = caseData ->
        getPredicateForResponseType(caseData, FULL_DEFENCE);

    private static boolean getPredicateForResponseType(CaseData caseData, RespondentResponseType responseType) {
        boolean basePredicate = caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1ClaimResponseType() == responseType;
        boolean predicate = false;
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP:
                predicate = basePredicate && (caseData.getRespondentResponseIsSame() == YES
                    || caseData.getRespondent2ClaimResponseType() == responseType);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                predicate = basePredicate && caseData.getRespondent2ClaimResponseType() == responseType;
                break;
            case ONE_V_ONE:
                predicate = basePredicate;
                break;
            case TWO_V_ONE:
                predicate = basePredicate && caseData.getRespondent1ClaimResponseTypeToApplicant2() == responseType;
                break;
            default:
                break;
        }
        return predicate;
    }

    public static final Predicate<CaseData> divergentRespondWithDQAndGoOffline = FlowPredicate::isDivergentResponsesWithDQAndGoOffline;

    private static boolean isDivergentResponsesWithDQAndGoOffline(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP:
                //scenario: either of them have submitted full defence response
                return !caseData.getRespondent1ClaimResponseType().equals(caseData.getRespondent2ClaimResponseType())
                    && (caseData.getRespondent1ClaimResponseType().equals(FULL_DEFENCE)
                    || caseData.getRespondent2ClaimResponseType().equals(FULL_DEFENCE));
            case ONE_V_TWO_TWO_LEGAL_REP:
                //scenario: latest response is full defence
                return !Objects.equals(
                    caseData.getRespondent1ClaimResponseType(),
                    caseData.getRespondent2ClaimResponseType()
                )
                    && ((caseData.getRespondent2ClaimResponseType().equals(FULL_DEFENCE)
                    && caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()))
                    || (caseData.getRespondent1ClaimResponseType().equals(FULL_DEFENCE)
                    && caseData.getRespondent1ResponseDate().isAfter(caseData.getRespondent2ResponseDate())));
            case TWO_V_ONE:
                return (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
                    || FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeToApplicant2()))
                    && !(FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
                    && FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeToApplicant2()));
            default:
                return false;
        }
    }

    public static final Predicate<CaseData> divergentRespondGoOffline = FlowPredicate::isDivergentResponsesGoOffline;

    private static boolean isDivergentResponsesGoOffline(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                return !Objects.equals(
                    caseData.getRespondent1ClaimResponseType(),
                    caseData.getRespondent2ClaimResponseType()
                )
                    //scenario: latest response is not full defence
                    && ((!caseData.getRespondent2ClaimResponseType().equals(FULL_DEFENCE)
                    && caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate())
                    || !caseData.getRespondent1ClaimResponseType().equals(FULL_DEFENCE)
                    && caseData.getRespondent1ResponseDate().isAfter(caseData.getRespondent2ResponseDate()))
                    //scenario: neither responses are full defence
                    || (!caseData.getRespondent1ClaimResponseType().equals(FULL_DEFENCE)
                    && !caseData.getRespondent2ClaimResponseType().equals(FULL_DEFENCE)));
            case ONE_V_TWO_ONE_LEGAL_REP:
                return !caseData.getRespondent1ClaimResponseType().equals(caseData.getRespondent2ClaimResponseType())
                    && (!caseData.getRespondent1ClaimResponseType().equals(FULL_DEFENCE)
                    && !caseData.getRespondent2ClaimResponseType().equals(FULL_DEFENCE));
            case TWO_V_ONE:
                return !(FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType()) || FULL_DEFENCE
                    .equals(caseData.getRespondent1ClaimResponseTypeToApplicant2()));
            default:
                return false;
        }
    }

    public static final Predicate<CaseData> allResponsesReceived = FlowPredicate::getPredicateForResponses;

    private static boolean getPredicateForResponses(CaseData caseData) {
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP) {
            return caseData.getRespondent1ResponseDate() != null && caseData.getRespondent2ResponseDate() != null;
        }
        return caseData.getRespondent1ResponseDate() != null;
    }

    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceived = FlowPredicate::getPredicateForAwaitingResponsesFullDefenceReceived;

    private static boolean getPredicateForAwaitingResponsesFullDefenceReceived(CaseData caseData) {
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP) {
            return (caseData.getRespondent1ClaimResponseType() != null
                && caseData.getRespondent2ClaimResponseType() == null
                && FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType()))
                ||
                (caseData.getRespondent1ClaimResponseType() == null
                    && caseData.getRespondent2ClaimResponseType() != null
                    && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType()));
        }
        return false;
    }

    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceReceived
        = FlowPredicate::getPredicateForAwaitingResponsesNonFullDefenceReceived;

    private static boolean getPredicateForAwaitingResponsesNonFullDefenceReceived(CaseData caseData) {
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP) {
            return (caseData.getRespondent1ClaimResponseType() != null
                && caseData.getRespondent2ClaimResponseType() == null
                && !FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType()))
                ||
                (caseData.getRespondent1ClaimResponseType() == null
                    && caseData.getRespondent2ClaimResponseType() != null
                    && !FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType()));
        }
        return false;
    }

    public static final Predicate<CaseData> fullAdmission = caseData ->
        getPredicateForResponseType(caseData, FULL_ADMISSION);

    public static final Predicate<CaseData> partAdmission = caseData ->
        getPredicateForResponseType(caseData, PART_ADMISSION);

    public static final Predicate<CaseData> counterClaim = caseData ->
        getPredicateForResponseType(caseData, COUNTER_CLAIM);

    public static final Predicate<CaseData> fullDefenceProceed = FlowPredicate::getPredicateForClaimantIntentionProceed;

    public static final Predicate<CaseData> lipFullDefenceProceed = FlowPredicate::getPredicateForLipClaimantIntentionProceed;

    public static final Predicate<CaseData> fullAdmitPayImmediately = FlowPredicate::getPredicateForPayImmediately;

    public static final Predicate<CaseData> isCarmApplicableLipCase = FlowPredicate::getPredicateIfLipCaseCarmApplicable;

    public static final Predicate<CaseData> takenOfflineSDONotDrawn = caseData ->
        caseData.getReasonNotSuitableSDO() != null
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
            && caseData.getTakenOfflineDate() != null
            && caseData.getTakenOfflineByStaffDate() == null;

    public static final Predicate<CaseData> specSmallClaimCarm = caseData ->
        isSpecSmallClaim(caseData) && getCarmEnabledForDate(caseData);

    private static boolean getPredicateIfLipCaseCarmApplicable(CaseData caseData) {
        boolean basePredicate = getCarmEnabledForDate(caseData) && isSpecSmallClaim(caseData)
            && caseData.getRespondent2() == null;
        if (basePredicate) {
            basePredicate = NO.equals(caseData.getApplicant1Represented())
                || NO.equals(caseData.getRespondent1Represented());
        }
        return basePredicate;
    }

    private static boolean isSpecSmallClaim(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack());
    }

    private static boolean getCarmEnabledForDate(CaseData caseData) {
        // Date of go live is 1st August, as we use "isAfter" we compare with 31st July
        return caseData.getSubmittedDate().toLocalDate().isAfter(LocalDate.of(2024, 7, 31));
    }

    public static final Predicate<CaseData> takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension =
        FlowPredicate::getPredicateTakenOfflineSDONotDrawnAfterNotificationAckTimeExt;

    private static boolean getPredicateTakenOfflineSDONotDrawnAfterNotificationAckTimeExt(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> (caseData.getReasonNotSuitableSDO() != null
                && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
                && caseData.getTakenOfflineDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() != null
                && caseData.getRespondent1ResponseDate() == null
                && caseData.getRespondent2AcknowledgeNotificationDate() != null
                && caseData.getRespondent2TimeExtensionDate() != null
                && caseData.getRespondent2ResponseDate() == null);
            default -> (caseData.getReasonNotSuitableSDO() != null
                && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
                && caseData.getTakenOfflineDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() != null
                && caseData.getRespondent1ResponseDate() == null);
        };
    }

    public static final Predicate<CaseData> takenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension = caseData ->
        caseData.getReasonNotSuitableSDO() != null
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
            && caseData.getTakenOfflineDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1ResponseDate() == null;

    public static final Predicate<CaseData> takenOfflineSDONotDrawnAfterNotificationAcknowledged =
        FlowPredicate::getPredicateTakenOfflineSDONotDrawnAfterNotificationAcknowledged;

    private static boolean getPredicateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> (caseData.getReasonNotSuitableSDO() != null
                && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
                && caseData.getTakenOfflineDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1ResponseDate() == null
                && caseData.getRespondent2AcknowledgeNotificationDate() != null
                && caseData.getRespondent2TimeExtensionDate() == null
                && caseData.getRespondent2ResponseDate() == null);
            default -> (caseData.getReasonNotSuitableSDO() != null
                && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
                && caseData.getTakenOfflineDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1ResponseDate() == null);
        };
    }

    public static final Predicate<CaseData> takenOfflineSDONotDrawnAfterClaimDetailsNotified =
        FlowPredicate::getPredicateTakenOfflineSDONotDrawnAfterClaimDetailsNotified;

    private static boolean getPredicateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> (caseData.getReasonNotSuitableSDO() != null
                && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
                && caseData.getTakenOfflineDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() == null
                && caseData.getRespondent1ResponseDate() == null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent2ResponseDate() == null
                && caseData.getRespondent2AcknowledgeNotificationDate() == null
                && caseData.getRespondent2TimeExtensionDate() == null
                && caseData.getClaimDismissedDate() == null);
            default -> (caseData.getReasonNotSuitableSDO() != null
                && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
                && caseData.getTakenOfflineDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() == null
                && caseData.getRespondent1ResponseDate() == null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getClaimDismissedDate() == null);
        };
    }

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

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimantResponseBeforeSDO = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
        && caseData.getApplicant1ResponseDate() != null
        && caseData.getDrawDirectionsOrderRequired() == null
        && caseData.getReasonNotSuitableSDO() == null;

    public static final Predicate<CaseData> takenOfflineByStaffAfterSDO = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getDrawDirectionsOrderRequired() != null
            && caseData.getReasonNotSuitableSDO() == null;

    public static final Predicate<CaseData> takenOfflineAfterNotSuitableForSdo = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getDrawDirectionsOrderRequired() == null
            && caseData.getReasonNotSuitableSDO() != null
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput());

    public static final Predicate<CaseData> takenOfflineByStaffAfterDefendantResponse = FlowPredicate::getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse;

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimIssue = FlowPredicate::getPredicateTakenOfflineByStaffAfterClaimIssue;

    public static final Predicate<CaseData> takenOfflineByStaffBeforeClaimIssued = FlowPredicate::getPredicateTakenOfflineByStaffBeforeClaimIssue;

    public static final boolean getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse(CaseData caseData) {
        boolean basePredicate = caseData.getTakenOfflineByStaffDate() != null
            && caseData.getApplicant1ResponseDate() == null;

        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && YES.equals(caseData.getAddApplicant2())) {
            return basePredicate && caseData.getApplicant2ResponseDate() == null;
        }

        return basePredicate;
    }

    public static final boolean getPredicateTakenOfflineByStaffBeforeClaimIssue(CaseData caseData) {
        // In case of SPEC and UNSPEC claim ClaimNotificationDeadline will be set when the case is issued
        return caseData.getTakenOfflineByStaffDate() != null
            && caseData.getClaimNotificationDeadline() == null
            && caseData.getClaimNotificationDate() == null
            && caseData.getSubmittedDate() != null;
    }

    public static final boolean getPredicateTakenOfflineByStaffAfterClaimIssue(CaseData caseData) {
        // In case of SPEC claim ClaimNotificationDate will be set even when the case is issued
        // In case of UNSPEC ClaimNotificationDate will be set only after notification step
        boolean basePredicate = caseData.getTakenOfflineByStaffDate() != null
            && caseData.getClaimDetailsNotificationDate() == null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getClaimNotificationDeadline() != null
            && caseData.getClaimNotificationDeadline().isAfter(LocalDateTime.now());

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return basePredicate && caseData.getClaimNotificationDate() != null;
        }

        return basePredicate && caseData.getClaimNotificationDate() == null;
    }

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimNotified = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getClaimDetailsNotificationDate() == null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getClaimDetailsNotificationDeadline() != null
            && caseData.getClaimDetailsNotificationDeadline().isAfter(LocalDateTime.now());

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimDetailsNotified = FlowPredicate::getPredicateTakenOfflineByStaffAfterClaimDetailsNotified;

    public static final boolean getPredicateTakenOfflineByStaffAfterClaimDetailsNotified(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP:
                return (caseData.getTakenOfflineByStaffDate() != null
                    && caseData.getRespondent1AcknowledgeNotificationDate() == null
                    && caseData.getRespondent1ResponseDate() == null
                    && caseData.getRespondent1TimeExtensionDate() == null
                    && caseData.getRespondent2ResponseDate() == null
                    && caseData.getRespondent2AcknowledgeNotificationDate() == null
                    && caseData.getRespondent2TimeExtensionDate() == null
                    && caseData.getClaimDismissedDate() == null);
            default:
                return (caseData.getTakenOfflineByStaffDate() != null
                    && caseData.getRespondent1AcknowledgeNotificationDate() == null
                    && caseData.getRespondent1ResponseDate() == null
                    && caseData.getRespondent1TimeExtensionDate() == null
                    && caseData.getClaimDismissedDate() == null);
        }
    }

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimDetailsNotifiedExtension = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1ResponseDate() == null;

    public static final Predicate<CaseData> takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension
        = FlowPredicate::getPredicateTakenOfflineByStaffAfterNotificationAckTimeExt;

    public static final boolean getPredicateTakenOfflineByStaffAfterNotificationAckTimeExt(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> (caseData.getTakenOfflineByStaffDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() != null
                && caseData.getRespondent2AcknowledgeNotificationDate() != null
                && caseData.getRespondent2TimeExtensionDate() != null);
            default -> (caseData.getTakenOfflineByStaffDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() != null);
        };
    }

    public static final Predicate<CaseData> takenOfflineByStaffAfterNotificationAcknowledged = FlowPredicate::getPredicateTakenOfflineByStaffAfterNotificationAcknowledged;

    public static final boolean getPredicateTakenOfflineByStaffAfterNotificationAcknowledged(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> (caseData.getTakenOfflineByStaffDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1ResponseDate() == null
                && caseData.getRespondent2AcknowledgeNotificationDate() != null
                && caseData.getRespondent2TimeExtensionDate() == null
                && caseData.getRespondent2ResponseDate() == null);
            default -> (caseData.getTakenOfflineByStaffDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1ResponseDate() == null);
        };
    }

    public static final Predicate<CaseData> caseDismissedAfterDetailNotified = FlowPredicate::getPredicateForCaseDismissedAfterDetailNotified;

    private static boolean getPredicateForCaseDismissedAfterDetailNotified(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP ->
                caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
                    && caseData.getRespondent1AcknowledgeNotificationDate() == null
                    && caseData.getRespondent1TimeExtensionDate() == null
                    && caseData.getRespondent1ClaimResponseIntentionType() == null
                    && caseData.getRespondent2AcknowledgeNotificationDate() == null
                    && caseData.getRespondent2TimeExtensionDate() == null
                    && caseData.getRespondent2ClaimResponseIntentionType() == null
                    && caseData.getRespondent1ResponseDate() == null
                    && caseData.getRespondent2ResponseDate() == null
                    && caseData.getTakenOfflineByStaffDate() == null;
            default -> caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
                && caseData.getRespondent1AcknowledgeNotificationDate() == null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1ClaimResponseIntentionType() == null
                && caseData.getRespondent1ResponseDate() == null
                && caseData.getTakenOfflineByStaffDate() == null;
        };
    }

    public static final Predicate<CaseData> caseDismissedAfterDetailNotifiedExtension = caseData ->
        caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
            && ((caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() != null)
            || (caseData.getRespondent2AcknowledgeNotificationDate() == null
            && caseData.getRespondent2TimeExtensionDate() != null))
            && caseData.getRespondent1ClaimResponseIntentionType() == null
            && caseData.getRespondent2ClaimResponseIntentionType() == null;

    public static final Predicate<CaseData> caseDismissedAfterClaimAcknowledged = caseData -> {
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP) {
            return caseData.getClaimDismissedDeadline().isBefore(java.time.LocalDateTime.now())
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent2TimeExtensionDate() == null
                && caseData.getRespondent2AcknowledgeNotificationDate() != null
                && (caseData.getRespondent1ResponseDate() == null || caseData.getRespondent2ResponseDate() == null);
        }
        return caseData.getClaimDismissedDeadline().isBefore(java.time.LocalDateTime.now())
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null
            && caseData.getRespondent1ResponseDate() == null;
    };

    public static final Predicate<CaseData> caseDismissedAfterClaimAcknowledgedExtension = caseData -> {
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP) {
            return caseData.getClaimDismissedDeadline().isBefore(java.time.LocalDateTime.now())
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent2AcknowledgeNotificationDate() != null
                && (caseData.getRespondent1TimeExtensionDate() != null
                || caseData.getRespondent2TimeExtensionDate() != null)
                && caseData.getReasonNotSuitableSDO() == null
                && caseData.getTakenOfflineByStaffDate() == null;
        }
        return caseData.getClaimDismissedDeadline().isBefore(java.time.LocalDateTime.now())
            && caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null
            && caseData.getReasonNotSuitableSDO() == null
            && caseData.getTakenOfflineByStaffDate() == null;
    };

    public static final Predicate<CaseData> applicantOutOfTime = caseData ->
        caseData.getApplicant1ResponseDeadline() != null
            && caseData.getApplicant1ResponseDeadline().isBefore(LocalDateTime.now())
            && caseData.getApplicant1ResponseDate() == null;

    public static final Predicate<CaseData> claimDismissalOutOfTime = caseData ->
        caseData.getClaimDismissedDeadline() != null
            && caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now());

    public static final Predicate<CaseData> demageMultiClaim = caseData ->
        AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())
        && CaseCategory.UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory());

    public static final Predicate<CaseData> applicantOutOfTimeProcessedByCamunda = caseData ->
        caseData.getTakenOfflineDate() != null;

    public static final Predicate<CaseData> pastClaimNotificationDeadline = caseData ->
        caseData.getClaimNotificationDeadline() != null
            && caseData.getClaimNotificationDeadline().isBefore(LocalDateTime.now())
            && caseData.getClaimNotificationDate() == null;

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

        boolean basePredicate = caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() == responseType;
        boolean predicate = false;

        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return false;
        }

        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP:
                predicate = basePredicate && (caseData.getRespondentResponseIsSame() == YES
                    || caseData.getRespondent2ClaimResponseTypeForSpec() == responseType);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                predicate = basePredicate
                    && caseData.getRespondent2ClaimResponseTypeForSpec() == responseType
                    // for the time being, even if the response is the same, 1v2ds only deals with full defence
                    && responseType == RespondentResponseTypeSpec.FULL_DEFENCE;
                break;
            case ONE_V_ONE:
                predicate = basePredicate;
                break;
            case TWO_V_ONE:
                if (YES.equals(caseData.getDefendantSingleResponseToBothClaimants())) {
                    predicate = responseType.equals(caseData.getRespondent1ClaimResponseTypeForSpec());
                } else {
                    predicate = responseType.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                        && responseType.equals(caseData.getClaimant2ClaimResponseTypeForSpec());
                }
                break;
            default:
                break;
        }
        return predicate;
    }

    public static final Predicate<CaseData> divergentRespondWithDQAndGoOfflineSpec = FlowPredicate::isDivergentResponsesWithDQAndGoOfflineSpec;

    private static boolean isDivergentResponsesWithDQAndGoOfflineSpec(CaseData caseData) {

        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return false;
        }

        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP ->
                //scenario: only one of them have submitted full defence response
                caseData.getRespondent1ClaimResponseTypeForSpec() != null
                    && !caseData.getRespondent1ClaimResponseTypeForSpec()
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                    && NO.equals(caseData.getRespondentResponseIsSame())
                    && (RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    || RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
            case ONE_V_TWO_TWO_LEGAL_REP ->
                //scenario: latest response is full defence
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

        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return false;
        }

        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                // 1v2 diff sol DQ is always created for both defendants
                return false;
            case ONE_V_TWO_ONE_LEGAL_REP:
                return caseData.getRespondent1ClaimResponseTypeForSpec() != null
                    && !caseData.getRespondent1ClaimResponseTypeForSpec()
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                    && caseData.getRespondentResponseIsSame() != YES
                    && (!RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    && !RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
            case TWO_V_ONE:
                return (!RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                    && !RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))
                    && (caseData.getClaimant1ClaimResponseTypeForSpec() != null
                    && caseData.getClaimant2ClaimResponseTypeForSpec() != null)
                    && !caseData.getClaimant1ClaimResponseTypeForSpec()
                    .equals(caseData.getClaimant2ClaimResponseTypeForSpec());
            default:
                return false;
        }
    }

    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceivedSpec = FlowPredicate::getPredicateForAwaitingResponsesFullDefenceReceivedSpec;

    private static boolean getPredicateForAwaitingResponsesFullDefenceReceivedSpec(CaseData caseData) {
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP) {
            return (caseData.getRespondent1ClaimResponseTypeForSpec() != null
                && caseData.getRespondent2ClaimResponseTypeForSpec() == null
                && RespondentResponseTypeSpec.FULL_DEFENCE
                .equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
                ||
                (caseData.getRespondent1ClaimResponseTypeForSpec() == null
                    && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                    && RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
        }
        return false;
    }

    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceReceivedSpec = FlowPredicate::getPredicateForAwaitingResponsesNonFullDefenceReceivedSpec;

    private static boolean getPredicateForAwaitingResponsesNonFullDefenceReceivedSpec(CaseData caseData) {
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP) {
            return (caseData.getRespondent1ClaimResponseTypeForSpec() != null
                && caseData.getRespondent2ClaimResponseTypeForSpec() == null
                && !RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
                ||
                (caseData.getRespondent1ClaimResponseTypeForSpec() == null
                    && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                    && !RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
        }
        return false;
    }

    public static final Predicate<CaseData> specClaim = caseData ->
        SPEC_CLAIM.equals(caseData.getCaseAccessCategory());

    private static boolean getPredicateForClaimantIntentionProceed(CaseData caseData) {
        boolean predicate = false;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP, ONE_V_ONE ->
                    predicate = YES.equals(caseData.getApplicant1ProceedWithClaim());
                case TWO_V_ONE -> predicate = YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1());
                default -> {
                }
            }
        } else {
            switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    predicate = YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        || YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2());
                case ONE_V_ONE -> predicate = YES.equals(caseData.getApplicant1ProceedWithClaim());
                case TWO_V_ONE -> predicate = YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                    || YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1());
                default -> {
                }
            }
        }
        return predicate;
    }

    private static boolean getPredicateForLipClaimantIntentionProceed(CaseData caseData) {
        boolean predicate = false;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            predicate = NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim());
        }
        return predicate;
    }

    private static boolean getPredicateForPayImmediately(CaseData caseData) {
        boolean predicate = false;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && getMultiPartyScenario(caseData) == ONE_V_ONE) {
            predicate = null != caseData.getWhenToBePaidText()
                &&  null == caseData.getApplicant1ProceedWithClaim();
        }
        return predicate;
    }

    private static boolean getPredicateForClaimantIntentionNotProceed(CaseData caseData) {
        boolean predicate = false;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP, ONE_V_ONE:
                    predicate = NO.equals(caseData.getApplicant1ProceedWithClaim());
                    break;
                case TWO_V_ONE:
                    predicate = NO.equals(caseData.getApplicant1ProceedWithClaimSpec2v1());
                    break;
                default:
                    break;
            }
        } else {
            switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP:
                    predicate = NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        && NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2());
                    break;
                case ONE_V_ONE:
                    predicate = NO.equals(caseData.getApplicant1ProceedWithClaim());
                    break;
                case TWO_V_ONE:
                    predicate = NO.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                        && NO.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1());
                    break;
                default:
                    break;
            }
        }

        return predicate;
    }

    public static final Predicate<CaseData> oneVsOneCase = FlowPredicate::getPredicateFor1v1Case;

    private static boolean getPredicateFor1v1Case(CaseData caseData) {
        return ONE_V_ONE.equals(getMultiPartyScenario(caseData));
    }

    public static final Predicate<CaseData> multipartyCase = FlowPredicate::getPredicateForMultipartyCase;

    private static boolean getPredicateForMultipartyCase(CaseData caseData) {
        return isMultiPartyScenario(caseData);
    }

    public static final Predicate<CaseData> pinInPostEnabledAndLiP = caseData ->
        caseData.getRespondent1PinToPostLRspec() != null;

    public static final Predicate<CaseData> allAgreedToLrMediationSpec = caseData -> {
        boolean result = false;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack())
            && caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES) {
            if (caseData.getRespondent2() != null
                && caseData.getRespondent2SameLegalRepresentative().equals(NO)
                && caseData.getResponseClaimMediationSpec2Required() == YesOrNo.NO) {
                result = false;
            } else if (Optional.ofNullable(caseData.getApplicant1ClaimMediationSpecRequired())
                .map(SmallClaimMedicalLRspec::getHasAgreedFreeMediation)
                .filter(YesOrNo.NO::equals).isPresent()
                || Optional.ofNullable(caseData.getApplicantMPClaimMediationSpecRequired())
                .map(SmallClaimMedicalLRspec::getHasAgreedFreeMediation)
                .filter(YesOrNo.NO::equals).isPresent() || caseData.hasClaimantAgreedToFreeMediation()) {
                result = false;
            } else {
                result = true;
            }
        }
        return result;
    };

    public static final Predicate<CaseData> contactDetailsChange = caseData ->
        NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired());

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

    public static final Predicate<CaseData> agreePartAdmitSettle =
        CaseData::isPartAdmitClaimSettled;

    public static final Predicate<CaseData> isClaimantNotSettlePartAdmitClaim =
        CaseData::isClaimantNotSettlePartAdmitClaim;

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

    public static final Predicate<CaseData> isPayImmediately = CaseData::isPayImmediately;

    public static final Predicate<CaseData> reasonNotSuitableForSdo = caseData ->
        Objects.nonNull(caseData.getReasonNotSuitableSDO())
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput());

    public static final Predicate<CaseData> casemanMarksMediationUnsuccessful = caseData ->
        Objects.nonNull(caseData.getMediation().getUnsuccessfulMediationReason())
        || (Objects.nonNull(caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect())
            && !caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect().isEmpty());

    public static final Predicate<CaseData> takenOfflineByStaffBeforeMediationUnsuccessful = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
        && (Objects.isNull(caseData.getMediation().getUnsuccessfulMediationReason())
            && Objects.isNull(caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect()));
}
