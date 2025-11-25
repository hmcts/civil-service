package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

/**
 * Cohesive predicates about claimant intention: proceed vs not proceed.
 * Logic copied as-is from FlowPredicate to avoid functional changes.
 */
public final class ClaimantIntentionPredicates {

    private ClaimantIntentionPredicates() {
        // Utility class
    }

    public static final Predicate<CaseData> fullDefenceProceed = ClaimantIntentionPredicates::getPredicateForClaimantIntentionProceed;

    private static boolean getPredicateForClaimantIntentionProceed(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP, ONE_V_ONE ->
                    YES.equals(caseData.getApplicant1ProceedWithClaim());
                case TWO_V_ONE -> YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1());
            };
        } else {
            return switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        || YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2());
                case ONE_V_ONE -> YES.equals(caseData.getApplicant1ProceedWithClaim());
                case TWO_V_ONE -> YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                    || YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1());
            };
        }
    }

    public static final Predicate<CaseData> fullDefenceNotProceed = ClaimantIntentionPredicates::getPredicateForClaimantIntentionNotProceed;

    private static boolean getPredicateForClaimantIntentionNotProceed(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP, ONE_V_ONE ->
                    NO.equals(caseData.getApplicant1ProceedWithClaim());
                case TWO_V_ONE -> NO.equals(caseData.getApplicant1ProceedWithClaimSpec2v1());
            };
        } else {
            return switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        && NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2());
                case ONE_V_ONE -> NO.equals(caseData.getApplicant1ProceedWithClaim());
                case TWO_V_ONE -> NO.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                    && NO.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1());
            };
        }
    }
}
