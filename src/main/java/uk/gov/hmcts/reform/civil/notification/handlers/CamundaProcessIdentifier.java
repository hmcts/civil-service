package uk.gov.hmcts.reform.civil.notification.handlers;

public enum CamundaProcessIdentifier {
    ClaimDismissedNotifyParties,
    LitigationFriendAddedNotifier,
    ChangeOfRepresentationNotifyParties,
    //NoC - Lip v Lip to LR v Lip
    ClaimantLipRepresentedWithNoCNotifier,
    //NoC - Lip v Lip to Lip v Lr or Lr v Lip to Lr v Lr
    DefendantLipRepresentedWithNoCNotifier
}
