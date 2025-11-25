package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

public final class NotificationPredicate {

    @BusinessRule(
        group = "ClaimDetails",
        summary = "Respondent requested time extension before acknowledging",
        description = "Respondent has been granted a time extension but has not acknowledged the claim details notification"
    )
    public static final Predicate<CaseData> notifiedTimeExtension =
        CaseDataPredicate.ClaimDetails.hasTimeExtensionRespondent1
            .and(CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent1.negate());

    @BusinessRule(
        group = "ClaimDetails",
        summary = "Claim details notified to both",
        description = "Claim details notification option was set to 'Both' in the defendant notification options"
    )
    public static final Predicate<CaseData> notifiedOptionsToBoth =
        CaseDataPredicate.ClaimDetails.hasNotifyOptionsBoth;

    @BusinessRule(
        group = "ClaimDetails",
        summary = "Claim details notification completed",
        description = "Claim details notification date exists and notify options were set (not 'Both')"
    )
    public static final Predicate<CaseData> afterNotifiedOptions =
        CaseDataPredicate.ClaimDetails.hasNotificationDate
            .and(CaseDataPredicate.ClaimDetails.hasNotifyOptions)
            .and(CaseDataPredicate.ClaimDetails.hasNotifyOptionsBoth.negate());

    private NotificationPredicate() {
    }
}
