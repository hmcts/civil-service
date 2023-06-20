package uk.gov.hmcts.reform.civil.notify;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated
@Data
public class NotificationsProperties {

    @NotEmpty
    private String govNotifyApiKey;

    @NotEmpty
    private String respondentSolicitorClaimIssueMultipartyEmailTemplate;

    @NotEmpty
    private String respondentSolicitorClaimDetailsEmailTemplate;

    @NotEmpty
    private String solicitorDefendantResponseCaseTakenOffline;

    @NotEmpty
    private String solicitorDefendantResponseCaseTakenOfflineMultiparty;

    @NotEmpty
    private String claimantSolicitorDefendantResponseFullDefence;

    @NotEmpty
    private String respondentSolicitorAcknowledgeClaim;

    @NotEmpty
    private String respondentSolicitorAcknowledgeClaimForSpec;

    @NotEmpty
    private String applicantSolicitorAcknowledgeClaimForSpec;

    @NotEmpty
    private String failedPayment;

    @NotEmpty
    private String failedPaymentForSpec;

    @NotEmpty
    private String solicitorClaimDismissedWithin4Months;

    @NotEmpty
    private String solicitorClaimDismissedWithin14Days;

    @NotEmpty
    private String solicitorClaimDismissedWithinDeadline;

    @NotEmpty String applicantHearingFeeUnpaid;
    @NotEmpty String respondentHearingFeeUnpaid;

    @NotEmpty
    private String claimantSolicitorCaseWillProgressOffline;

    @NotEmpty
    private String claimantSolicitorSpecCaseWillProgressOffline;

    @NotEmpty
    private String claimantSolicitorAgreedExtensionDate;

    @NotEmpty
    private String claimantSolicitorAgreedExtensionDateForSpec;

    @NotEmpty
    private String respondentSolicitorAgreedExtensionDateForSpec;

    @NotEmpty
    private String claimantSolicitorConfirmsToProceed;

    @NotEmpty
    private String claimantSolicitorConfirmsNotToProceed;

    @NotEmpty
    private String claimantSolicitorClaimContinuingOnline;

    @NotEmpty
    private String claimantSolicitorClaimContinuingOnlineForSpec;

    @NotEmpty
    private String claimantSolicitorClaimContinuingOnline1v2ForSpec;

    @NotEmpty
    private String claimantClaimContinuingOnlineForSpec;

    @NotEmpty
    private String respondentSolicitorClaimContinuingOnlineForSpec;

    @NotEmpty
    private String solicitorCaseTakenOffline;

    @NotEmpty
    private String solicitorTrialReady;

    @NotEmpty
    private String otherPartyTrialReady;

    @NotEmpty
    private String solicitorCaseTakenOfflineForSpec;

    @NotEmpty
    private String solicitorLitigationFriendAdded;

    @NotEmpty
    private String claimantSolicitorDefendantResponseForSpec;

    @NotEmpty
    private String claimantSolicitorImmediatelyDefendantResponseForSpec;

    @NotEmpty
    private String respondentSolicitorDefendantResponseForSpec;

    @NotEmpty
    private String respondentDefendantResponseForSpec;

    @NotEmpty
    private String sdoOrdered;

    @NotEmpty
    private String sdoOrderedSpec;

    @NotEmpty
    private String sdoOrderedSpecBilingual;

    @NotEmpty
    private String claimantSolicitorConfirmsNotToProceedSpec;

    @NotEmpty
    private String claimantSolicitorConfirmsNotToProceedSpecLip;

    @NotEmpty
    private String respondentSolicitorNotifyNotToProceedSpec;

    @NotEmpty
    private String claimantSolicitorConfirmsToProceedSpec;

    @NotEmpty
    private String respondentSolicitorNotifyToProceedSpec;

    @NotEmpty
    private String applicantSolicitor1DefaultJudgmentReceived;

    @NotEmpty
    private String respondentSolicitor1DefaultJudgmentReceived;

    @NotEmpty
    private String breathingSpaceEnterDefendantEmailTemplate;

    @NotEmpty
    private String breathingSpaceEnterApplicantEmailTemplate;

    @NotEmpty
    private String breathingSpaceLiftedApplicantEmailTemplate;

