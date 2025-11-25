package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@SuppressWarnings("java:S1214")
public interface ResponsePredicate {

    @BusinessRule(
        group = "Response",
        summary = "Notification acknowledged",
        description = "At least one required defendant has acknowledged notification (matches State Flow 'NOTIFICATION_ACKNOWLEDGED')"
    )
    Predicate<CaseData> notificationAcknowledged =
        CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1.or(
            (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.or(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep))
                .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent2)
        );

    @BusinessRule(
        group = "Response",
        summary = "Respondent time extension granted",
        description = "A respondent has been granted a time extension to respond (aligns with State Flow time-extension conditions)"
    )
    Predicate<CaseData> respondentTimeExtension =
        CaseDataPredicate.Respondent.hasTimeExtensionRespondent1.or(
            CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent2)
        );

    @BusinessRule(
        group = "Response",
        summary = "All required responses received",
        description = "All required defendant responses for the multi-party scenario have been received (State Flow 'ALL_RESPONSES_RECEIVED')"
    )
    Predicate<CaseData> allResponsesReceived =
        CaseDataPredicate.Respondent.hasResponseDateRespondent1
            .and(
                (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.and(CaseDataPredicate.Respondent.hasResponseDateRespondent2))
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate())
            );

    @BusinessRule(
        group = "Response",
        summary = "Awaiting other responses — full defence received",
        description = "In 1v2(two legal reps) one defendant returned a full defence and the other has not yet provided a response type"
    )
    Predicate<CaseData> awaitingResponsesFullDefenceReceived =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.isTypeRespondent1(RespondentResponseType.FULL_DEFENCE).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent2.negate()))
                    .or(CaseDataPredicate.Respondent.isTypeRespondent2(RespondentResponseType.FULL_DEFENCE).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent1.negate()))
            );

    @BusinessRule(
        group = "Response",
        summary = "Awaiting other responses — full defence received (SPEC)",
        description = "In 1v2(two legal reps) one defendant returned a full defence and the other has not yet provided a response type"
    )
    Predicate<CaseData> awaitingResponsesFullDefenceReceivedSpec =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.isTypeSpecRespondent1(RespondentResponseTypeSpec.FULL_DEFENCE).and(CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent2.negate()))
                    .or(CaseDataPredicate.Respondent.isTypeSpecRespondent2(RespondentResponseTypeSpec.FULL_DEFENCE).and(CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent1.negate()))
            );

    @BusinessRule(
        group = "Response",
        summary = "Awaiting other responses — full admission received",
        description = "In 1v2(two legal reps) one defendant returned a full admission and the other has not yet provided a response type"
    )
    Predicate<CaseData> awaitingResponsesFullAdmitReceived =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.isTypeRespondent1(RespondentResponseType.FULL_ADMISSION).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent2.negate()))
                    .or(CaseDataPredicate.Respondent.isTypeRespondent2(RespondentResponseType.FULL_ADMISSION).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent1.negate()))
            );

    @BusinessRule(
        group = "Response",
        summary = "Awaiting other responses — full admission received (SPEC)",
        description = "In 1v2(two legal reps) one defendant returned a full admission and the other has not yet provided a response type"
    )
    Predicate<CaseData> awaitingResponsesFullAdmitReceivedSpec =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.isTypeSpecRespondent1(RespondentResponseTypeSpec.FULL_ADMISSION).and(CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent2.negate()))
                    .or(CaseDataPredicate.Respondent.isTypeSpecRespondent2(RespondentResponseTypeSpec.FULL_ADMISSION).and(CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent1.negate()))
            );

    @BusinessRule(
        group = "Response",
        summary = "Awaiting other responses — non-full response received",
        description = "In 1v2(two legal reps) one defendant returned a non-full-defence/admission response and the other has not yet provided a response type"
    )
    Predicate<CaseData> awaitingResponsesNonFullDefenceOrFullAdmitReceived =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.hasResponseTypeRespondent1
                    .and(CaseDataPredicate.Respondent.isTypeRespondent1(RespondentResponseType.FULL_DEFENCE).negate()
                        .and(CaseDataPredicate.Respondent.isTypeRespondent1(RespondentResponseType.FULL_ADMISSION).negate()
                            .and(CaseDataPredicate.Respondent.hasResponseTypeRespondent2.negate()))))
                    .or(CaseDataPredicate.Respondent.hasResponseTypeRespondent2
                        .and(CaseDataPredicate.Respondent.isTypeRespondent2(RespondentResponseType.FULL_DEFENCE).negate()
                            .and(CaseDataPredicate.Respondent.isTypeRespondent2(RespondentResponseType.FULL_ADMISSION).negate()
                                .and(CaseDataPredicate.Respondent.hasResponseTypeRespondent1.negate()))))
            );

    @BusinessRule(
        group = "Response",
        summary = "Awaiting other responses — non-full response received (SPEC)",
        description = "In 1v2(two legal reps) one defendant returned a non-full-defence/admission response and the other has not yet provided a response type"
    )
    Predicate<CaseData> awaitingResponsesNonFullDefenceOrFullAdmitReceivedSpec =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent1
                    .and(CaseDataPredicate.Respondent.isTypeSpecRespondent1(RespondentResponseTypeSpec.FULL_DEFENCE).negate()
                         .and(CaseDataPredicate.Respondent.isTypeSpecRespondent1(RespondentResponseTypeSpec.FULL_ADMISSION).negate()
                             .and(CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent2.negate()))))
                    .or(CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent2
                        .and(CaseDataPredicate.Respondent.isTypeSpecRespondent2(RespondentResponseTypeSpec.FULL_DEFENCE).negate()
                             .and(CaseDataPredicate.Respondent.isTypeSpecRespondent2(RespondentResponseTypeSpec.FULL_ADMISSION).negate()
                                 .and(CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent1.negate()))))
            );

    @BusinessRule(
        group = "Response",
        summary = "One-v-one response flag (spec)",
        description = "Flag indicating a one-v-one response was provided (used in SPEC response routing)"
    )
    Predicate<CaseData> isOneVOneResponseFlagSpec = CaseDataPredicate.Claim.hasOneVOneResponseFlag;


    @BusinessRule(
        group = "Response",
        summary = "Matches the specified non-SPEC response type",
        description = "Checks if respondent(s) non-SPEC response(s) match the provided RespondentResponseType according to multi-party scenario rules"
    )
    public static Predicate<CaseData> isType(RespondentResponseType responseType) {
        return CaseDataPredicate.Respondent.hasResponseDateRespondent1
            .and(
                c -> switch (getMultiPartyScenario(c)) {
                    case ONE_V_TWO_ONE_LEGAL_REP -> CaseDataPredicate.Respondent.isTypeRespondent1(responseType).test(c)
                        && (CaseDataPredicate.Respondent.isSameResponseFlag.test(c)
                        || CaseDataPredicate.Respondent.isTypeRespondent2(responseType).test(c));
                    case ONE_V_TWO_TWO_LEGAL_REP -> CaseDataPredicate.Respondent.isTypeRespondent1(responseType)
                        .and(CaseDataPredicate.Respondent.isTypeRespondent2(responseType)).test(c);
                    case ONE_V_ONE -> CaseDataPredicate.Respondent.isTypeRespondent1(responseType).test(c);
                    case TWO_V_ONE ->
                        CaseDataPredicate.Respondent.isTypeRespondent1(responseType)
                            .and(CaseDataPredicate.Respondent.isTypeRespondent1ToApplicant2(responseType)).test(c);
                }
            );
    }

    @BusinessRule(
        group = "Response",
        summary = "Matches the specified type for a SPEC",
        description = "Checks if the respondent's response type matches the specified type for a SPEC claim"
    )
    public static Predicate<CaseData> isType(RespondentResponseTypeSpec responseType) {
        return CaseDataPredicate.Claim.isSpecClaim.and(CaseDataPredicate.Respondent.hasResponseDateRespondent1)
            .and(c -> switch (getMultiPartyScenario(c)) {
                     case ONE_V_TWO_ONE_LEGAL_REP ->
                         CaseDataPredicate.Respondent.isTypeSpecRespondent1(responseType).test(c)
                             && (CaseDataPredicate.Respondent.isSameResponseFlag.test(c)
                             || CaseDataPredicate.Respondent.isTypeSpecRespondent2(responseType).test(c));
                     case ONE_V_TWO_TWO_LEGAL_REP -> CaseDataPredicate.Respondent.isTypeSpecRespondent1(responseType)
                         .and(CaseDataPredicate.Respondent.isTypeSpecRespondent2(responseType)).test(c)
                         && responseType == RespondentResponseTypeSpec.FULL_DEFENCE;
                     case ONE_V_ONE -> CaseDataPredicate.Respondent.isTypeSpecRespondent1(responseType).test(c);
                     case TWO_V_ONE -> CaseDataPredicate.Claimant.defendantSingleResponseToBothClaimants.test(c)
                         ? CaseDataPredicate.Respondent.isTypeSpecRespondent1(responseType).test(c)
                         : CaseDataPredicate.Claimant.responseTypeSpecClaimant1(responseType)
                         .and(CaseDataPredicate.Claimant.responseTypeSpecClaimant2(responseType)).test(c);
                 }
            );
    }

}
