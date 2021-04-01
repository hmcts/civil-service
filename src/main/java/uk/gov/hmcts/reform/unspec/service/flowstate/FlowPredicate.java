package uk.gov.hmcts.reform.unspec.service.flowstate;

import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.time.LocalDateTime;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.unspec.enums.CaseState.CLOSED;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.PROCEEDS_WITH_OFFLINE_JOURNEY;
import static uk.gov.hmcts.reform.unspec.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.unspec.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.unspec.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.unspec.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;

public class FlowPredicate {

    public static final Predicate<CaseData> pendingCaseIssued = caseData ->
        caseData.getLegacyCaseReference() != null;

    public static final Predicate<CaseData> respondent1NotRepresented = caseData ->
        caseData.getIssueDate() != null
            && (caseData.getRespondent1Represented() == NO || caseData.getRespondent1OrgRegistered() == NO);

    public static final Predicate<CaseData> paymentFailed = caseData ->
        caseData.getPaymentDetails() != null && caseData.getPaymentDetails().getStatus() == FAILED;

    public static final Predicate<CaseData> paymentSuccessful = caseData ->
        caseData.getPaymentDetails() != null && caseData.getPaymentDetails().getStatus() == SUCCESS;

    public static final Predicate<CaseData> claimIssued = caseData ->
        caseData.getIssueDate() != null
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent1OrgRegistered() == YES;

    public static final Predicate<CaseData> claimNotified = caseData ->
        caseData.getClaimNotificationDate() != null;

    public static final Predicate<CaseData> claimDetailsNotified = caseData ->
        caseData.getClaimDetailsNotificationDate() != null;

    public static final Predicate<CaseData> respondentAcknowledgeClaim = caseData ->
        caseData.getRespondent1ClaimResponseIntentionType() != null
            && caseData.getRespondent1ClaimResponseType() == null
            && caseData.getRespondent1ClaimResponseDocument() == null
            && caseData.getCcdState() != CLOSED;

    public static final Predicate<CaseData> respondentFullDefence = caseData ->
        caseData.getRespondent1ClaimResponseType() == FULL_DEFENCE
            && caseData.getCcdState() != CLOSED;

    public static final Predicate<CaseData> respondentFullAdmission = caseData ->
        caseData.getRespondent1ClaimResponseType() == FULL_ADMISSION
            && caseData.getCcdState() != CLOSED;

    public static final Predicate<CaseData> respondentPartAdmission = caseData ->
        caseData.getRespondent1ClaimResponseType() == PART_ADMISSION
            && caseData.getCcdState() != CLOSED;

    public static final Predicate<CaseData> respondentCounterClaim = caseData ->
        caseData.getRespondent1ClaimResponseType() == COUNTER_CLAIM
            && caseData.getCcdState() != CLOSED;

    public static final Predicate<CaseData> applicantRespondToDefence = caseData ->
        caseData.getApplicant1ProceedWithClaim() != null
            && caseData.getApplicant1DefenceResponseDocument() != null
            && caseData.getCcdState() != PROCEEDS_WITH_OFFLINE_JOURNEY;

    public static final Predicate<CaseData> claimWithdrawn = caseData ->
        caseData.getWithdrawClaim() != null
            && caseData.getCcdState() == CLOSED;

    public static final Predicate<CaseData> respondentAgreedExtension = caseData ->
        caseData.getRespondentSolicitor1AgreedDeadlineExtension() != null;

    public static final Predicate<CaseData> claimDiscontinued = caseData ->
        caseData.getDiscontinueClaim() != null
            && caseData.getCcdState() == CLOSED;

    // update with dateClaimTakenOffline date when exists
    public static final Predicate<CaseData> claimTakenOffline = caseData ->
        caseData.getCcdState() == PROCEEDS_WITH_OFFLINE_JOURNEY;

    public static final Predicate<CaseData> caseProceedsInCaseman = caseData ->
        caseData.getClaimProceedsInCaseman() != null;

    public static final Predicate<CaseData> caseDismissed = caseData ->
        caseData.getClaimDismissedDate() != null && caseData.getRespondent1ClaimResponseIntentionType() == null;

    public static final Predicate<CaseData> caseDismissedAfterClaimAcknowledged = caseData ->
        caseData.getClaimDismissedDate() != null && caseData.getRespondent1ClaimResponseIntentionType() != null;

    public static final Predicate<CaseData> applicantOutOfTime = caseData ->
        caseData.getTakenOfflineDate() != null && caseData.getTakenOfflineDate().isAfter(LocalDateTime.now());

    public static final Predicate<CaseData> failToNotifyClaim = caseData ->
        caseData.getClaimDismissedDate() != null
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
