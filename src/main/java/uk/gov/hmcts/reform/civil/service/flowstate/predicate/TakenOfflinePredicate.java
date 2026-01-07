package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public non-sealed interface TakenOfflinePredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "TakenOffline",
        summary = "Case taken offline by system",
        description = "System-driven taken-offline marker present (takenOffline date exists)"
    )
    Predicate<CaseData> bySystem = CaseDataPredicate.TakenOffline.dateExists;

    @BusinessRule(
        group = "TakenOffline",
        summary = "Case taken offline by staff",
        description = "Manual taken-offline marker present (takenOfflineByStaff date exists)"
    )
    Predicate<CaseData> byStaff = CaseDataPredicate.TakenOffline.byStaffDateExists;

    @BusinessRule(
        group = "TakenOffline",
        summary = "SDO not Suitable",
        description = "Case flagged 'not suitable for SDO'"
    )
    Predicate<CaseData> sdoNotSuitable = CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable;

    @BusinessRule(
        group = "TakenOffline",
        summary = "SDO not drawn",
        description = "Case flagged 'not suitable for SDO' and a taken-offline date exists"
    )
    Predicate<CaseData> sdoNotDrawn =
        CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable
            .and(CaseDataPredicate.TakenOffline.dateExists);

    @BusinessRule(
        group = "TakenOffline",
        summary = "Before SDO (draw directions order) handling",
        description = "Applicant response date present; draw-directions-order NOT required; NOT marked 'not suitable for SDO'"
    )
    Predicate<CaseData> beforeSdo =
        CaseDataPredicate.Applicant.hasResponseDateApplicant1
            .and(CaseDataPredicate.TakenOffline.hasDrawDirectionsOrderRequired.negate())
            .and(CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable.negate());

    @BusinessRule(
        group = "TakenOffline",
        summary = "After SDO (draw directions order) handling",
        description = "Draw-directions-order required; NOT marked 'not suitable for SDO'"
    )
    Predicate<CaseData> afterSdo =
        CaseDataPredicate.TakenOffline.hasDrawDirectionsOrderRequired
            .and(CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable.negate());

    @BusinessRule(
        group = "TakenOffline",
        summary = "Not suitable for SDO (no draw directions order)",
        description = "Draw-directions-order NOT required; marked 'not suitable for SDO'"
    )
    Predicate<CaseData> afterSdoNotSuitable =
        CaseDataPredicate.TakenOffline.hasDrawDirectionsOrderRequired.negate()
            .and(CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable);

    @BusinessRule(
        group = "TakenOffline",
        summary = "Before claim notification (submitted, not notified)",
        description = "Case submitted; claim notification NOT set (no notification deadline/date)"
    )
    Predicate<CaseData> beforeClaimIssue =
        // If SPEC and UNSPEC claim ClaimNotificationDeadline will be set when the case is issued
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
        summary = "After claim notified - respondent extension, no AoS/response",
        description = "Respondent 1 has a time extension but has not acknowledged service or responded"
    )
    Predicate<CaseData> afterClaimNotifiedExtension =
            CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1.negate()
            .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent1);

    @BusinessRule(
        group = "TakenOffline",
        summary = "Taken offline after claim notified",
        description = "After claim notified but before claim details notification and any respondent acknowledgement"
    )
    Predicate<CaseData> afterClaimNotifiedFutureDeadline =
            CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1.negate()
            .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.ClaimDetails.hasNotificationDate.negate())
            .and(CaseDataPredicate.ClaimDetails.futureNotificationDeadline);

    @BusinessRule(
        group = "TakenOffline",
        summary = "Taken offline after claim details notified (acknowledged, N/A response, extension)",
        description = "Respondent(s) acknowledged service; time extension(s) granted"
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
        description = "Respondent(s) acknowledged service; no response yet; time extension(s) granted"
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
        description = "Respondent(s) acknowledged service; no response yet; no time extension"
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
        description = "Case taken offline via Camunda/automation (takenOffline date present) where LiP NoC/JO by admission and representation change apply"
    )
    Predicate<CaseData> isDefendantNoCOnlineForCaseAfterJBA =
        CaseDataPredicate.Lip.isPartyUnrepresented
            .and(CaseDataPredicate.Judgment.isByAdmission)
            .and(CaseDataPredicate.TakenOffline.dateExists)
            .and(CaseDataPredicate.Claim.hasChangeOfRepresentation);

}
