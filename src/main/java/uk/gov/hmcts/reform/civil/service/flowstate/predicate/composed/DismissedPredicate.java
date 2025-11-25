package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public interface DismissedPredicate {

    @BusinessRule(
        group = "Dismissed",
        summary = "Claim dismissed by Camunda",
        description = "A claim dismissal date has been recorded (automated Camunda handling)")
    Predicate<CaseData> byCamunda = CaseDataPredicate.Claim.hasDismissedDate;

    @BusinessRule(
        group = "Dismissed",
        summary = "Case dismissed past hearing fee due",
        description = "Case dismissed due to past hearing fee due date")
    Predicate<CaseData> pastHearingFeeDue = CaseDataPredicate.Hearing.hasDismissedFeeDueDate;

    @BusinessRule(
        group = "Dismissed",
        summary = "Case dismissed past claim deadline",
        description = "Case dismissed due to past claim deadline")
    Predicate<CaseData> pastClaimDeadline = CaseDataPredicate.Claim.hasPassedDismissalDeadline;

    @BusinessRule(
        group = "Dismissed",
        summary = "Past claim notification deadline",
        description = "Claim notification deadline has passed without a claim notification date being set")
    Predicate<CaseData> pastClaimNotificationDeadline =
        CaseDataPredicate.Claim.hasPassedNotificationDeadline
            .and(CaseDataPredicate.Claim.hasNotificationDate.negate());

    @BusinessRule(
        group = "Dismissed",
        summary = "Past claim details notification deadline",
        description = "Claim details notification deadline has passed without a claim details notification date being set")
    Predicate<CaseData> pastClaimDetailsNotificationDeadline =
        CaseDataPredicate.ClaimDetails.passedNotificationDeadline
            .and(CaseDataPredicate.ClaimDetails.hasNotificationDate.negate())
            .and(CaseDataPredicate.Claim.hasNotificationDate);

    @BusinessRule(
        group = "Dismissed",
        summary = "Dismissed after claim notified extension",
        description = "Dismissal deadline has passed with no intention to proceed from respondents, and at least one respondent with a time extension has not acknowledged the claim."
    )
    Predicate<CaseData> afterClaimNotifiedExtension =
        CaseDataPredicate.Claim.hasPassedDismissalDeadline
            .and(CaseDataPredicate.Respondent.hasIntentionToProceedRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasIntentionToProceedRespondent2.negate())
            .and(
                (CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1.negate()
                    .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent1))
                    .or(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent2.negate()
                        .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent2))
            );

    @BusinessRule(
        group = "Dismissed",
        summary = "Case dismissed after claim acknowledged and deadline passed",
        description = "Checks if a case is dismissed after claim acknowledgment and deadline passed"
    )
    Predicate<CaseData> afterClaimAcknowledged =
        CaseDataPredicate.Claim.hasPassedDismissalDeadline
            .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1)
            .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent1.negate())
            .and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate())
            .and(
                (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
                    .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent2)
                    .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent2.negate())
                    .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate()
                        .or(CaseDataPredicate.Respondent.hasResponseDateRespondent2.negate()))
                )
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate()
                        .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate())
                    )
            );

    @BusinessRule(
        group = "Dismissed",
        summary = "Case dismissed after claim acknowledged and time extension deadline passed",
        description = "Checks if a case is dismissed after claim acknowledgment and time extension"
    )
    Predicate<CaseData> afterClaimAcknowledgedExtension =
        CaseDataPredicate.Claim.hasPassedDismissalDeadline
            .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1)
            .and(CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable.negate())
            .and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate())
            .and(
                (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.or(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep)
                    .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent2
                         .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent1
                             .or(CaseDataPredicate.Respondent.hasTimeExtensionRespondent2)))
                )
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate()
                        .and(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep.negate())
                            .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent1)
                    )
            );

    @BusinessRule(
        group = "Dismissed",
        summary = "Case dismissed after claim-notified deadline passed",
        description = "Claim dismissal deadline passed and respondents have not acknowledged/responded, and case not taken offline by staff")
    Predicate<CaseData> afterClaimDetailNotified =
        CaseDataPredicate.Claim.hasPassedDismissalDeadline
            .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasIntentionToProceedRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate())
            .and(
                (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.or(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep)
                    .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent2.negate()
                         .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent2.negate())
                         .and(CaseDataPredicate.Respondent.hasIntentionToProceedRespondent2.negate())
                         .and(CaseDataPredicate.Respondent.hasResponseDateRespondent2.negate()))
                )
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate()
                        .and(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep.negate())
                    )
            );

}
