package uk.gov.hmcts.reform.civil.config.properties.notification;

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
    private String respondentSolicitorClaimDetailsEmailTemplateMultiParty;

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
    private String solicitorClaimDismissed;

    @NotEmpty
    private String claimantSolicitorCaseWillProgressOffline;

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
    private String respondentSolicitorClaimContinuingOnlineForSpec;

    @NotEmpty
    private String solicitorCaseTakenOffline;

    @NotEmpty
    private String solicitorCaseTakenOfflineForSpec;

    @NotEmpty
    private String solicitorLitigationFriendAdded;

    @NotEmpty
    private String claimantSolicitorDefendantResponseForSpec;

    @NotEmpty
    private String respondentSolicitorDefendantResponseForSpec;

    @NotEmpty
    private String sdoOrdered;

    @NotEmpty
    private String claimantSolicitorConfirmsNotToProceedSpec;

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
}
