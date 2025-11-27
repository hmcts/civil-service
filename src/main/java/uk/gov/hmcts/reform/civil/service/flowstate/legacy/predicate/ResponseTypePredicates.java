package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

/**
 * Cohesive predicates about a response type.
 * Logic copied as-is from FlowPredicate to avoid functional changes.
 */
public final class ResponseTypePredicates {

    private ResponseTypePredicates() {
        // Utility class
    }

    public static final Predicate<CaseData> fullAdmissionSpec = caseData ->
        getPredicateForResponseTypeSpec(caseData, RespondentResponseTypeSpec.FULL_ADMISSION);

    public static final Predicate<CaseData> partAdmissionSpec = caseData ->
        getPredicateForResponseTypeSpec(caseData, RespondentResponseTypeSpec.PART_ADMISSION);

    public static final Predicate<CaseData> counterClaimSpec = caseData ->
        getPredicateForResponseTypeSpec(caseData, RespondentResponseTypeSpec.COUNTER_CLAIM);

    public static final Predicate<CaseData> fullDefenceSpec = caseData ->
        getPredicateForResponseTypeSpec(caseData, RespondentResponseTypeSpec.FULL_DEFENCE);

    public static final Predicate<CaseData> counterClaim = caseData ->
        getPredicateForResponseType(caseData, COUNTER_CLAIM);

    private static boolean getPredicateForResponseType(CaseData caseData, RespondentResponseType responseType) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        boolean respondent1Matches = caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1ClaimResponseType() == responseType;

        return switch (scenario) {
            case ONE_V_TWO_ONE_LEGAL_REP -> respondent1Matches
                && (YES.equals(caseData.getRespondentResponseIsSame()) || caseData.getRespondent2ClaimResponseType() == responseType);
            case ONE_V_TWO_TWO_LEGAL_REP -> respondent1Matches && caseData.getRespondent2ClaimResponseType() == responseType;
            case ONE_V_ONE -> respondent1Matches;
            case TWO_V_ONE -> respondent1Matches && caseData.getRespondent1ClaimResponseTypeToApplicant2() == responseType;
        };
    }

    private static boolean getPredicateForResponseTypeSpec(CaseData caseData, RespondentResponseTypeSpec responseType) {
        // If not a SPEC claim, return false
        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return false;
        }

        boolean basePredicate = caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() == responseType;

        MultiPartyScenario scenario = getMultiPartyScenario(caseData);

        return switch (scenario) {
            case ONE_V_TWO_ONE_LEGAL_REP -> basePredicate && (YES.equals(caseData.getRespondentResponseIsSame())
                || caseData.getRespondent2ClaimResponseTypeForSpec() == responseType);
            case ONE_V_TWO_TWO_LEGAL_REP -> basePredicate
                && caseData.getRespondent2ClaimResponseTypeForSpec() == responseType
                // For the time being, even if the response is the same, 1v2ds only deals with full defence
                && responseType == RespondentResponseTypeSpec.FULL_DEFENCE;
            case ONE_V_ONE -> basePredicate;
            case TWO_V_ONE -> {
                if (YES.equals(caseData.getDefendantSingleResponseToBothClaimants())) {
                    yield responseType.equals(caseData.getRespondent1ClaimResponseTypeForSpec());
                } else {
                    yield responseType.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                        && responseType.equals(caseData.getClaimant2ClaimResponseTypeForSpec());
                }
            }
        };
    }
}
