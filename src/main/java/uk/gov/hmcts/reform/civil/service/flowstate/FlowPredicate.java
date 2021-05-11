package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class FlowPredicate {

    public static final Predicate<CaseData> claimSubmitted = caseData ->
        caseData.getSubmittedDate() != null;

    public static final Predicate<CaseData> respondent1NotRepresented = caseData ->
        caseData.getIssueDate() != null && caseData.getRespondent1Represented() == NO;

    public static final Predicate<CaseData> respondent1OrgNotRegistered = caseData ->
        caseData.getIssueDate() != null && caseData.getRespondent1OrgRegistered() == NO;

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
            && caseData.getRespondent1OrgRegistered() == YES;

    public static final Predicate<CaseData> claimNotified = caseData ->
        caseData.getClaimNotificationDate() != null;

    public static final Predicate<CaseData> claimIssued = caseData ->
        caseData.getClaimNotificationDeadline() != null;

    public static final Predicate<CaseData> claimDetailsNotifiedTimeExtension = caseData ->
        caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null;

    public static final Predicate<CaseData> claimDetailsNotified = caseData ->
        caseData.getClaimDetailsNotificationDate() != null;

    public static final Predicate<CaseData> notificationAcknowledged = caseData ->
        caseData.getRespondent1AcknowledgeNotificationDate() != null;

    public static final Predicate<CaseData> notificationAcknowledgedTimeExtension = caseData ->
        caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null;

    public static final Predicate<CaseData> fullDefence = caseData ->
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1ClaimResponseType() == FULL_DEFENCE;

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
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1ClaimResponseType() == FULL_ADMISSION;

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
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1ClaimResponseType() == PART_ADMISSION;

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
        caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1ClaimResponseType() == COUNTER_CLAIM;

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
        caseData.getApplicant1ProceedWithClaim() != null
            && caseData.getApplicant1ProceedWithClaim() == YES;

    public static final Predicate<CaseData> fullDefenceNotProceed = caseData ->
        caseData.getApplicant1ProceedWithClaim() != null
            && caseData.getApplicant1ProceedWithClaim() == NO;

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
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getClaimDismissedDate() == null
            && caseData.getRespondent1ResponseDate() == null;

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimDetailsNotifiedExtension = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1ResponseDate() == null;

    public static final Predicate<CaseData> takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null
            && caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1ResponseDate() == null;

    public static final Predicate<CaseData> takenOfflineByStaffAfterNotificationAcknowledged = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1ResponseDate() == null;

    public static final Predicate<CaseData> caseDismissedAfterDetailNotified = caseData ->
        caseData.getClaimDismissedDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1ClaimResponseIntentionType() == null;

    public static final Predicate<CaseData> caseDismissedAfterDetailNotifiedExtension = caseData ->
        caseData.getClaimDismissedDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1ClaimResponseIntentionType() == null;

    public static final Predicate<CaseData> caseDismissedAfterClaimAcknowledged = caseData ->
        caseData.getClaimDismissedDate() != null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null;

    public static final Predicate<CaseData> caseDismissedAfterClaimAcknowledgedExtension = caseData ->
        caseData.getClaimDismissedDate() != null
            && caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() != null;

    public static final Predicate<CaseData> applicantOutOfTime = caseData ->
        caseData.getTakenOfflineDate() != null
            && caseData.getApplicant1ResponseDeadline() != null
            && caseData.getApplicant1ResponseDeadline().isBefore(LocalDateTime.now());

    public static final Predicate<CaseData> pastClaimNotificationDeadline = caseData ->
        caseData.getClaimDismissedDate() != null
            && caseData.getClaimNotificationDeadline() != null
            && caseData.getClaimNotificationDeadline().isBefore(LocalDateTime.now())
            && caseData.getClaimNotificationDate() == null;

    public static final Predicate<CaseData> pastClaimDetailsNotificationDeadline = caseData ->
        caseData.getClaimDetailsNotificationDeadline() != null
            && caseData.getClaimDetailsNotificationDeadline().isBefore(LocalDateTime.now())
            && caseData.getClaimDetailsNotificationDate() == null
            && caseData.getClaimNotificationDate() != null
            && caseData.getClaimDismissedDate() != null;

    private FlowPredicate() {
        //Utility class
    }
}
