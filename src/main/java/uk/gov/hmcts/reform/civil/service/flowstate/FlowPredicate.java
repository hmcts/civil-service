package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseCategoryUtils.isSpecCaseCategory;

public class FlowPredicate {

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

    // have to use this for now because cannot use featureToggleService.isNoticeOfChangeEnabled() as predicate
    public static final Predicate<CaseData> noticeOfChangeEnabledAndLiP = caseData ->
        caseData.getAddLegalRepDeadline() != null;

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
            && (caseData.getAddRespondent2() == YES && caseData.getRespondent2OrgRegistered() == NO);

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
        caseData.getPaymentSuccessfulDate() == null
            && (caseData.getPaymentDetails() != null
            && caseData.getPaymentDetails().getStatus() == FAILED)
            || (caseData.getClaimIssuedPaymentDetails() != null
            && caseData.getClaimIssuedPaymentDetails().getStatus() == FAILED);

    public static final Predicate<CaseData> paymentSuccessful = caseData ->
        caseData.getPaymentSuccessfulDate() != null;

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
        !isSpecCaseCategory(caseData, caseData.getCaseAccessCategory() != null)
            && caseData.getClaimNotificationDate() != null
            && (caseData.getDefendantSolicitorNotifyClaimOptions() == null
            || Objects.equals(caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel(), "Both"));

    public static final Predicate<CaseData> takenOfflineAfterClaimNotified = caseData ->
        caseData.getClaimNotificationDate() != null
            && caseData.getDefendantSolicitorNotifyClaimOptions() != null
            && !Objects.equals(caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel(), "Both");

    public static final Predicate<CaseData> claimIssued = caseData ->
        caseData.getClaimNotificationDeadline() != null;

    public static final Predicate<CaseData> claimDetailsNotifiedTimeExtension = caseData ->
        caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null;

    public static final Predicate<CaseData> claimDetailsNotified = caseData ->
        !isSpecCaseCategory(caseData, caseData.getCaseAccessCategory() != null)
            && caseData.getClaimDetailsNotificationDate() != null
            && (caseData.getDefendantSolicitorNotifyClaimDetailsOptions() == null
            || hasNotifiedClaimDetailsToBoth.test(caseData));

    public static final Predicate<CaseData> takenOfflineAfterClaimDetailsNotified = caseData ->
        caseData.getClaimDetailsNotificationDate() != null
            && caseData.getDefendantSolicitorNotifyClaimDetailsOptions() != null
            && hasNotifiedClaimDetailsToBoth.negate().test(caseData);

    public static final Predicate<CaseData> notificationAcknowledged = caseData ->
        getPredicateForNotificationAcknowledged(caseData);

