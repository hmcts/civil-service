package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;

public final class ResponsesPredicate {

    @BusinessRule(
        group = "Responses",
        summary = "Notification acknowledged",
        description = "At least one required defendant has acknowledged notification (matches State Flow 'NOTIFICATION_ACKNOWLEDGED')"
    )
    public static final Predicate<CaseData> notificationAcknowledged =
        CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent1.or(
            (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.or(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep))
                .and(CaseDataPredicate.ClaimDetails.acknowledgedNotificationRespondent2)
        );

    @BusinessRule(
        group = "Responses",
        summary = "Respondent time extension granted",
        description = "A respondent has been granted a time extension to respond (aligns with State Flow time-extension conditions)"
    )
    public static final Predicate<CaseData> respondentTimeExtension =
        CaseDataPredicate.ClaimDetails.hasTimeExtensionRespondent1.or(
            CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.and(CaseDataPredicate.ClaimDetails.hasTimeExtensionRespondent2)
        );

    @BusinessRule(
        group = "Responses",
        summary = "All required responses received",
        description = "All required defendant responses for the multi-party scenario have been received (State Flow 'ALL_RESPONSES_RECEIVED')"
    )
    public static final Predicate<CaseData> allResponsesReceived =
        CaseDataPredicate.Respondent.hasResponseDateRespondent1
            .and(
                (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.and(CaseDataPredicate.Respondent.hasResponseDateRespondent2))
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate())
            );

    @BusinessRule(
        group = "Responses",
        summary = "Awaiting other responses — full defence received",
        description = "In 1v2(two legal reps) one defendant returned a full defence and the other has not yet provided a response type"
    )
    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceived =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.isTypeRespondent1(FULL_DEFENCE).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent2.negate()))
                    .or(CaseDataPredicate.Respondent.isTypeRespondent2(FULL_DEFENCE).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent1.negate()))
            );

    @BusinessRule(
        group = "Responses",
        summary = "Awaiting other responses — full admission received",
        description = "In 1v2(two legal reps) one defendant returned a full admission and the other has not yet provided a response type"
    )
    public static final Predicate<CaseData> awaitingResponsesFullAdmitReceived =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.isTypeRespondent1(FULL_ADMISSION).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent2.negate()))
                    .or(CaseDataPredicate.Respondent.isTypeRespondent2(FULL_ADMISSION).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent1.negate()))
            );

    @BusinessRule(
        group = "Responses",
        summary = "Awaiting other responses — non-full response received",
        description = "In 1v2(two legal reps) one defendant returned a non-full-defence/admission response and the other has not yet provided a response type"
    )
    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceOrFullAdmitReceived =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.hasResponseTypeRespondent1
                    .and(CaseDataPredicate.Respondent.isTypeRespondent1(FULL_DEFENCE).negate()
                             .and(CaseDataPredicate.Respondent.isTypeRespondent1(FULL_ADMISSION).negate()
                                      .and(CaseDataPredicate.Respondent.hasResponseTypeRespondent2.negate()))))
                    .or(CaseDataPredicate.Respondent.hasResponseTypeRespondent2
                            .and(CaseDataPredicate.Respondent.isTypeRespondent2(FULL_DEFENCE).negate()
                                     .and(CaseDataPredicate.Respondent.isTypeRespondent2(FULL_ADMISSION).negate()
                                              .and(CaseDataPredicate.Respondent.hasResponseTypeRespondent1.negate()))))
            );

    @BusinessRule(
        group = "Responses",
        summary = "One-v-one response flag (spec)",
        description = "Flag indicating a one-v-one response was provided (used in SPEC response routing)"
    )
    public static final Predicate<CaseData> isOneVOneResponseFlagSpec = CaseDataPredicate.Claim.hasOneVOneResponseFlag;

    private ResponsesPredicate() {
    }
}
