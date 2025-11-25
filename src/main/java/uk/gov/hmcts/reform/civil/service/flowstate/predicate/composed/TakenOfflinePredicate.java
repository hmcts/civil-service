package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

public final class TakenOfflinePredicate {

    @BusinessRule(
        group = "TakenOffline",
        summary = "Case taken offline by system",
        description = "Taken offline date exists and no change-of-representation recorded"
    )
    public static final Predicate<CaseData> bySystem =
        CaseDataPredicate.TakenOffline.dateExists
            .and(CaseDataPredicate.TakenOffline.hasChangeOfRepresentation.negate());

    @BusinessRule(
        group = "TakenOffline",
        summary = "Case taken offline by staff",
        description = "Taken offline by staff date exists"
    )
    public static final Predicate<CaseData> byStaff = CaseDataPredicate.TakenOffline.byStaffDateExists;

    @BusinessRule(
        group = "TakenOffline",
        summary = "After SDO (draw directions order) handling",
        description = "Case marked for draw directions order and not marked as 'not suitable for SDO', takenOffline date exists and not taken offline by staff"
    )
    public static final Predicate<CaseData> afterSdo =
        CaseDataPredicate.TakenOffline.hasDrawDirectionsOrderRequired
            .and(CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable.negate())
            .and(CaseDataPredicate.TakenOffline.dateExists)
            .and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate());

    @BusinessRule(
        group = "TakenOffline",
        summary = "SDO not drawn",
        description = "Case has 'not suitable for SDO' reason and is taken offline without a staff offline date"
    )
    public static final Predicate<CaseData> sdoNotDrawn =
        CaseDataPredicate.TakenOffline.hasSdoReasonNotSuitable
            .and(CaseDataPredicate.TakenOffline.dateExists)
            .and(CaseDataPredicate.TakenOffline.byStaffDateExists.negate());

    @BusinessRule(
        group = "TakenOffline",
        summary = "Taken offline after claim notified",
        description = "Case taken offline by staff after claim notification but before claim details notification and respondent acknowledgement"
    )
    public static final Predicate<CaseData> afterClaimNotified =
        CaseDataPredicate.TakenOffline.byStaffDateExists
            .and(CaseDataPredicate.ClaimDetails.hasNotificationDate.negate())
            .and(CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent1.negate())
            .and(CaseDataPredicate.ClaimDetails.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.ClaimDetails.hasFutureNotificationDeadline);

    @BusinessRule(
        group = "TakenOffline",
        summary = "Taken offline after claim details notified",
        description = "Taken offline by staff with no respondent acknowledgment/response and no time extension — legacy offline path"
    )
    public static final Predicate<CaseData> afterClaimDetailsNotified =
        // Not tested !!! but equivalent to legacy logic - add new test....
        CaseDataPredicate.TakenOffline.byStaffDateExists
            .and(CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent1.negate())
            .and(CaseDataPredicate.ClaimDetails.hasResponseDateRespondent1.negate())
            .and(CaseDataPredicate.ClaimDetails.hasTimeExtensionRespondent1.negate())
            .and(CaseDataPredicate.Claim.hasDismissedDate.negate())
            .and(
                (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.or(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep)
                    .and(CaseDataPredicate.ClaimDetails.hasResponseDateRespondent2.negate()
                             .and(CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent2.negate())
                             .and(CaseDataPredicate.ClaimDetails.hasTimeExtensionRespondent2.negate()))
                )
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate()
                            .and(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep.negate()))
            );

    @BusinessRule(
        group = "TakenOffline",
        summary = "Offline caused by Camunda processing",
        description = "Indicates the case was taken offline via Camunda/automated processing (takenOffline date present)"
    )
    public static final Predicate<CaseData> isDefendantNoCOnlineForCaseAfterJBA =
        CaseDataPredicate.Lip.partyIsUnrepresented
            .and(CaseDataPredicate.Judgment.isByAdmission)
            .and(CaseDataPredicate.TakenOffline.dateExists)
            .and(CaseDataPredicate.TakenOffline.hasChangeOfRepresentation);

    private TakenOfflinePredicate() {
    }

}