    @NotEmpty
    private String breathingSpaceLiftedRespondentEmailTemplate;

    @NotEmpty
    private String claimantSolicitorCounterClaimForSpec;

    @NotEmpty
    private String respondentSolicitorCounterClaimForSpec;

    @NotEmpty
    private String respondentDeadlineExtension;

    @NotEmpty
    private String respondentDeadlineExtensionWelsh;

    @NotEmpty
    private String claimantDeadlineExtension;

    @NotEmpty
    private String respondentSolicitor1DefaultJudgmentRequested;

    @NotEmpty
    private String applicantSolicitor1DefaultJudgmentRequested;

    @NotEmpty
    private String interimJudgmentRequestedClaimant;

    @NotEmpty
    private String interimJudgmentApprovalClaimant;

    @NotEmpty
    private String interimJudgmentRequestedDefendant;

    @NotEmpty
    private String interimJudgmentApprovalDefendant;

    @NotEmpty
    private String standardDirectionOrderDJTemplate;

    @NotEmpty
    private String caseworkerDefaultJudgmentRequested;

    private String respondentChangeOfAddressNotificationTemplate;

    @NotEmpty
    private String respondentLipFullAdmitOrPartAdmitTemplate;

    @NotEmpty
    private String respondentLipFullDefenceWithMediationTemplate;

    @NotEmpty
    private String respondentLipFullDefenceNoMediationTemplate;

    @NotEmpty
    private String respondentLipResponseSubmissionTemplate;

    @NotEmpty
    private String respondentLipResponseSubmissionBilingualTemplate;

    @NotEmpty
    private String hearingListedFeeClaimantLrTemplate;

    @NotEmpty
    private String hearingListedNoFeeClaimantLrTemplate;

    @NotEmpty
    private String hearingListedNoFeeDefendantLrTemplate;

    @NotEmpty
    private String noticeOfChangeFormerSolicitor;

    @NotEmpty
    private String noticeOfChangeOtherParties;

    @NotEmpty
    private String claimantSolicitorClaimContinuingOnlineCos;

    @NotEmpty
    private String evidenceUploadTemplate;

    @NotEmpty
    private String respondentCcjNotificationTemplate;

    @NotEmpty
    private String respondentCcjNotificationWelshTemplate;

    @NotEmpty
    private String respondentSolicitorCcjNotificationTemplate;

    @NotEmpty
    private String notifyClaimantLrTemplate;

    @NotEmpty
    private String notifyDefendantLipTemplate;

    @NotEmpty
    private String notifyApplicantLRMediationAgreementTemplate;

    @NotEmpty
    private String notifyRespondentLiPMediationAgreementTemplate;

    @NotEmpty
    private String notifyRespondentLiPMediationAgreementTemplateWelsh;

    @NotEmpty
    private String notifyRespondentLRMediationAgreementTemplate;

    @NotEmpty
    private String bundleCreationTemplate;

    @NotEmpty
    private String generateOrderNotificationTemplate;

    @NotEmpty
    private String respondentLipPartAdmitSettleClaimTemplate;

    @NotEmpty
    private String notifyDefendantLipWelshTemplate;

    @NotEmpty
    private String mediationUnsuccessfulClaimantLRTemplate;

    @NotEmpty
    private String mediationUnsuccessfulDefendantLIPTemplate;

    @NotEmpty
    private String mediationUnsuccessfulDefendantLIPBilingualTemplate;

    @NotEmpty
    private String respondent1DefaultJudgmentRequestedTemplate;

    @NotEmpty
    private String respondentLipPartAdmitSettleClaimBilingualTemplate;

    @NotEmpty
    private String notifyClaimantTranslatedDocumentUploaded;

    @NotEmpty
    private String notifyDefendantTranslatedDocumentUploaded;

    @NotEmpty
    private String respondent1LipClaimUpdatedTemplate;

    @NotEmpty
    private String hearingFeeUnpaidNoc;

    @NotEmpty
    private String notifyApplicantLRMediationSuccessfulTemplate;

    @NotEmpty
    private String notifyRespondentLiPMediationSuccessfulTemplate;

    @NotEmpty
    private String notifyRespondentLiPMediationSuccessfulTemplateWelsh;

}
