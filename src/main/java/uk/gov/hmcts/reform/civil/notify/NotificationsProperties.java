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
    private String solicitorCaseTakenOfflineNoApplicantResponse;

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
    private String claimantSolicitorDefendantResponse1v2DSForSpec;

    @NotEmpty
    private String claimantSolicitorImmediatelyDefendantResponseForSpec;

    @NotEmpty
    private String respondentSolicitorDefendantResponseForSpec;

    @NotEmpty
    private String respondentSolicitorDefResponseSpecWithClaimantAction;

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
    private String notifyRespondentLipPartAdmitPayImmediatelyAcceptedSpec;

    @NotEmpty
    private String respondentSolicitorNotifyNotToProceedSpec;

    @NotEmpty
    private String notifyRespondentSolicitorPartAdmitPayImmediatelyAcceptedSpec;

    @NotEmpty
    private String claimantSolicitorConfirmsToProceedSpec;

    @NotEmpty
    private String claimantSolicitorConfirmsToProceedSpecWithAction;

    @NotEmpty
    private String respondentSolicitorNotifyToProceedSpec;

    @NotEmpty
    private String respondentSolicitorNotifyToProceedSpecWithAction;

    @NotEmpty
    private String applicantSolicitor1DefaultJudgmentReceived;

    @NotEmpty
    private String applicantLiPDefaultJudgmentRequested;

    @NotEmpty
    private String respondentSolicitor1DefaultJudgmentReceived;

    @NotEmpty
    private String respondentSolicitor1DefaultJudgmentReceivedForLipVSLR;

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
    private String hearingListedNoFeeDefendantLrTemplateHMC;

    @NotEmpty
    private String hearingListedFeeClaimantLrTemplateHMC;

    @NotEmpty
    private String hearingListedNoFeeClaimantLrTemplateHMC;

    @NotEmpty
    private String noticeOfChangeFormerSolicitor;

    @NotEmpty
    private String noticeOfChangeOtherParties;

    @NotEmpty
    private String notifyNewDefendantSolicitorNOC;

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
    private String mediationUnsuccessfulClaimantLIPWelshTemplate;

    @NotEmpty
    private String mediationUnsuccessfulClaimantLIPTemplate;

    @NotEmpty
    private String mediationUnsuccessfulLRTemplate;

    @NotEmpty
    private String mediationUnsuccessfulLRTemplateForLipVLr;

    @NotEmpty
    private String mediationUnsuccessfulNoAttendanceLRTemplate;

    @NotEmpty
    private String mediationUnsuccessfulLIPTemplate;

    @NotEmpty
    private String mediationUnsuccessfulLIPTemplateWelsh;

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
    private String claimantLipClaimUpdatedTemplate;

    @NotEmpty
    private String hearingFeeUnpaidNoc;

    @NotEmpty
    private String notifyApplicantLRMediationSuccessfulTemplate;

    @NotEmpty
    private String notifyApplicantLiPMediationSuccessfulTemplate;

    @NotEmpty
    private String notifyApplicantLiPMediationSuccessfulWelshTemplate;

    @NotEmpty
    private String notifyRespondentLiPMediationSuccessfulTemplate;

    @NotEmpty
    private String notifyRespondentLiPMediationSuccessfulTemplateWelsh;

    @NotEmpty
    private String notifyDefendantLIPClaimantSettleTheClaimTemplate;

    @NotEmpty
    private String  evidenceUploadLipTemplate;

    @NotEmpty
    private String notifyLipUpdateTemplate;

    @NotEmpty
    private String notifyLipUpdateTemplateBilingual;

    @NotEmpty
    private String notifyUpdateTemplate;

    @NotEmpty
    private String notifyClaimReconsiderationLRTemplate;

    @NotEmpty
    private String hearingNotificationLipDefendantTemplate;

    @NotEmpty
    private String notifyLiPClaimantClaimSubmittedAndPayClaimFeeTemplate;

    @NotEmpty
    private String notifyLiPClaimantDefendantResponded;

    @NotEmpty
    private String notifyLiPClaimantDefendantChangedContactDetails;

    @NotEmpty
    private String notifyApplicant1EnteredBreathingSpaceLip;

    @NotEmpty
    private String notifyLiPClaimantClaimSubmittedAndHelpWithFeeTemplate;

    @NotEmpty
    private String notifyEnteredBreathingSpaceForDefendantLip;

    @NotEmpty
    private String notifyDefendantLrTemplate;

    @NotEmpty
    private String respondentLrPartAdmitSettleClaimTemplate;

    @NotEmpty
    private String notifyLiPApplicantBreathingSpaceLifted;

    @NotEmpty
    private String notifyLiPRespondentBreathingSpaceLifted;

    @NotEmpty
    private String claimantLipDeadlineExtension;

    @NotEmpty
    private String notifyClaimantLipTemplateManualDetermination;

    @NotEmpty
    private String notifyApplicantLipRequestJudgementByAdmissionNotificationTemplate;

    @NotEmpty
    private String notifyRespondentLipRequestJudgementByAdmissionNotificationTemplate;

    @NotEmpty
    private String notifyClaimantLipHelpWithFees;

    @NotEmpty
    private String notifyClaimantLipHelpWithFeesWelsh;

    @NotEmpty
    private String notifyClaimantAfterClaimIssue;

    @NotEmpty
    private  String notifyApplicantForSignedSettlementAgreement;

    @NotEmpty
    private  String notifyApplicantForNotAgreedSignSettlement;

    @NotEmpty
    private String notifyRespondentForSignedSettlementAgreement;

    @NotEmpty
    private String notifyRespondentForNotAgreedSignSettlement;

    @NotEmpty
    private String notifyRespondentLipForClaimantRepresentedTemplate;

    @NotEmpty
    private String notifyClaimantLipForNoLongerAccessTemplate;

    @NotEmpty
    private String notifyClaimantLipForNoLongerAccessWelshTemplate;

    @NotEmpty
    private String notifyClaimantLiPTranslatedDocumentUploadedWhenClaimIssuedInBilingual;

    @NotEmpty
    private String notifyClaimantLiPTranslatedDocumentUploadedWhenClaimIssuedInEnglish;

    @NotEmpty
    private String notifyClaimantLipForClaimSubmissionTemplate;

    @NotEmpty
    private String notifyLiPClaimantClaimSubmittedAndPayClaimFeeBilingualTemplate;

    @NotEmpty
    private String notifyLiPClaimantClaimSubmittedAndHelpWithFeeBilingualTemplate;

    @NotEmpty
    private String claimantLipClaimUpdatedBilingualTemplate;

    @NotEmpty
    private String bilingualClaimantClaimContinuingOnlineForSpec;

    @NotEmpty
    private String applicantLiPDefaultJudgmentRequestedBilingualTemplate;

    @NotEmpty
    private String notifyApplicantLipForSignedSettlementAgreementInBilingual;

    @NotEmpty
    private String notifyApplicantLipForNotAgreedSignSettlementInBilingual;

    @NotEmpty
    private String notifyApplicantForHwFMoreInformationNeeded;

    @NotEmpty
    private String notifyApplicantForHwFMoreInformationNeededWelsh;

    @NotEmpty
    private String notifyApplicantForHwfNoRemission;

    @NotEmpty
    private String notifyApplicantForHwfNoRemissionWelsh;

    @NotEmpty
    private String notifyApplicantForHwfUpdateRefNumber;

    @NotEmpty
    private String notifyApplicantForHwfPartialRemission;

    @NotEmpty
    private String notifyApplicantForHwfInvalidRefNumber;

    @NotEmpty
    private String notifyApplicantForHwfUpdateRefNumberBilingual;

    @NotEmpty
    private String notifyApplicantForHwfPartialRemissionBilingual;

    @NotEmpty
    private String notifyApplicantForHwfInvalidRefNumberBilingual;

    @NotEmpty
    private String notifyApplicantForHwfFeePaymentOutcome;

    @NotEmpty
    private String notifyApplicantForHwfFeePaymentOutcomeInBilingual;

    @NotEmpty
    private String notifyDefendantLRForMediation;

    @NotEmpty
    private String notifyApplicantLRMediationTemplate;

    @NotEmpty
    private String notifySetAsideJudgmentTemplate;

    @NotEmpty
    private String notifyLrClaimantSuccessfulMediation;

    @NotEmpty
    private String notifyOneVTwoClaimantSuccessfulMediation;

    @NotEmpty
    private String notifyLipSuccessfulMediation;

    @NotEmpty
    private String notifyLipSuccessfulMediationWelsh;

    @NotEmpty
    private String notifyLrDefendantSuccessfulMediation;

    @NotEmpty
    private String notifyLrDefendantSuccessfulMediationForLipVLrClaim;

    @NotEmpty
    private String notifyTwoVOneDefendantSuccessfulMediation;

    @NotEmpty
    private String notifyClaimantJudgmentVariedDeterminationOfMeansTemplate;

    @NotEmpty
    private String notifyDefendantJudgmentVariedDeterminationOfMeansTemplate;

    @NotEmpty
    private String notifyLrRecordJudgmentDeterminationMeansTemplate;

    @NotEmpty
    private String noticeOfChangeApplicantLipSolicitorTemplate;

    @NotEmpty
    private String notifyClaimantLipForDefendantRepresentedTemplate;

    @NotEmpty
    private String notifyDefendantLipForNoLongerAccessTemplate;

    @NotEmpty
    private String notifyDefendantLrAfterNoticeOfChangeTemplate;

    @NotEmpty
    private String notifyClaimantLipBilingualAfterDefendantNOC;

    @NotEmpty
    private String notifyDefendantLipBilingualAfterDefendantNOC;

    @NotEmpty
    private String notifyDJNonDivergentSpecClaimantTemplate;

    @NotEmpty
    private String notifyDJNonDivergentSpecDefendantTemplate;

    @NotEmpty
    private String hearingNotificationLipDefendantTemplateWelsh;

    @NotEmpty
    private String evidenceUploadLipTemplateWelsh;

    @NotEmpty
    private String notifySettleClaimMarkedPaidInFullDefendantTemplate;

    @NotEmpty
    private String notifyClaimantLRJudgmentByAdmissionTemplate;

    @NotEmpty
    private String notifyDefendantLIPJudgmentByAdmissionTemplate;

    @NotEmpty
    private String orderBeingTranslatedTemplateWelsh;

    @NotEmpty
    private String notifyLiPOrderTranslatedTemplate;

    @NotEmpty
    private String notifyClaimantLrValidationDiscontinuanceFailureTemplate;

    @NotEmpty
    private String notifyClaimDiscontinuedLRTemplate;

    @NotEmpty
    private String notifyLRBundleRestitched;

    @NotEmpty
    private String notifyClaimDiscontinuedLipTemplate;

    @NotEmpty
    private String notifyLRCaseStayed;

    @NotEmpty
    private String notifyLRCaseDismissed;

    @NotEmpty
    private String notifyLRStayLifted;

    @NotEmpty
    private String notifyLRStayUpdateRequested;

    @NotEmpty
    private String notifyLipStayUpdateRequested;

    @NotEmpty
    private String notifyLipBilingualStayUpdateRequested;

    @NotEmpty
    private String notifyClaimantLRCoscApplied;

    @NotEmpty
    private String sdoOrderedSpecEa;
}
