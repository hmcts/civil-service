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
    private String respondentSolicitorClaimIssueEmailTemplate;

    @NotEmpty
    private String respondentSolicitorClaimDetailsEmailTemplate;

    @NotEmpty
    private String solicitorDefendantResponseCaseTakenOffline;

    @NotEmpty
    private String claimantSolicitorDefendantResponseFullDefence;

    @NotEmpty
    private String respondentSolicitorAcknowledgeClaim;

    @NotEmpty
    private String failedPayment;

    @NotEmpty
    private String solicitorClaimDismissed;

    @NotEmpty
    private String claimantSolicitorCaseWillProgressOffline;

    @NotEmpty
    private String claimantSolicitorAgreedExtensionDate;

    @NotEmpty
    private String claimantSolicitorConfirmsToProceed;

    @NotEmpty
    private String claimantSolicitorConfirmsNotToProceed;

    @NotEmpty
    private String claimantSolicitorClaimContinuingOnline;

    @NotEmpty
    private String solicitorCaseTakenOffline;

    @NotEmpty
    private String solicitorLitigationFriendAdded;
}
