package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public interface NotificationPredicate {

    @BusinessRule(
        group = "Notification",
        summary = "Respondent requested time extension before acknowledging",
        description = "Respondent has been granted a time extension but has not acknowledged the claim details notification"
    )
    Predicate<CaseData> notifiedTimeExtension =
        CaseDataPredicate.Respondent.hasTimeExtensionRespondent1
            .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1.negate());

    @BusinessRule(
        group = "Notification",
        summary = "Claim details notified to both",
        description = "Claim details notification option was set to 'Both' in the defendant notification options"
    )
    Predicate<CaseData> hasNotifyOptionsBoth =
        CaseDataPredicate.ClaimDetails.isNotifyOptionsBoth;

    @BusinessRule(
        group = "Notification",
        summary = "Claim notified to both",
        description = "Claim notification option was set to 'Both' in the defendant notification options"
    )
    Predicate<CaseData> hasClaimNotifiedToBoth =
        CaseDataPredicate.Claim.hasNotificationDate
            .and(CaseDataPredicate.Claim.hasNotifyOptions.negate()
                     .or(CaseDataPredicate.Claim.isNotifyOptionsBoth));

    @BusinessRule(
        group = "Notification",
        summary = "Claim details notified to both",
        description = "Claim details notification option was set to 'Both' in the defendant notification options"
    )
    Predicate<CaseData> hasClaimDetailsNotifiedToBoth =
        CaseDataPredicate.ClaimDetails.hasNotificationDate
            .and(CaseDataPredicate.ClaimDetails.hasNotifyOptions.negate()
                     .or(CaseDataPredicate.ClaimDetails.isNotifyOptionsBoth));
}
