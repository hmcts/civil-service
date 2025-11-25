package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

public final class DismissedPredicate {

    @BusinessRule(
        group = "Dismissed",
        summary = "Case dismissed after claim-notified deadline",
        description = "Claim dismissal deadline passed and respondents have not acknowledged/responded, and case not taken offline by staff")
    public static final Predicate<CaseData> dismissedAfterClaimDetailNotified =
        CaseDataPredicate.Claim.hasPassedDismissalDeadline
            .and(CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent1.negate())
            .and(CaseDataPredicate.ClaimDetails.hasTimeExtensionRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasIntentionToProceedRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate())
            .and(
                (
                    CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.or(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep)
                        .and(CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent2.negate()
                                 .and(CaseDataPredicate.ClaimDetails.hasTimeExtensionRespondent2.negate())
                                 .and(CaseDataPredicate.Respondent.hasIntentionToProceedRespondent2.negate())
                                 .and(CaseDataPredicate.Respondent.hasResponseDateRespondent2.negate()))
                )
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate()
                            .and(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep.negate())
                    )
            );

    @BusinessRule(
        group = "Dismissed",
        summary = "Past claim details notification deadline",
        description = "Claim details notification deadline has passed without a claim details notification date being set")
    public static final Predicate<CaseData> pastClaimDetailsNotificationDeadline =
        CaseDataPredicate.ClaimDetails.hasPassedNotificationDeadline
            .and(CaseDataPredicate.ClaimDetails.hasNotificationDate.negate())
            .and(CaseDataPredicate.Claim.hasNotificationDate);

    @BusinessRule(
        group = "Dismissed",
        summary = "Claim dismissed by Camunda",
        description = "A claim dismissal date has been recorded (automated Camunda handling)")
    public static final Predicate<CaseData> claimDismissedByCamunda = CaseDataPredicate.Claim.hasDismissedDate;

    @BusinessRule(
        group = "Dismissed",
        summary = "Case dismissed: past hearing fee due",
        description = "Case dismissed due to past hearing fee due date")
    public static final Predicate<CaseData> caseDismissedPastHearingFeeDue = CaseDataPredicate.Hearing.hasDismissedFeeDueDate;

    private DismissedPredicate() {
    }
}
