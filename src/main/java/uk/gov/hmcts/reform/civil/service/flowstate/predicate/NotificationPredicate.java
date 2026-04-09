package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public non-sealed interface NotificationPredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Notification",
        summary = "Claim notification completed - options both/unspecified",
        description = "Claim notification date exists and defendant notify options are either not set or set to 'Both'"
    )
    Predicate<CaseData> hasClaimNotifiedToBoth =
        CaseDataPredicate.Claim.hasNotificationDate
            .and(CaseDataPredicate.Claim.hasNotifyOptions.negate()
                     .or(CaseDataPredicate.Claim.isNotifyOptionsBoth));

    @BusinessRule(
        group = "Notification",
        summary = "Claim details notification completed - options both/unspecified",
        description = "Claim details notification date exists and defendant notify options are either not set or set to 'Both'"
    )
    Predicate<CaseData> hasClaimDetailsNotifiedToBoth =
        CaseDataPredicate.ClaimDetails.hasNotificationDate
            .and(CaseDataPredicate.ClaimDetails.hasNotifyOptions.negate()
                     .or(CaseDataPredicate.ClaimDetails.isNotifyOptionsBoth));
}
