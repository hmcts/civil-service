package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public interface TakenOfflinePredicate {

    @BusinessRule(
        group = "TakenOffline",
        summary = "Case taken offline by system",
        description = "Taken offline date exists"
    )
    Predicate<CaseData> bySystem = CaseDataPredicate.TakenOffline.dateExists;

    @BusinessRule(
        group = "TakenOffline",
        summary = "Case taken offline by staff",
        description = "Taken offline by staff date exists"
    )
    Predicate<CaseData> byStaff = CaseDataPredicate.TakenOffline.byStaffDateExists;

    @BusinessRule(
        group = "TakenOffline",
        summary = "SDO not Suitable",
        description = "Case has 'not suitable for SDO' reason"
    )
    Predicate<CaseData> sdoNotSuitable = CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable;

    @BusinessRule(
        group = "TakenOffline",
        summary = "SDO not drawn",
        description = "Case has 'not suitable for SDO' reason and taken offline date exists"
    )
    Predicate<CaseData> sdoNotDrawn =
        CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable
            .and(CaseDataPredicate.TakenOffline.dateExists);

    @BusinessRule(
        group = "TakenOffline",
        summary = "Before SDO (draw directions order) handling",
        description = "Case not marked for draw directions order and not marked as 'not suitable for SDO', response date exists"
    )
    Predicate<CaseData> beforeSdo =
        CaseDataPredicate.Applicant.hasResponseDateApplicant1
            .and(CaseDataPredicate.TakenOffline.hasDrawDirectionsOrderRequired.negate())
            .and(CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable.negate());

    @BusinessRule(
        group = "TakenOffline",
        summary = "After SDO (draw directions order) handling",
        description = "Case marked for draw directions order and not marked as 'not suitable for SDO',"
    )
    Predicate<CaseData> afterSdo =
        CaseDataPredicate.TakenOffline.hasDrawDirectionsOrderRequired
            .and(CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable.negate());

    @BusinessRule(
        group = "TakenOffline",
        summary = "After SDO (draw directions order) not suitable for SDO",
        description = "Case marked for draw directions order and marked as 'not suitable for SDO',"
    )
    Predicate<CaseData> afterSdoNotSuitable =
        CaseDataPredicate.TakenOffline.hasDrawDirectionsOrderRequired.negate()
            .and(CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable);

    @BusinessRule(
        group = "TakenOffline",
        summary = "Claim notification completed",
        description = "Claim notification date exists and notify options were set (not 'Both')"
    )
    Predicate<CaseData> beforeClaimIssue =
        // In case of SPEC and UNSPEC claim ClaimNotificationDeadline will be set when the case is issued
        CaseDataPredicate.Claim.hasNotificationDeadline.negate()
            .and(CaseDataPredicate.Claim.hasNotificationDate.negate())
            .and(CaseDataPredicate.Claim.hasSubmittedDate);

    @BusinessRule(
        group = "TakenOffline",
        summary = "Claim notification completed",
        description = "Claim notification date exists and notify options were set (not 'Both')"
    )
    Predicate<CaseData> afterClaimNotified =
        CaseDataPredicate.Claim.hasNotificationDate
            .and(CaseDataPredicate.Claim.hasNotifyOptions)
            .and(CaseDataPredicate.Claim.isNotifyOptionsBoth.negate()); // offline

    @BusinessRule(
        group = "TakenOffline",
        summary = "Claim details notification completed",
        description = "Claim details notification date exists and notify options were set (not 'Both')"
    )
    Predicate<CaseData> afterClaimDetailsNotified =
        CaseDataPredicate.ClaimDetails.hasNotificationDate
            .and(CaseDataPredicate.ClaimDetails.hasNotifyOptions)
            .and(CaseDataPredicate.ClaimDetails.isNotifyOptionsBoth.negate()); // offline

    @BusinessRule(
        group = "TakenOffline",
        summary = "SDO not drawn after claim notified extension",
        description = "Case has 'not suitable for SDO' reason, has been taken offline, respondent 1 has a time extension for response, but has not yet acknowledged service or provided a response."
    )
    Predicate<CaseData> afterClaimNotifiedExtension =
            CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1.negate()
            .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent1);

    @BusinessRule(
        group = "TakenOffline",
        summary = "Taken offline after claim notified",
        description = "Case taken offline after claim notification but before claim details notification and respondent acknowledgement"
    )
    Predicate<CaseData> afterClaimNotifiedFutureDeadline =
            CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1.negate()
            .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.ClaimDetails.hasNotificationDate.negate())
            .and(CaseDataPredicate.ClaimDetails.futureNotificationDeadline);

    @BusinessRule(
        group = "TakenOffline",
        summary = "Taken offline after claim details notified (acknowledged, N/A response, extension)",
        description = "Taken offline by staff with respondent acknowledgment and time extension"
    )
    Predicate<CaseData> afterClaimNotifiedAckExtension =
        CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1
            .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent1)
            .and(
                (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.or(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep)
                     .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent2
                     .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent2))
                )
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate()
                            .and(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep.negate()))
            );

    @BusinessRule(
        group = "TakenOffline",
        summary = "Taken offline after claim details notified (acknowledged, no response, extension)",
        description = "Taken offline by staff with respondent acknowledgment and time extension"
    )
    Predicate<CaseData> afterClaimNotifiedAckNoResponseExtension =
        CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1
            .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent1)
            .and(
                (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.or(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep)
                    .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent2
                             .and(CaseDataPredicate.Respondent.hasResponseDateRespondent2.negate())
                             .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent2))
                )
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate()
                            .and(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep.negate()))
            );

    @BusinessRule(
        group = "TakenOffline",
        summary = "Taken offline after claim details notified not Dismissed (no acknowledged, no response, no Extension)",
        description = "Taken offline by staff with no respondent acknowledgment and no time extension"
    )
    Predicate<CaseData> afterClaimNotifiedNoAckNoResponseNoExtension =
            CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1.negate()
            .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent1.negate())
            .and(CaseDataPredicate.Claim.hasDismissedDate.negate())
            .and(
                (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.or(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep)
                    .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent2.negate()
                             .and(CaseDataPredicate.Respondent.hasResponseDateRespondent2.negate())
                             .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent2.negate()))
                )
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate()
                            .and(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep.negate()))
            );

    @BusinessRule(
        group = "TakenOffline",
        summary = "Taken offline after claim details notified (acknowledged, no response, no Extension)",
        description = "Taken offline by staff with no respondent acknowledgment and no time extension"
    )
    Predicate<CaseData> afterClaimNotifiedAckNoResponseNoExtension =
        CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1
            .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent1.negate())
            .and(
                (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.or(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep)
                    .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent2
                        .and(CaseDataPredicate.Respondent.hasResponseDateRespondent2.negate())
                        .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent2.negate()))
                )
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate()
                        .and(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep.negate()))
            );

    @BusinessRule(
        group = "TakenOffline",
        summary = "Offline caused by Camunda processing",
        description = "Indicates the case was taken offline via Camunda/automated processing (takenOffline date present)"
    )
    Predicate<CaseData> isDefendantNoCOnlineForCaseAfterJBA =
        CaseDataPredicate.Lip.isPartyUnrepresented
            .and(CaseDataPredicate.Judgment.isByAdmission)
            .and(CaseDataPredicate.TakenOffline.dateExists)
            .and(CaseDataPredicate.Claim.hasChangeOfRepresentation);

}
