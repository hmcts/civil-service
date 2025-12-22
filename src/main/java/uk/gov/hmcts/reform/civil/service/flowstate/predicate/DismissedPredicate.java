package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public non-sealed interface DismissedPredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Dismissed",
        summary = "Claim dismissed (Camunda)",
        description = "Claim has a recorded dismissed date indicating automated processing (Camunda)"
    )
    Predicate<CaseData> byCamunda = CaseDataPredicate.Claim.hasDismissedDate;

    @BusinessRule(
        group = "Dismissed",
        summary = "Hearing fee due date missed - case dismissed",
        description = "Case dismissed because the hearing fee due date has passed"
    )
    Predicate<CaseData> pastHearingFeeDue = CaseDataPredicate.Hearing.hasDismissedFeeDueDate;

    @BusinessRule(
        group = "Dismissed",
        summary = "Claim dismissal deadline has expired",
        description = "Case dismissed because the claim dismissal deadline has passed"
    )
    Predicate<CaseData> pastClaimDeadline = CaseDataPredicate.Claim.hasPassedDismissalDeadline;

    @BusinessRule(
        group = "Dismissed",
        summary = "Claim notification deadline expired (no notification recorded)",
        description = "Claim notification deadline has passed and no claim notification date is set"
    )
    Predicate<CaseData> pastClaimNotificationDeadline =
        CaseDataPredicate.Claim.hasPassedNotificationDeadline
            .and(CaseDataPredicate.Claim.hasNotificationDate.negate());

    @BusinessRule(
        group = "Dismissed",
        summary = "Claim details notification deadline expired (no details recorded)",
        description = "Claim details notification deadline has passed with no claim details notification date set (claim notification exists)"
    )
    Predicate<CaseData> pastClaimDetailsNotificationDeadline =
        CaseDataPredicate.ClaimDetails.passedNotificationDeadline
            .and(CaseDataPredicate.ClaimDetails.hasNotificationDate.negate())
            .and(CaseDataPredicate.Claim.hasNotificationDate);

    @BusinessRule(
        group = "Dismissed",
        summary = "Dismissed - extension active and no acknowledgement",
        description = "Dismissal deadline has passed; no respondent intends to proceed; at least one respondent with a time extension has not acknowledged service"
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
        summary = "Dismissed - acknowledged, no extension, no response",
        description = "Dismissal deadline has passed; at least respondent 1 acknowledged service and has no time " +
            "extension; not taken offline by staff; in 1v2 two‑solicitor cases both acknowledged with at least one still " +
            "without a response"
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
        summary = "Dismissed - acknowledged with extension(s)",
        description = "Dismissal deadline has passed; respondent(s) acknowledged service and at least one time extension " +
            "applies; not marked 'not suitable for SDO' and not taken offline by staff"
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
        summary = "Dismissed - claim details deadline expired (no AoS/response/extension)",
        description = "Dismissal deadline has passed; no acknowledgement, no response, no time extension or intention to " +
            "proceed recorded; not taken offline by staff (supports 1v1 and 1v2 scenarios)"
    )
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
