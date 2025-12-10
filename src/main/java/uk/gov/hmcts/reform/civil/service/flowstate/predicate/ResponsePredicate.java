package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@SuppressWarnings("java:S1214")
public non-sealed interface ResponsePredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Response",
        summary = "Case has a second respondent",
        description = "Checks if respondent 2 is present in the case data."
    )
    Predicate<CaseData> hasRespondent2 = CaseDataPredicate.Respondent.hasRespondent2;

    @BusinessRule(
        group = "Response",
        summary = "Case has a second respondent",
        description = "Checks if respondent 2 is present in the case data."
    )
    Predicate<CaseData> isNotSameLegalRepresentative = CaseDataPredicate.Respondent.isNotSameLegalRepresentative;

    @BusinessRule(
        group = "Response",
        summary = "Notification acknowledged",
        description = "At least one required defendant has acknowledged service " +
            "(matches State Flow 'NOTIFICATION_ACKNOWLEDGED')"
    )
    Predicate<CaseData> notificationAcknowledged =
        CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent1.or(
            (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.or(CaseDataPredicate.MultiParty.isOneVTwoOneLegalRep))
                .and(CaseDataPredicate.Respondent.hasAcknowledgedNotificationRespondent2)
        );

    @BusinessRule(
        group = "Response",
        summary = "Respondent time extension granted",
        description = "A defendant has obtained a time extension to respond"
    )
    Predicate<CaseData> respondentTimeExtension =
        CaseDataPredicate.Respondent.hasTimeExtensionRespondent1.or(
            CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.and(CaseDataPredicate.Respondent.hasTimeExtensionRespondent2)
        );

    @BusinessRule(
        group = "Response",
        summary = "Every required defendant response has been received",
        description = "All required defendant responses for the current multi-party scenario have been received " +
            "(State Flow 'ALL_RESPONSES_RECEIVED')"
    )
    Predicate<CaseData> allResponsesReceived =
        CaseDataPredicate.Respondent.hasResponseDateRespondent1
            .and(
                (CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.and(CaseDataPredicate.Respondent.hasResponseDateRespondent2))
                    .or(CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep.negate())
            );

    @BusinessRule(
        group = "Response",
        summary = "Awaiting other responses - full defence received",
        description = "In a 1v2 two‑solicitor case one defendant has provided a full defence and the co‑defendant is yet " +
            "to respond"
    )
    Predicate<CaseData> awaitingResponsesFullDefenceReceived =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.isTypeRespondent1(RespondentResponseType.FULL_DEFENCE).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent2.negate()))
                    .or(CaseDataPredicate.Respondent.isTypeRespondent2(RespondentResponseType.FULL_DEFENCE).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent1.negate()))
            );

    @BusinessRule(
        group = "Response",
        summary = "Awaiting other responses - full defence received (SPEC)",
        description = "In a 1v2 two‑solicitor SPEC case one defendant has provided a full defence and the co‑defendant " +
            "is yet to respond"
    )
    Predicate<CaseData> awaitingResponsesFullDefenceReceivedSpec =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.isTypeSpecRespondent1(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .and(CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent2.negate()))
                    .or(CaseDataPredicate.Respondent.isTypeSpecRespondent2(RespondentResponseTypeSpec.FULL_DEFENCE)
                            .and(CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent1.negate()))
            );

    @BusinessRule(
        group = "Response",
        summary = "Awaiting other responses - full admission received",
        description = "In a 1v2 two‑solicitor case one defendant has provided a full admission and the co‑defendant is " +
            "yet to respond"
    )
    Predicate<CaseData> awaitingResponsesFullAdmitReceived =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.isTypeRespondent1(RespondentResponseType.FULL_ADMISSION).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent2.negate()))
                    .or(CaseDataPredicate.Respondent.isTypeRespondent2(RespondentResponseType.FULL_ADMISSION).and(CaseDataPredicate.Respondent.hasResponseTypeRespondent1.negate()))
            );

    @BusinessRule(
        group = "Response",
        summary = "Awaiting other responses - full admission received (SPEC)",
        description = "In a 1v2 two‑solicitor SPEC case one defendant has provided a full admission and the co‑defendant " +
            "is yet to respond"
    )
    Predicate<CaseData> awaitingResponsesFullAdmitReceivedSpec =
        CaseDataPredicate.MultiParty.isOneVTwoTwoLegalRep
            .and(
                (CaseDataPredicate.Respondent.isTypeSpecRespondent1(RespondentResponseTypeSpec.FULL_ADMISSION)
                    .and(CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent2.negate()))
                    .or(CaseDataPredicate.Respondent.isTypeSpecRespondent2(RespondentResponseTypeSpec.FULL_ADMISSION)
                            .and(CaseDataPredicate.Respondent.hasResponseTypeSpecRespondent1.negate()))
            );

    @BusinessRule(
        group = "Response",
        summary = "Awaiting other responses - non-full response received",
        description = "In a 1v2 two‑solicitor case one defendant has provided a response other than full defence / " +
            "admission and the co‑defendant is yet to respond"
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
        summary = "Awaiting other responses - non-full response received (SPEC)",
        description = "In a 1v2 two‑solicitor SPEC case one defendant has provided a response other than full defence / " +
            "admission and the co‑defendant is yet to respond"
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
        description = "Flag indicating a one‑v‑one response was provided (used in SPEC response routing)"
    )
    Predicate<CaseData> isOneVOneResponseFlagSpec = CaseDataPredicate.Claim.hasOneVOneResponseFlag;

    @BusinessRule(
        group = "Response",
        summary = "Matches the specified non-SPEC response type",
        description = "Checks if respondent response type(s) for UNSPEC match the given RespondentResponseType for " +
            "multi‑party scenario"
    )
    static Predicate<CaseData> isType(RespondentResponseType responseType) {
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
        description = "Checks if the respondent response type(s) for SPEC match the given RespondentResponseTypeSpec for " +
            "multi‑party scenario"
    )
    static Predicate<CaseData> isType(RespondentResponseTypeSpec responseType) {
        return CaseDataPredicate.Claim.isSpecClaim.and(CaseDataPredicate.Respondent.hasResponseDateRespondent1)
            .and(c -> switch (getMultiPartyScenario(c)) {
                case ONE_V_TWO_ONE_LEGAL_REP -> CaseDataPredicate.Respondent.isTypeSpecRespondent1(responseType).test(c)
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
