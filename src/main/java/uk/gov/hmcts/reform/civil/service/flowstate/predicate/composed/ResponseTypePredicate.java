package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

public final class ResponseTypePredicate {

    @BusinessRule(
        group = "ResponseType",
        summary = "Matches the specified non-SPEC response type",
        description = "Checks if respondent(s) non-SPEC response(s) match the provided RespondentResponseType according to multi-party scenario rules"
    )
    public static Predicate<CaseData> is(RespondentResponseType responseType) {
        return CaseDataPredicate.Respondent.hasResponseDateRespondent1
            .and(
            c -> switch (getMultiPartyScenario(c)) {
                case ONE_V_TWO_ONE_LEGAL_REP -> CaseDataPredicate.Respondent.isTypeRespondent1(responseType).test(c)
                    && (CaseDataPredicate.Respondent.respondentsHaveSameResponseFlag.test(c)
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
        group = "ResponseType",
        summary = "Matches the specified type for a SPEC",
        description = "Checks if the respondent's response type matches the specified type for a SPEC claim"
    )
    public static Predicate<CaseData> is(RespondentResponseTypeSpec responseType) {
        return CaseDataPredicate.Claim.isSpecClaim.and(CaseDataPredicate.Respondent.hasResponseDateRespondent1)
            .and(c ->
             switch (getMultiPartyScenario(c)) {
                case ONE_V_TWO_ONE_LEGAL_REP ->
                            CaseDataPredicate.Respondent.isTypeSpecRespondent1(responseType).test(c)
                                && (CaseDataPredicate.Respondent.respondentsHaveSameResponseFlag.test(c)
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

    private ResponseTypePredicate() {
    }

}