    private static boolean getPredicateForNotificationAcknowledged(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
            case ONE_V_TWO_ONE_LEGAL_REP:
                return (caseData.getRespondent1AcknowledgeNotificationDate() != null
                    || caseData.getRespondent2AcknowledgeNotificationDate() != null);
            default:
                return caseData.getRespondent1AcknowledgeNotificationDate() != null;
        }
    }

    public static final Predicate<CaseData> respondentTimeExtension = caseData ->
        getPredicateForTimeExtension(caseData);

    private static boolean getPredicateForTimeExtension(CaseData caseData) {
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP) {
            return caseData.getRespondent1TimeExtensionDate() != null
                || caseData.getRespondent2TimeExtensionDate() != null;
        }
        return caseData.getRespondent1TimeExtensionDate() != null;
    }

    public static final Predicate<CaseData> notificationAcknowledgedTimeExtension = caseData ->
        caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null;

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

    public static final Predicate<CaseData> divergentRespondWithDQAndGoOffline = caseData ->
        isDivergentResponsesWithDQAndGoOffline(caseData);

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

    public static final Predicate<CaseData> divergentRespondGoOffline = caseData ->
        isDivergentResponsesGoOffline(caseData);

    private static boolean isDivergentResponsesGoOffline(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                return !Objects.equals(
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

    public static final Predicate<CaseData> allResponsesReceived = caseData ->
        getPredicateForResponses(caseData);

    private static boolean getPredicateForResponses(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                return caseData.getRespondent1ResponseDate() != null && caseData.getRespondent2ResponseDate() != null;
            default:
                return caseData.getRespondent1ResponseDate() != null;
        }
    }

    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceived = caseData ->
        getPredicateForAwaitingResponsesFullDefenceReceived(caseData);

    private static boolean getPredicateForAwaitingResponsesFullDefenceReceived(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                return (caseData.getRespondent1ClaimResponseType() != null
                    && caseData.getRespondent2ClaimResponseType() == null
                    && RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType()))
                    ||
                    (caseData.getRespondent1ClaimResponseType() == null
                        && caseData.getRespondent2ClaimResponseType() != null
                        && RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType()));
            default:
                return false;
        }
    }

    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceReceived = caseData ->
        getPredicateForAwaitingResponsesNonFullDefenceReceived(caseData);

    private static boolean getPredicateForAwaitingResponsesNonFullDefenceReceived(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                return (caseData.getRespondent1ClaimResponseType() != null
                    && caseData.getRespondent2ClaimResponseType() == null
                    && !RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType()))
                    ||
                    (caseData.getRespondent1ClaimResponseType() == null
                        && caseData.getRespondent2ClaimResponseType() != null
                        && !RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType()));
            default:
                return false;
        }
    }

    public static final Predicate<CaseData> fullDefenceAfterNotifyDetails = caseData ->
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ClaimResponseType() == FULL_DEFENCE;

    public static final Predicate<CaseData> fullDefenceAfterAcknowledge = caseData ->
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1ClaimResponseType() == FULL_DEFENCE;

    public static final Predicate<CaseData> fullAdmission = caseData ->
        getPredicateForResponseType(caseData, FULL_ADMISSION);

    public static final Predicate<CaseData> fullAdmissionAfterNotifyDetails = caseData ->
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ClaimResponseType() == FULL_ADMISSION;

    public static final Predicate<CaseData> fullAdmissionAfterAcknowledge = caseData ->
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1ClaimResponseType() == FULL_ADMISSION;

    public static final Predicate<CaseData> partAdmission = caseData ->
        getPredicateForResponseType(caseData, PART_ADMISSION);

    public static final Predicate<CaseData> partAdmissionAfterNotifyDetails = caseData ->
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ClaimResponseType() == PART_ADMISSION;

    public static final Predicate<CaseData> partAdmissionAfterAcknowledge = caseData ->
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1ClaimResponseType() == PART_ADMISSION;

    public static final Predicate<CaseData> counterClaim = caseData ->
        getPredicateForResponseType(caseData, COUNTER_CLAIM);

    public static final Predicate<CaseData> counterClaimAfterNotifyDetails = caseData ->
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ClaimResponseType() == COUNTER_CLAIM;

    public static final Predicate<CaseData> counterClaimAfterAcknowledge = caseData ->
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1ClaimResponseType() == COUNTER_CLAIM;

    public static final Predicate<CaseData> fullDefenceProceed = caseData ->
        getPredicateForClaimantIntentionProceed(caseData);

    public static final Predicate<CaseData> fullDefenceNotProceed = caseData ->
        getPredicateForClaimantIntentionNotProceed(caseData);

    public static final Predicate<CaseData> takenOfflineBySystem = caseData ->
        caseData.getTakenOfflineDate() != null;

    public static final Predicate<CaseData> takenOfflineByStaff = caseData ->
        caseData.getTakenOfflineByStaffDate() != null;

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimIssue = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getClaimNotificationDate() == null
            && caseData.getClaimDetailsNotificationDate() == null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getClaimNotificationDeadline() != null
            && caseData.getClaimNotificationDeadline().isAfter(LocalDateTime.now());

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimNotified = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getClaimDetailsNotificationDate() == null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getClaimDetailsNotificationDeadline() != null
            && caseData.getClaimDetailsNotificationDeadline().isAfter(LocalDateTime.now());

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimDetailsNotified = caseData ->
        getPredicateTakenOfflineByStaffAfterClaimDetailsNotified(caseData);

    public static final boolean getPredicateTakenOfflineByStaffAfterClaimDetailsNotified(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
            case ONE_V_TWO_ONE_LEGAL_REP:
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

    public static final Predicate<CaseData> takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension = caseData ->
        getPredicateTakenOfflineByStaffAfterNotificationAckTimeExt(caseData);

    public static final boolean getPredicateTakenOfflineByStaffAfterNotificationAckTimeExt(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
            case ONE_V_TWO_ONE_LEGAL_REP:
                return (caseData.getTakenOfflineByStaffDate() != null
                    && caseData.getRespondent1AcknowledgeNotificationDate() != null
                    && caseData.getRespondent1TimeExtensionDate() != null
                    && caseData.getRespondent1ResponseDate() == null
                    && caseData.getRespondent2AcknowledgeNotificationDate() != null
                    && caseData.getRespondent2TimeExtensionDate() != null
                    && caseData.getRespondent2ResponseDate() == null);
            default:
                return (caseData.getTakenOfflineByStaffDate() != null
                    && caseData.getRespondent1AcknowledgeNotificationDate() != null
                    && caseData.getRespondent1TimeExtensionDate() != null
                    && caseData.getRespondent1ResponseDate() == null);
        }
    }

    public static final Predicate<CaseData> takenOfflineByStaffAfterNotificationAcknowledged = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1ResponseDate() == null;

    public static final Predicate<CaseData> caseDismissedAfterDetailNotified = caseData ->
        caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1ClaimResponseIntentionType() == null
            && caseData.getRespondent2AcknowledgeNotificationDate() == null
            && caseData.getRespondent2TimeExtensionDate() == null
            && caseData.getRespondent2ClaimResponseIntentionType() == null;

    public static final Predicate<CaseData> caseDismissedAfterDetailNotifiedExtension = caseData ->
        caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
            && ((caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() != null)
            || (caseData.getRespondent2AcknowledgeNotificationDate() == null
            && caseData.getRespondent2TimeExtensionDate() != null))
            && caseData.getRespondent1ClaimResponseIntentionType() == null
            && caseData.getRespondent2ClaimResponseIntentionType() == null;

    public static final Predicate<CaseData> caseDismissedAfterClaimAcknowledged = caseData -> {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                return caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
                    && caseData.getRespondent1TimeExtensionDate() == null
                    && caseData.getRespondent1AcknowledgeNotificationDate() != null
                    && caseData.getRespondent2TimeExtensionDate() == null
                    && caseData.getRespondent2AcknowledgeNotificationDate() != null;

            default:
                return caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
                    && caseData.getRespondent1TimeExtensionDate() == null
                    && caseData.getRespondent1AcknowledgeNotificationDate() != null;
        }
    };

    public static final Predicate<CaseData> caseDismissedAfterClaimAcknowledgedExtension = caseData -> {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                return caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
                    && caseData.getRespondent1AcknowledgeNotificationDate() != null
                    && caseData.getRespondent2AcknowledgeNotificationDate() != null
                    && (caseData.getRespondent1TimeExtensionDate() != null
                    || caseData.getRespondent2TimeExtensionDate() != null);
            default:
                return caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
                    && caseData.getRespondent1TimeExtensionDate() != null
                    && caseData.getRespondent1AcknowledgeNotificationDate() != null;
        }
    };

    public static final Predicate<CaseData> applicantOutOfTime = caseData ->
        caseData.getApplicant1ResponseDeadline() != null
            && caseData.getApplicant1ResponseDeadline().isBefore(LocalDateTime.now())
            && caseData.getApplicant1ProceedWithClaim() == null;

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

    public static final Predicate<CaseData> pastAddLegalRepDeadline = (caseData) ->
        // when notify change is merged, replace with this code
        // caseData.getAddLegalRepDeadline() != null && caseData.getAddLegalRepDeadline().isBefore(LocalDateTime.now());
        caseData.getAddLegalRepDeadline() == null
            ? true
            : caseData.getAddLegalRepDeadline().isBefore(LocalDateTime.now());

    public static final Predicate<CaseData> claimDismissedByCamunda = caseData ->
        caseData.getClaimDismissedDate() != null;

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

        if (!isSpecCaseCategory(caseData, caseData.getCaseAccessCategory() != null)) {
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

    public static final Predicate<CaseData> divergentRespondWithDQAndGoOfflineSpec = caseData ->
        isDivergentResponsesWithDQAndGoOfflineSpec(caseData);

    private static boolean isDivergentResponsesWithDQAndGoOfflineSpec(CaseData caseData) {

        if (!isSpecCaseCategory(caseData, caseData.getCaseAccessCategory() != null)) {
            return false;
        }

        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP:
                //scenario: only one of them have submitted full defence response
                return caseData.getRespondent1ClaimResponseTypeForSpec() != null
                    && !caseData.getRespondent1ClaimResponseTypeForSpec()
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                    && NO.equals(caseData.getRespondentResponseIsSame())
                    && (RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    || RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
            case ONE_V_TWO_TWO_LEGAL_REP:
                //scenario: latest response is full defence
                return caseData.getRespondent1ClaimResponseTypeForSpec() != null
                    && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                    && caseData.getRespondent1ResponseDate() != null
                    && caseData.getRespondent2ResponseDate() != null
                    && (!caseData.getRespondent1ClaimResponseTypeForSpec()
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                    // for the time being, 1v2ds not full defence goes offline
                    || caseData.getRespondent1ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_DEFENCE);
            case TWO_V_ONE:
                return (RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                    || RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec()))
                    && !(RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                    && RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec()));
            default:
                return false;
        }
    }

    public static final Predicate<CaseData> divergentRespondGoOfflineSpec = caseData ->
        isDivergentResponsesGoOfflineSpec(caseData);

    private static boolean isDivergentResponsesGoOfflineSpec(CaseData caseData) {

        if (!isSpecCaseCategory(caseData, caseData.getCaseAccessCategory() != null)) {
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
                if ((!RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                    && !RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))
                    && (caseData.getClaimant1ClaimResponseTypeForSpec() != null
                    && caseData.getClaimant2ClaimResponseTypeForSpec() != null)
                    && !caseData.getClaimant1ClaimResponseTypeForSpec()
                    .equals(caseData.getClaimant2ClaimResponseTypeForSpec())) {
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceivedSpec = caseData ->
        getPredicateForAwaitingResponsesFullDefenceReceivedSpec(caseData);

    private static boolean getPredicateForAwaitingResponsesFullDefenceReceivedSpec(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                return (caseData.getRespondent1ClaimResponseTypeForSpec() != null
                    && caseData.getRespondent2ClaimResponseTypeForSpec() == null
                    && RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
                    ||
                    (caseData.getRespondent1ClaimResponseTypeForSpec() == null
                        && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                        && RespondentResponseTypeSpec.FULL_DEFENCE
                        .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
            default:
                return false;
        }
    }

    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceReceivedSpec = caseData ->
        getPredicateForAwaitingResponsesNonFullDefenceReceivedSpec(caseData);

    private static boolean getPredicateForAwaitingResponsesNonFullDefenceReceivedSpec(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                return (caseData.getRespondent1ClaimResponseTypeForSpec() != null
                    && caseData.getRespondent2ClaimResponseTypeForSpec() == null
                    && !RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData
                                                                           .getRespondent1ClaimResponseTypeForSpec()))
                    ||
                    (caseData.getRespondent1ClaimResponseTypeForSpec() == null
                        && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                        && !RespondentResponseTypeSpec.FULL_DEFENCE
                        .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
            default:
                return false;
        }
    }

    public static final Predicate<CaseData> specClaim = caseData ->
        isSpecCaseCategory(caseData, caseData.getCaseAccessCategory() != null);

    private FlowPredicate() {
        //Utility class
    }

    private static boolean getPredicateForClaimantIntentionProceed(CaseData caseData) {
        boolean predicate = false;
        if (isSpecCaseCategory(caseData, caseData.getCaseAccessCategory() != null)) {
            switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP:
                case ONE_V_TWO_TWO_LEGAL_REP:
                case ONE_V_ONE:
                    predicate = YES.equals(caseData.getApplicant1ProceedWithClaim());
                    break;
                case TWO_V_ONE:
                    predicate = YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1());
                    break;
                default:
                    break;
            }
        } else {
            switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP:
                case ONE_V_TWO_TWO_LEGAL_REP:
                    predicate = YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        || YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2());
                    break;
                case ONE_V_ONE:
                    predicate = YES.equals(caseData.getApplicant1ProceedWithClaim());
                    break;
                case TWO_V_ONE:
                    predicate = YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                        || YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1());
                    break;
                default:
                    break;
            }
        }
        return predicate;
    }

    private static boolean getPredicateForClaimantIntentionNotProceed(CaseData caseData) {
        boolean predicate = false;
        if (isSpecCaseCategory(caseData, caseData.getCaseAccessCategory() != null)) {
            switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP:
                case ONE_V_TWO_TWO_LEGAL_REP:
                case ONE_V_ONE:
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
                case ONE_V_TWO_ONE_LEGAL_REP:
                case ONE_V_TWO_TWO_LEGAL_REP:
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

    public static final Predicate<CaseData> oneVsOneCase = caseData ->
        getPredicateFor1v1Case(caseData);

    private static boolean getPredicateFor1v1Case(CaseData caseData) {
        return ONE_V_ONE.equals(getMultiPartyScenario(caseData));
    }

    public static final Predicate<CaseData> multipartyCase = caseData ->
        getPredicateForMultipartyCase(caseData);

    private static boolean getPredicateForMultipartyCase(CaseData caseData) {
        return isMultiPartyScenario(caseData);
    }

    public static final Predicate<CaseData> pinInPostEnabledAndLiP = caseData ->
        caseData.getRespondent1PinToPostLRspec() != null;

    public static final Predicate<CaseData> allAgreedToMediation = caseData -> {
        if (isSpecCaseCategory(caseData, caseData.getCaseAccessCategory() != null)
            && AllocatedTrack.SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack())
            && caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES) {
            if (caseData.getRespondent2() != null
                && caseData.getRespondent2SameLegalRepresentative().equals(NO)
                && caseData.getResponseClaimMediationSpec2Required() == YesOrNo.NO) {
                return false;
            }
            if (Optional.ofNullable(caseData.getApplicant1ClaimMediationSpecRequired())
                .map(SmallClaimMedicalLRspec::getHasAgreedFreeMediation)
                .filter(YesOrNo.NO::equals).isPresent()
                || Optional.ofNullable(caseData.getApplicantMPClaimMediationSpecRequired())
                .map(SmallClaimMedicalLRspec::getHasAgreedFreeMediation)
                .filter(YesOrNo.NO::equals).isPresent()) {
                return false;
            }
            return true;
        }
        return false;
    };

    public static final Predicate<CaseData> contactDetailsChange = caseData ->
        NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired());
}
