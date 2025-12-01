package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;

@SuppressWarnings("java:S1214")
public non-sealed interface DivergencePredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Divergence",
        summary = "Responses diverge - generate DQs then go offline",
        description = "Responses diverge and discrete directions questionnaires must be produced before going offline."
    )
    Predicate<CaseData> divergentRespondWithDQAndGoOffline =
        c -> switch (getMultiPartyScenario(c)) {
            case ONE_V_TWO_ONE_LEGAL_REP -> CaseDataPredicate.Respondent.responsesDiffer.test(c)
                && (CaseDataPredicate.Respondent.isTypeRespondent1(FULL_DEFENCE).test(c)
                || CaseDataPredicate.Respondent.isTypeRespondent2(FULL_DEFENCE).test(c));
            case ONE_V_TWO_TWO_LEGAL_REP -> CaseDataPredicate.Respondent.responsesDiffer.test(c)
                && ((CaseDataPredicate.Respondent.isTypeRespondent2(FULL_DEFENCE)
                .and(CaseDataPredicate.Respondent.respondent2ResponseAfterRespondent1)).test(c)
                || (CaseDataPredicate.Respondent.isTypeRespondent1(FULL_DEFENCE)
                .and(CaseDataPredicate.Respondent.respondent1ResponseAfterRespondent2)).test(c));
            case TWO_V_ONE -> (CaseDataPredicate.Respondent.isTypeRespondent1(FULL_DEFENCE).test(c)
                || CaseDataPredicate.Respondent.isTypeRespondent1ToApplicant2(FULL_DEFENCE).test(c))
                && !(CaseDataPredicate.Respondent.isTypeRespondent1(FULL_DEFENCE).test(c)
                && CaseDataPredicate.Respondent.isTypeRespondent1ToApplicant2(FULL_DEFENCE).test(c));
            default -> false;
        };

    @BusinessRule(
        group = "Divergence",
        summary = "Responses diverge - go offline",
        description = "Defendant responses diverge so the case must go offline."
    )
    Predicate<CaseData> divergentRespondGoOffline =
        c ->
            switch (getMultiPartyScenario(c)) {
                case ONE_V_TWO_TWO_LEGAL_REP -> CaseDataPredicate.Respondent.responsesDiffer.test(c)
                    && ((CaseDataPredicate.Respondent.isTypeRespondent2(FULL_DEFENCE).negate()
                    .and(CaseDataPredicate.Respondent.respondent2ResponseAfterRespondent1)).test(c)
                    || (CaseDataPredicate.Respondent.isTypeRespondent1(FULL_DEFENCE).negate()
                    .and(CaseDataPredicate.Respondent.respondent1ResponseAfterRespondent2)).test(c)
                    || (CaseDataPredicate.Respondent.isTypeRespondent1(FULL_DEFENCE).negate()
                    .and(CaseDataPredicate.Respondent.isTypeRespondent2(FULL_DEFENCE).negate())).test(c));
                case ONE_V_TWO_ONE_LEGAL_REP -> CaseDataPredicate.Respondent.responsesDiffer.test(c)
                    && CaseDataPredicate.Respondent.isTypeRespondent1(FULL_DEFENCE).negate().test(c)
                    && CaseDataPredicate.Respondent.isTypeRespondent2(FULL_DEFENCE).negate().test(c);
                case TWO_V_ONE -> !(CaseDataPredicate.Respondent.isTypeRespondent1(FULL_DEFENCE).test(c)
                    || CaseDataPredicate.Respondent.isTypeRespondent1ToApplicant2(FULL_DEFENCE).test(c));
                default -> false;
            };

    @BusinessRule(
        group = "Divergence",
        summary = "SPEC divergence - generate DQs then go offline",
        description = "SPEC divergent responses requiring DQs before going offline."
    )
    Predicate<CaseData> divergentRespondWithDQAndGoOfflineSpec =
        CaseDataPredicate.Claim.isSpecClaim.and(c ->
            switch (getMultiPartyScenario(c)) {
                case ONE_V_TWO_ONE_LEGAL_REP ->
                    CaseDataPredicate.Respondent.isTypeSpecRespondent1(null).negate()
                        .and(CaseDataPredicate.Respondent.isTypeSpecRespondent2(
                            null).negate())
                        .and(CaseDataPredicate.Respondent.isSameResponseFlag.negate())
                        .and(CaseDataPredicate.Respondent.responsesDifferSpec)
                        .and(CaseDataPredicate.Respondent.isTypeSpecRespondent1(
                                RespondentResponseTypeSpec.FULL_DEFENCE)
                                 .or(CaseDataPredicate.Respondent.isTypeSpecRespondent2(
                                     RespondentResponseTypeSpec.FULL_DEFENCE)))
                        .test(c);
                case ONE_V_TWO_TWO_LEGAL_REP ->
                    CaseDataPredicate.Respondent.isTypeSpecRespondent1(null).negate()
                        .and(CaseDataPredicate.Respondent.isTypeSpecRespondent2(
                            null).negate())
                        .and(CaseDataPredicate.Respondent.hasResponseDateRespondent1)
                        .and(CaseDataPredicate.Respondent.hasResponseDateRespondent2)
                        .and(CaseDataPredicate.Respondent.isTypeSpecRespondent1(
                                RespondentResponseTypeSpec.FULL_DEFENCE).negate()
                                 .or(CaseDataPredicate.Respondent.responsesDifferSpec))
                        .test(c);
                case TWO_V_ONE ->
                    (CaseDataPredicate.Claimant.responseTypeSpecClaimant2(
                            RespondentResponseTypeSpec.FULL_DEFENCE)
                        .or(CaseDataPredicate.Claimant.responseTypeSpecClaimant1(
                            RespondentResponseTypeSpec.FULL_DEFENCE)))
                        .and((CaseDataPredicate.Claimant.responseTypeSpecClaimant2(
                                RespondentResponseTypeSpec.FULL_DEFENCE)
                            .and(CaseDataPredicate.Claimant.responseTypeSpecClaimant1(
                                RespondentResponseTypeSpec.FULL_DEFENCE))).negate())
                        .test(c);
                default -> false;
            });

    @BusinessRule(
        group = "Divergence",
        summary = "SPEC divergence - go offline",
        description = "SPEC defendant/claimant responses diverge so the case must go offline."
    )
    Predicate<CaseData> divergentRespondGoOfflineSpec =
        CaseDataPredicate.Claim.isSpecClaim.and(c ->
            switch (getMultiPartyScenario(c)) {
                case ONE_V_TWO_ONE_LEGAL_REP ->
                    CaseDataPredicate.Respondent.isTypeSpecRespondent1(null).negate()
                        .and(CaseDataPredicate.Respondent.isTypeSpecRespondent2(null).negate())
                        .and(CaseDataPredicate.Respondent.isSameResponseFlag.negate())
                        .and(CaseDataPredicate.Respondent.responsesDifferSpec)
                        .and(CaseDataPredicate.Respondent.isTypeSpecRespondent1(
                            RespondentResponseTypeSpec.FULL_DEFENCE).negate())
                        .and(CaseDataPredicate.Respondent.isTypeSpecRespondent2(
                            RespondentResponseTypeSpec.FULL_DEFENCE).negate())
                        .test(c);
                case TWO_V_ONE ->
                    CaseDataPredicate.Claimant.responseTypeSpecClaimant1(
                            RespondentResponseTypeSpec.FULL_DEFENCE).negate()
                        .and(CaseDataPredicate.Claimant.responseTypeSpecClaimant2(
                            RespondentResponseTypeSpec.FULL_DEFENCE).negate())
                        .and(CaseDataPredicate.Claimant.responsesDifferSpec)
                        .test(c);
                default -> false;
            });

}
